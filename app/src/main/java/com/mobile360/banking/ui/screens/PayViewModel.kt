package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.ContactEntity
import com.mobile360.banking.data.SessionRepository
import com.mobile360.banking.data.SessionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class PayViewModel(private val bank: BankRepository, session: SessionRepository) : ViewModel() {
    val query = MutableStateFlow("")
    val sync = MutableStateFlow(true)
    val message = MutableStateFlow<String?>(null)

    /** null while loading */
    val contacts: StateFlow<List<ContactEntity>?> = combine(sync, query.flatMapLatest { bank.contacts(it) }) { on, list ->
        if (on) list else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val me: StateFlow<SessionState> = session.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState())

    fun setQuery(v: String) { query.value = v }
    fun setSync(on: Boolean) {
        sync.value = on
        message.value = if (on) null else "Contact sync stopped. Synced contacts are hidden; you can still enter a recipient manually."
    }
    fun setMessage(m: String?) { message.value = m }
}
