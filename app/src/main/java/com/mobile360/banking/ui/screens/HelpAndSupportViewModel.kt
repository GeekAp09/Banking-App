package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.FaqEntity
import com.mobile360.banking.data.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HelpState(
    val loading: Boolean = true,
    val faqs: List<FaqEntity> = emptyList(),
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HelpAndSupportViewModel(
    private val bank: BankRepository,
    @Suppress("unused") private val session: SessionRepository
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query
    private val retryTick = MutableStateFlow(0)

    val state: StateFlow<HelpState> = combine(_query, retryTick) { q, _ -> q }
        .flatMapLatest { q ->
            bank.faqs(q).map { HelpState(loading = false, faqs = it) }
                .catch { emit(HelpState(loading = false, error = "Help articles could not be loaded.")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HelpState())

    fun setQuery(q: String) { _query.value = q }
    fun retry() { retryTick.value += 1 }
}
