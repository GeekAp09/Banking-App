package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.ClientEntity
import com.mobile360.banking.data.ClientHoldingEntity
import com.mobile360.banking.data.DemoAccounts
import com.mobile360.banking.data.SessionRepository
import com.mobile360.banking.data.TradePreview
import com.mobile360.banking.data.TradeResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TradeStage { None, Confirm, Verify }

private data class TradeLocal(
    val side: String = "BUY",
    val qty: Int = 10,
    val stage: TradeStage = TradeStage.None,
    val codeError: String? = null,
    val result: String? = null,
    val resultIsError: Boolean = false,
    val authNote: String? = null,
    val busy: Boolean = false
)

data class TradeUiState(
    val loading: Boolean = true,
    val client: ClientEntity? = null,
    val holding: ClientHoldingEntity? = null,
    val cashCents: Long? = null,
    val preview: TradePreview? = null,
    val currentWeightPct: Double = 0.0,
    val side: String = "BUY",
    val qty: Int = 10,
    val stage: TradeStage = TradeStage.None,
    val codeError: String? = null,
    val result: String? = null,
    val resultIsError: Boolean = false,
    val authNote: String? = null,
    val busy: Boolean = false
) {
    val blocked get() = preview?.blockedReason != null
    val canSubmit get() = preview != null && !blocked && !busy
}

@OptIn(ExperimentalCoroutinesApi::class)
class TradeViewModel(private val bank: BankRepository, private val session: SessionRepository) : ViewModel() {
    private val id = MutableStateFlow("")
    private val local = MutableStateFlow(TradeLocal())

    private val clientFlow = id.flatMapLatest { if (it.isEmpty()) flowOf(null) else bank.client(it) }
    private val holdingsFlow = id.flatMapLatest { bank.clientHoldings(it) }
    private val previewFlow = combine(clientFlow, local.map { it.qty }.distinctUntilChanged()) { c, q -> c to q }
        .mapLatest { (c, q) -> if (c == null) null else bank.previewTrade(c.id, q) }

    val state: StateFlow<TradeUiState> = combine(clientFlow, holdingsFlow, previewFlow, local, id) { c, h, p, l, i ->
        val top = h.filter { !it.name.contains("Cash", true) }.maxByOrNull { it.valueCents }
        TradeUiState(
            loading = i.isEmpty(), client = c, holding = top,
            cashCents = h.firstOrNull { it.name.contains("Cash", true) }?.valueCents,
            preview = p,
            currentWeightPct = if (c != null && top != null && c.aumCents > 0) top.valueCents * 100.0 / c.aumCents else 0.0,
            side = l.side, qty = l.qty, stage = l.stage, codeError = l.codeError, result = l.result,
            resultIsError = l.resultIsError, authNote = l.authNote, busy = l.busy
        )
    }.catch { emit(TradeUiState(loading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TradeUiState())

    fun load(clientId: String) { if (id.value != clientId) { id.value = clientId; local.value = TradeLocal() } }
    fun setSide(s: String) = local.update { it.copy(side = s, result = null) }
    fun changeQty(delta: Int) = local.update { it.copy(qty = (it.qty + delta).coerceIn(1, 100_000), result = null) }
    fun openConfirm() { if (state.value.canSubmit) local.update { it.copy(stage = TradeStage.Confirm, result = null) } }
    fun toVerify() = local.update { it.copy(stage = TradeStage.Verify, codeError = null) }
    fun dismiss() = local.update { it.copy(stage = TradeStage.None, codeError = null) }

    fun requestAuthorization() {
        val cid = id.value
        viewModelScope.launch {
            bank.requestTradeAuthorization(cid)
            local.update { it.copy(authNote = "Authorisation prompt recorded for the client. The trade stays blocked until the client authorises.") }
        }
    }

    fun verifyAndSubmit(code: String) {
        if (code != DemoAccounts.DEMO_CODE) { local.update { it.copy(codeError = "That code is not correct. Try again.") }; return }
        val l = local.value
        local.update { it.copy(busy = true, codeError = null) }
        viewModelScope.launch {
            val r = bank.submitTrade(id.value, l.side, l.qty, reverified = true)
            local.update {
                when (r) {
                    is TradeResult.Success -> it.copy(busy = false, stage = TradeStage.None, resultIsError = false,
                        result = "Trade executed: ${if (l.side == "BUY") "bought" else "sold"} ${r.trade.quantity} units of ${r.trade.instrument}. Logged to the audit trail.")
                    is TradeResult.Blocked -> it.copy(busy = false, stage = TradeStage.None, resultIsError = true, result = r.reason)
                    TradeResult.NeedsReverification -> it.copy(busy = false, stage = TradeStage.Verify, codeError = "Fresh verification is required.")
                    TradeResult.Invalid -> it.copy(busy = false, stage = TradeStage.None, resultIsError = true, result = "This trade is not valid.")
                }
            }
        }
    }
}
