package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.ConsentEntity
import com.mobile360.banking.data.DemoAccounts
import com.mobile360.banking.data.DeviceEntity
import com.mobile360.banking.data.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PrivacyState(
    val loading: Boolean = true,
    val devices: List<DeviceEntity> = emptyList(),
    val consents: List<ConsentEntity> = emptyList(),
    val closureStatus: String = "NONE",
    val loadError: String? = null,
    val actionError: String? = null,
    val retryBlockId: String? = null,
    val message: String? = null
) {
    val activeCount get() = devices.count { !it.blocked }
    val closurePending get() = closureStatus != "NONE"
}

class PrivacyAndDevicesViewModel(
    private val bank: BankRepository,
    private val session: SessionRepository
) : ViewModel() {
    private data class Extras(val error: String? = null, val retryId: String? = null, val message: String? = null)

    private val extras = MutableStateFlow(Extras())
    private val _closureError = MutableStateFlow<String?>(null)
    val closureError: StateFlow<String?> = _closureError

    val state: StateFlow<PrivacyState> = combine(bank.devices(), bank.consents(), session.prefs, extras) { d, c, p, e ->
        PrivacyState(
            loading = false, devices = d, consents = c, closureStatus = p.closureStatus,
            actionError = e.error, retryBlockId = e.retryId, message = e.message
        )
    }.catch { emit(PrivacyState(loading = false, loadError = "Privacy settings could not be loaded.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrivacyState())

    fun blockDevice(id: String) {
        viewModelScope.launch {
            try {
                bank.blockDevice(id)
                bank.audit("DEVICE_BLOCKED", id)
                extras.value = Extras(message = "Device blocked. Its session has ended and it cannot sign in.")
            } catch (e: Exception) {
                extras.value = Extras(error = "Could not block the device: ${e.message ?: "request failed"}.", retryId = id)
            }
        }
    }

    fun setConsent(id: String, granted: Boolean) {
        viewModelScope.launch {
            try {
                bank.setConsent(id, granted)
                bank.audit(if (granted) "CONSENT_GRANTED" else "CONSENT_WITHDRAWN", id)
                extras.value = Extras(message = if (granted) "Consent granted." else "Consent withdrawn. The related data use has stopped.")
            } catch (e: Exception) {
                extras.value = Extras(error = "Could not update the consent: ${e.message ?: "request failed"}.")
            }
        }
    }

    fun clearClosureError() { _closureError.value = null }

    fun requestClosure(code: String, onDone: () -> Unit) {
        if (code != DemoAccounts.DEMO_CODE) {
            _closureError.value = "That code is not correct. Re-verification failed."
            return
        }
        viewModelScope.launch {
            session.setClosureStatus("PENDING")
            bank.audit("ACCOUNT_CLOSURE_REQUESTED", "re-verified")
            _closureError.value = null
            extras.value = Extras(message = "Closure request submitted. Status: Pending.")
            onDone()
        }
    }

    fun dismissMessage() { extras.value = Extras() }
}
