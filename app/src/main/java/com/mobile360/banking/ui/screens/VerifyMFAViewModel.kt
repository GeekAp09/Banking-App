package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.SessionRepository
import com.mobile360.banking.data.Times
import com.mobile360.banking.data.VerifyResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VerifyUiState(
    val secondsLeft: Int = 300,
    val error: String? = null,
    val info: String? = null,
    val loading: Boolean = false
)

class VerifyMFAViewModel(private val bank: BankRepository, private val session: SessionRepository) : ViewModel() {
    private val _state = MutableStateFlow(VerifyUiState())
    val state: StateFlow<VerifyUiState> = _state.asStateFlow()
    private var issuedAt = System.currentTimeMillis()

    init {
        viewModelScope.launch {
            issuedAt = session.codeIssuedAt()
            while (true) {
                val left = (300 - (System.currentTimeMillis() - issuedAt) / 1000).toInt().coerceIn(0, 300)
                _state.update { it.copy(secondsLeft = left) }
                delay(1000)
            }
        }
    }

    fun verify(code: String, onHome: () -> Unit) {
        if (code.length != 6) {
            _state.update { it.copy(error = "Enter all 6 digits of the code.", info = null) }
            return
        }
        _state.update { it.copy(loading = true, error = null, info = null) }
        viewModelScope.launch {
            when (val r = session.verifyCode(code)) {
                VerifyResult.Success -> { _state.update { it.copy(loading = false) }; onHome() }
                is VerifyResult.Wrong -> _state.update {
                    it.copy(loading = false, error = "That code is not correct. " + Times.plural(r.attemptsLeft, "attempt") + " left.")
                }
                is VerifyResult.Locked -> _state.update {
                    it.copy(loading = false, error = "Too many wrong codes. Sign-in is locked until " + Times.dateTime(r.untilMillis) + ".")
                }
                VerifyResult.Expired -> _state.update {
                    it.copy(loading = false, error = "This code has expired. Request a new code.")
                }
            }
        }
    }

    fun resend() {
        viewModelScope.launch {
            session.resendCode()
            issuedAt = session.codeIssuedAt()
            _state.update { it.copy(error = null, info = "A new demo code is ready (local demo: use 123456). Nothing was sent.", secondsLeft = 300) }
        }
    }

    fun continueAsWealthManager(onDone: () -> Unit) {
        viewModelScope.launch {
            session.continueAsWealthManager()
            onDone()
        }
    }
}
