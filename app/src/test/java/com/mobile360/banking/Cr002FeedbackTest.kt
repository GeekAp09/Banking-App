package com.mobile360.banking

import com.mobile360.banking.data.FeedbackEntity
import com.mobile360.banking.ui.screens.FEEDBACK_MAX_LENGTH
import com.mobile360.banking.ui.screens.FeedbackUiState
import com.mobile360.banking.ui.screens.FeedbackValidation
import com.mobile360.banking.ui.screens.validateFeedback
import org.junit.Assert.assertEquals
import org.junit.Test

// CR-002
class Cr002FeedbackTest {
    @Test
    fun cr_002_blank_message_is_rejected() {
        assertEquals(FeedbackValidation.Empty, validateFeedback("   "))
    }

    @Test
    fun cr_002_message_is_trimmed_and_capped() {
        assertEquals(FeedbackValidation.Valid("Add dark mode"), validateFeedback("  Add dark mode "))
        val long = validateFeedback("x".repeat(FEEDBACK_MAX_LENGTH + 50)) as FeedbackValidation.Valid
        assertEquals(FEEDBACK_MAX_LENGTH, long.message.length)
    }

    @Test
    fun cr_002_state_defaults_to_feedback_with_no_reports() {
        val s = FeedbackUiState()
        assertEquals("Feedback", s.type)
        assertEquals(emptyList<FeedbackEntity>(), s.reports)
    }
}
