package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignInUiState(val loading: Boolean = false, val error: String? = null)

class SignInViewModel(private val bank: BankRepository, private val session: SessionRepository) : ViewModel() {
    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun proceed(onDone: () -> Unit) {
        if (_state.value.loading) return
        _state.value = SignInUiState(loading = true)
        viewModelScope.launch {
            try {
                session.beginMfa()
                _state.value = SignInUiState()
                onDone()
            } catch (e: Exception) {
                _state.value = SignInUiState(error = "Sign-in could not start. Please try again.")
            }
        }
    }

    fun pinChanged(pin: String, onDone: () -> Unit) {
        if (pin.length == 6) proceed(onDone)
    }
}
