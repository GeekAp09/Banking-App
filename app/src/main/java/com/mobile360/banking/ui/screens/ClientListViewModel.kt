package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.ClientEntity
import com.mobile360.banking.data.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class ClientListUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val query: String = "",
    val sortDesc: Boolean = true,
    val clients: List<ClientEntity> = emptyList(),
    val allClients: List<ClientEntity> = emptyList()
) {
    val totalAumCents get() = allClients.sumOf { it.aumCents }
    val overLimit get() = allClients.filter { it.growthExposurePct > it.riskThresholdPct }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ClientListViewModel(private val bank: BankRepository, private val session: SessionRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val sortDesc = MutableStateFlow(true)

    val state: StateFlow<ClientListUiState> = combine(
        query, sortDesc, query.flatMapLatest { bank.clients(it) }, bank.clients("")
    ) { q, desc, list, all ->
        ClientListUiState(
            loading = false, query = q, sortDesc = desc,
            clients = if (desc) list.sortedByDescending { it.aumCents } else list.sortedBy { it.aumCents },
            allClients = all
        )
    }.catch { emit(ClientListUiState(loading = false, error = "Clients could not be loaded. Please try again.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClientListUiState())

    fun onQuery(q: String) { query.value = q }
    fun toggleSort() { sortDesc.value = !sortDesc.value }
}
