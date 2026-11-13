package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.AppPrefs
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.PrefFlag
import com.mobile360.banking.data.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MoreState(
    val loading: Boolean = true,
    val prefs: AppPrefs = AppPrefs(),
    val activeDevices: Int = 0,
    val error: String? = null
)

class MoreAndSettingsViewModel(
    private val bank: BankRepository,
    private val session: SessionRepository
) : ViewModel() {
    val state: StateFlow<MoreState> = combine(session.prefs, bank.devices()) { prefs, devices ->
        MoreState(loading = false, prefs = prefs, activeDevices = devices.count { !it.blocked })
    }.catch { emit(MoreState(loading = false, error = "Settings could not be loaded.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MoreState())

    fun setFlag(flag: PrefFlag, value: Boolean) {
        viewModelScope.launch { session.setFlag(flag, value) }
    }

    fun signOut(done: () -> Unit) {
        viewModelScope.launch {
            session.signOut()
            done()
        }
    }

    // CR-008: appearance choices
    val appearance = session.appearance

    fun setAccent(index: Int) {
        viewModelScope.launch { session.setAccent(index) }
    }

    fun setDynamicColor(on: Boolean) {
        viewModelScope.launch { session.setDynamicColor(on) }
    }
}
