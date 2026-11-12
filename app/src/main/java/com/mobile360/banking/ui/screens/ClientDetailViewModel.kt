package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.ClientEntity
import com.mobile360.banking.data.ClientHoldingEntity
import com.mobile360.banking.data.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val ClientRiskProfiles = listOf("Conservative", "Balanced", "Growth")

data class ClientDetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val client: ClientEntity? = null,
    val holdings: List<ClientHoldingEntity> = emptyList(),
    val risk: String = "",
    val note: String = "",
    val noteError: String? = null,
    val saved: String? = null
)

private data class Draft(val risk: String? = null, val note: String? = null, val noteError: String? = null, val saved: String? = null)

@OptIn(ExperimentalCoroutinesApi::class)
class ClientDetailViewModel(private val bank: BankRepository, private val session: SessionRepository) : ViewModel() {
    private val id = MutableStateFlow("")
    private val draft = MutableStateFlow(Draft())

    val state: StateFlow<ClientDetailUiState> = combine(
        id.flatMapLatest { if (it.isEmpty()) kotlinx.coroutines.flow.flowOf(null) else bank.client(it) },
        id.flatMapLatest { bank.clientHoldings(it) },
        draft, id
    ) { c, h, d, i ->
        ClientDetailUiState(
            loading = i.isEmpty(), client = c, holdings = h.sortedByDescending { it.allocationPct },
            risk = d.risk ?: c?.riskProfile.orEmpty(), note = d.note ?: c?.adviceNote.orEmpty(),
            noteError = d.noteError, saved = d.saved
        )
    }.catch { emit(ClientDetailUiState(loading = false, error = "Client details could not be loaded.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClientDetailUiState())

    fun load(clientId: String) { if (id.value != clientId) { id.value = clientId; draft.value = Draft() } }
    fun selectRisk(r: String) = draft.update { it.copy(risk = r, saved = null) }
    fun editNote(n: String) = draft.update { it.copy(note = n, noteError = null, saved = null) }

    fun save() {
        val s = state.value
        if (s.client == null) return
        if (s.note.isBlank()) { draft.update { it.copy(noteError = "Write an advice note before saving.") }; return }
        viewModelScope.launch {
            bank.saveAdvice(s.client.id, s.risk.ifBlank { s.client.riskProfile }, s.note.trim())
            draft.value = Draft(saved = "Advice and risk profile saved and logged to the audit trail.")
        }
    }
}
