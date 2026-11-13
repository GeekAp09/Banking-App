package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.Limits
import com.mobile360.banking.data.Money
import com.mobile360.banking.data.SessionRepository
import com.mobile360.banking.data.TransferEntity
import com.mobile360.banking.data.TransferResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class ConfirmUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val editable: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val instant: Boolean = true,
    val amountText: String = "",
    val availableCents: Long = 0,
    val accountName: String = "",
    val sending: Boolean = false,
    val error: String? = null,
    val receipt: TransferEntity? = null
) {
    val amountCents: Long get() = parseCents(amountText)
    val overPerTransfer get() = amountCents > Limits.PER_TRANSFER_CENTS
    val initials get() = name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").ifBlank { "?" }
    val masked get() = phone.filter { it.isDigit() }.takeLast(4).let { if (it.isEmpty()) "" else "•••• $it" }

    // CR-011: live per-field errors, shown while typing
    val nameError: String? get() = if (name.isBlank()) "Enter the recipient's name." else null
    val phoneError: String? get() =
        if (phone.filter { it.isDigit() }.length < 7) "Enter a valid recipient phone number." else null
    val amountError: String? get() = when {
        amountText.isBlank() -> null
        parseCents(amountText) <= 0L -> "Enter an amount greater than \$0.00."
        overPerTransfer -> "Over the ${Money.formatWhole(Limits.PER_TRANSFER_CENTS)} per-transfer limit"
        else -> null
    }
}

fun parseCents(text: String): Long = try {
    if (text.isBlank() || text == ".") 0L else BigDecimal(text).movePointRight(2).toLong()
} catch (e: Exception) { 0L }

class ConfirmAndReceiptViewModel(private val bank: BankRepository, session: SessionRepository) : ViewModel() {
    private val _state = MutableStateFlow(ConfirmUiState())
    val state: StateFlow<ConfirmUiState> = _state
    private var loadedFor: String? = null

    fun load(recipientId: String) {
        if (loadedFor == recipientId) return
        loadedFor = recipientId
        viewModelScope.launch {
            val account = bank.accounts().first().firstOrNull { it.id == "a1" }
            val base = _state.value.copy(
                loading = false, availableCents = account?.balanceCents ?: 0, accountName = account?.name ?: ""
            )
            when (recipientId) {
                "qr" -> _state.value = base.copy(editable = true, instant = true)
                "manual" -> _state.value = base.copy(editable = true, instant = false)
                else -> {
                    val c = bank.contacts("").first().firstOrNull { it.id == recipientId }
                    _state.value = if (c == null) base.copy(notFound = true)
                    else base.copy(name = c.name, phone = c.phone, instant = c.instant)
                }
            }
        }
    }

    fun setAmount(v: String) {
        val cleaned = v.filter { it.isDigit() || it == '.' }
        val parts = cleaned.split(".")
        val fixed = if (parts.size > 1) parts[0].take(7) + "." + parts.drop(1).joinToString("").take(2) else cleaned.take(7)
        _state.update { it.copy(amountText = fixed, error = null) }
    }
    fun setQuick(dollars: Int) = setAmount(dollars.toString())
    fun setName(v: String) = _state.update { it.copy(name = v.take(60), error = null) }
    fun setPhone(v: String) = _state.update { it.copy(phone = v.filter { c -> c.isDigit() || c == '+' || c == ' ' }.take(18), error = null) }

    /** Returns true when the form is valid and the re-verify step can start. */
    fun validate(): Boolean {
        val s = _state.value
        val msg = when {
            s.name.isBlank() -> "Enter the recipient's name."
            s.phone.filter { it.isDigit() }.length < 7 -> "Enter a valid recipient phone number."
            s.amountCents <= 0 -> "Enter an amount greater than $0.00."
            else -> null
        }
        _state.update { it.copy(error = msg) }
        return msg == null
    }

    fun send() {
        val s = _state.value
        if (s.sending || s.receipt != null) return
        _state.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            val result = bank.sendTransfer(s.name.trim(), s.phone.trim(), s.amountCents)
            _state.update {
                when (result) {
                    is TransferResult.Success -> it.copy(sending = false, receipt = result.transfer)
                    TransferResult.InsufficientFunds -> it.copy(sending = false, error = "Insufficient funds. Your available balance is ${Money.format(it.availableCents)}. No money was sent.")
                    is TransferResult.OverPerTransferLimit -> it.copy(sending = false, error = "Blocked: over the ${Money.formatWhole(result.limitCents)} per-transfer limit. No money was sent.")
                    is TransferResult.OverDailyLimit -> it.copy(sending = false, error = "Blocked: this would exceed the ${Money.formatWhole(result.limitCents)} daily limit. No money was sent.")
                    TransferResult.Invalid -> it.copy(sending = false, error = "The transfer could not be completed. No money was sent; please try again.")
                }
            }
        }
    }
}
