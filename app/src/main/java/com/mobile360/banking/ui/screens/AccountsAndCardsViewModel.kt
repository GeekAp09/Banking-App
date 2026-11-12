package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.AccountEntity
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.CardEntity
import com.mobile360.banking.data.SessionRepository
import com.mobile360.banking.data.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AccountsUiState(
    val loaded: Boolean = false,
    val accounts: List<AccountEntity> = emptyList(),
    val cards: List<CardEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList()
)

class AccountsAndCardsViewModel(private val bank: BankRepository, private val session: SessionRepository) : ViewModel() {
    val state: StateFlow<AccountsUiState> =
        combine(bank.accounts(), bank.cards(), bank.transactions()) { a, c, t -> AccountsUiState(true, a, c, t) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountsUiState())
    val message = MutableStateFlow<String?>(null)
    fun show(text: String?) { message.value = text }
}
