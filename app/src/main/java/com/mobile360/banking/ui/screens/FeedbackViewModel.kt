package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.FeedbackEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// CR-002
const val FEEDBACK_MAX_LENGTH = 500

sealed interface FeedbackValidation {
    data class Valid(val message: String) : FeedbackValidation
    data object Empty : FeedbackValidation
}

/** Trims the message and caps its length; blank input is rejected. */
fun validateFeedback(raw: String): FeedbackValidation {
    val text = raw.trim().take(FEEDBACK_MAX_LENGTH)
    return if (text.isEmpty()) FeedbackValidation.Empty else FeedbackValidation.Valid(text)
}

data class FeedbackUiState(
    val type: String = "Feedback",
    val message: String = "",
    val error: String? = null,
    val saved: String? = null,
    val reports: List<FeedbackEntity> = emptyList(),
)

class FeedbackViewModel(private val bank: BankRepository) : ViewModel() {
    private val _state = MutableStateFlow(FeedbackUiState())
    val state: StateFlow<FeedbackUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            bank.feedbackReports().collect { list -> _state.update { it.copy(reports = list) } }
        }
    }

    fun setType(type: String) = _state.update { it.copy(type = type) }

    fun setMessage(message: String) =
        _state.update { it.copy(message = message.take(FEEDBACK_MAX_LENGTH), error = null) }

    fun submit() {
        val current = _state.value
        when (val v = validateFeedback(current.message)) {
            FeedbackValidation.Empty ->
                _state.update { it.copy(error = "Enter a message before submitting.", saved = null) }
            is FeedbackValidation.Valid -> viewModelScope.launch {
                try {
                    bank.submitFeedback(current.type, v.message)
                    _state.update {
                        it.copy(message = "", error = null, saved = "Thanks, your report was saved on this device.")
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(error = "Could not save your report. Try again.", saved = null) }
                }
            }
        }
    }
}
