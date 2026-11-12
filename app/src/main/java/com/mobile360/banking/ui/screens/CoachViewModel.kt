package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.AppPrefs
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.InsightEntity
import com.mobile360.banking.data.PrefFlag
import com.mobile360.banking.data.RoundEventEntity
import com.mobile360.banking.data.SessionRepository
import com.mobile360.banking.data.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CoachUiState(
    val loaded: Boolean = false,
    val insights: List<InsightEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val rounds: List<RoundEventEntity> = emptyList(),
    val roundUps: Boolean = true
)

class CoachViewModel(private val bank: BankRepository, private val session: SessionRepository) : ViewModel() {
    val state: StateFlow<CoachUiState> =
        combine(bank.insights(), bank.transactions(), bank.rounds(), session.prefs) { i, t, r, p: AppPrefs ->
            CoachUiState(true, i, t, r, p.roundUps)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CoachUiState())
    val message = MutableStateFlow<String?>(null)

    private val roundFlag = PrefFlag.entries.first { it.name.contains("ROUND", true) && !it.name.contains("ALERT", true) }

    fun setCategory(txId: String, category: String) {
        viewModelScope.launch { bank.changeCategory(txId, category); message.value = "Category changed to $category. We will remember your change." }
    }

    fun toggleRoundUps(on: Boolean) {
        viewModelScope.launch { session.setFlag(roundFlag, on); message.value = if (on) "Round up is on." else "Round up is paused." }
    }

    fun roundUp(txId: String) {
        viewModelScope.launch {
            if (!state.value.roundUps) { message.value = "Turn on Round up to savings first."; return@launch }
            val e = bank.applyRoundUp(txId)
            message.value = if (e == null) "This transaction could not be rounded up." else if (e.status.equals("SAVED", true)) "Round up saved." else "Round skipped: ${e.note}"
        }
    }

    fun dismiss() { message.value = null }
}
