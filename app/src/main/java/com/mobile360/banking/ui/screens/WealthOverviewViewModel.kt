package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.AssetSlice
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.HoldingEntity
import com.mobile360.banking.data.LinkedAccountEntity
import com.mobile360.banking.data.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WealthUiState(
    val loaded: Boolean = false,
    val total: Long = 0,
    val mix: List<AssetSlice> = emptyList(),
    val holdings: List<HoldingEntity> = emptyList(),
    val linked: List<LinkedAccountEntity> = emptyList()
)

class WealthOverviewViewModel(private val bank: BankRepository, private val session: SessionRepository) : ViewModel() {
    val state: StateFlow<WealthUiState> =
        combine(bank.totalWealthCents(), bank.assetMix(), bank.holdings(), bank.linkedAccounts()) { t, m, h, l ->
            WealthUiState(true, t, m, h, l)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WealthUiState())
    val message = MutableStateFlow<String?>(null)
    val error = MutableStateFlow<String?>(null)

    fun link(name: String, mask: String, consent: Boolean) {
        error.value = null; message.value = null
        when {
            !consent -> error.value = "Give your consent to share data before linking an account."
            name.isBlank() -> error.value = "Enter the name of the institution or account."
            mask.length != 4 -> error.value = "Enter the last 4 digits of the account number."
            else -> viewModelScope.launch {
                val ok = bank.linkAccount(name.trim(), "•••• $mask", true)
                if (ok) message.value = "Account linked." else error.value = "The link failed: the institution could not be verified. Try again later."
            }
        }
    }

    fun remove(id: String) {
        viewModelScope.launch { bank.removeLinkedAccount(id); message.value = "Account removed and its data is no longer used." }
    }
}
