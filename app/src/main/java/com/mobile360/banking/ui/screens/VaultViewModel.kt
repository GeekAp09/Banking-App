package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.SessionRepository
import com.mobile360.banking.data.VaultDocEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModel(private val bank: BankRepository, session: SessionRepository) : ViewModel() {
    val query = MutableStateFlow("")
    val badgeFilter = MutableStateFlow<String?>(null)
    val selectedId = MutableStateFlow<String?>(null)
    val message = MutableStateFlow<String?>(null)
    private var verifiedAt = 0L

    private val allDocs = query.flatMapLatest { bank.vaultDocs(it) }

    /** null while loading */
    val docs: StateFlow<List<VaultDocEntity>?> = combine(allDocs, badgeFilter) { d, f ->
        if (f == null) d else d.filter { it.badge == f }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val badges: StateFlow<List<String>> = allDocs.combine(query) { d, _ -> d.map { it.badge }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(v: String) { query.value = v }
    fun setFilter(b: String?) { badgeFilter.value = b }
    fun select(id: String) { selectedId.value = id }
    fun setMessage(m: String?) { message.value = m }

    fun isVerified() = System.currentTimeMillis() - verifiedAt < 5 * 60_000L
    fun markVerified() { verifiedAt = System.currentTimeMillis() }
    fun lock() { verifiedAt = 0L; message.value = "Vault locked. Verify again to open documents." }

    fun logAccess(id: String) = viewModelScope.launch { bank.logVaultAccess(id) }
    fun upload(name: String, sizeLabel: String) = viewModelScope.launch {
        bank.addVaultDoc(name, sizeLabel)
        message.value = "$name was stored encrypted in your vault."
    }
    fun delete(doc: VaultDocEntity) = viewModelScope.launch {
        bank.deleteVaultDoc(doc.id)
        if (selectedId.value == doc.id) selectedId.value = null
        message.value = "${doc.name} was deleted from your vault."
    }
}
