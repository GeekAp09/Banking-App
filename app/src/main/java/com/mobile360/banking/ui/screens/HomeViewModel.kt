package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val initials: String = "",
    val totalCents: Long = 0,
    val accountsLine: String = "",
    val coachLine: String = "",
    val notifications: List<NotificationEntity> = emptyList()
)

class HomeViewModel(private val bank: BankRepository, private val session: SessionRepository) : ViewModel() {
    private val core = combine(session.session, bank.totalWealthCents(), bank.accounts(), bank.cards()) { s, total, accts, cards ->
        Triple(s, total, Times.plural(accts.size, "account") + ", " + Times.plural(cards.size, "card"))
    }
    val state: StateFlow<HomeUiState> = combine(core, bank.insights(), bank.notifications()) { (s, total, line), ins, notes ->
        HomeUiState(
            loading = false,
            initials = s.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
            totalCents = total,
            accountsLine = line,
            coachLine = Times.plural(ins.size, "smart suggestion"),
            notifications = notes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    // CR-013: show the changelog once per installed version code
    private val currentVersion = MutableStateFlow(0L)
    private val whatsNewDismissed = MutableStateFlow(false)

    val showWhatsNew: StateFlow<Boolean> = combine(
        currentVersion, session.lastSeenVersion, whatsNewDismissed
    ) { current, stored, dismissed -> !dismissed && shouldShowWhatsNew(stored, current) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setCurrentVersion(code: Long) {
        currentVersion.value = code
    }

    fun dismissWhatsNew() {
        whatsNewDismissed.value = true
        val code = currentVersion.value
        viewModelScope.launch { runCatching { session.markVersionSeen(code) } }
    }
}

/** CR-013: true when the installed version is newer than the last one whose changelog was seen. */
fun shouldShowWhatsNew(stored: Long, current: Long): Boolean = current > 0 && stored < current
