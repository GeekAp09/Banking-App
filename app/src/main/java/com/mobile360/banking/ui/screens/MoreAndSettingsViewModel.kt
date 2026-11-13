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
import android.content.Context
import com.mobile360.banking.data.StorageInfo
import com.mobile360.banking.data.clearDirContents
import com.mobile360.banking.data.measureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

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

    // CR-009
    private val _storage = MutableStateFlow(StorageUi())
    val storage: StateFlow<StorageUi> = _storage

    private fun measure(context: Context): StorageInfo = measureStorage(
        context.getDatabasePath("banking.db"),
        context.cacheDir,
        listOf(context.getExternalFilesDir(null)),
    )

    fun refreshStorage(context: Context) {
        val app = context.applicationContext
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { measure(app) } }
            _storage.value = result.fold(
                { _storage.value.copy(info = it, error = null) },
                { _storage.value.copy(error = "Storage could not be measured.") },
            )
        }
    }

    fun clearCache(context: Context) {
        val app = context.applicationContext
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { runCatching { clearDirContents(app.cacheDir) }.getOrDefault(false) }
            _storage.value = _storage.value.copy(
                message = if (ok) "Cache cleared" else "Could not clear the cache.",
            )
            refreshStorage(app)
        }
    }
}

// CR-009: storage breakdown shown in Settings
data class StorageUi(
    val info: StorageInfo? = null,
    val error: String? = null,
    val message: String? = null,
)
