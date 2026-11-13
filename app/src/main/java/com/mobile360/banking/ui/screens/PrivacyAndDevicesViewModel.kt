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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

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

    private val _export = MutableStateFlow(ExportState())
    val exportState: StateFlow<ExportState> = _export

    // CR-014: compiles the user's records into a JSON file in [cacheDir]; the screen shares it once.
    fun exportData(cacheDir: File) {
        if (_export.value.progress != null) return
        _export.value = ExportState(progress = 0)
        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    writeDataExport(File(cacheDir, "exports"), bank) { p -> _export.value = _export.value.copy(progress = p) }
                }
                bank.audit("DATA_EXPORT", "User exported their data to a local JSON file.")
                _export.value = ExportState(ready = true, shareFile = file)
            } catch (e: Exception) {
                _export.value = ExportState(error = "Could not export your data: ${e.message ?: "request failed"}.")
            }
        }
    }

    fun exportShared() {
        _export.value = _export.value.copy(shareFile = null)
    }

    fun exportFailed(reason: String) {
        _export.value = ExportState(error = "Could not export your data: $reason.")
    }
}

// CR-014: export progress. progress is 0-100 while running, null otherwise.
data class ExportState(
    val progress: Int? = null,
    val ready: Boolean = false,
    val error: String? = null,
    val shareFile: File? = null,
)
