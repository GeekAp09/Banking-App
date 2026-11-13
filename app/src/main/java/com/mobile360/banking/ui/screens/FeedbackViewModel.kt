package com.mobile360.banking.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile360.banking.data.BankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val FEEDBACK_AUDIT_TYPE = "APP_FEEDBACK"
const val FEEDBACK_EMPTY_ERROR = "Tell us what went wrong."

data class FeedbackUiState(
    val comment: String = "",
    val error: String? = null,
    val submitted: Boolean = false,
)

// CR-015: internal feedback form for 1-3 star ratings
class FeedbackViewModel(private val bank: BankRepository) : ViewModel() {
    private val _state = MutableStateFlow(FeedbackUiState())
    val state: StateFlow<FeedbackUiState> = _state

    fun setComment(v: String) = _state.update { it.copy(comment = v, error = null) }

    fun submit(stars: Int) {
        val comment = _state.value.comment.trim()
        if (comment.isEmpty()) {
            _state.update { it.copy(error = FEEDBACK_EMPTY_ERROR) }
            return
        }
        viewModelScope.launch {
            bank.audit(FEEDBACK_AUDIT_TYPE, "$stars: $comment")
            _state.update { it.copy(submitted = true, error = null) }
        }
    }
}
