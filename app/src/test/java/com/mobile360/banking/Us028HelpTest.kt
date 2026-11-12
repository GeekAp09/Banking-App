package com.mobile360.banking

import com.mobile360.banking.data.FaqEntity
import com.mobile360.banking.ui.screens.HelpState
import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests: US-028 (REQ-035) */
class Us028HelpTest {
    private val faqs = listOf(
        FaqEntity(id = "f1", group = "Security", section = "PIN", question = "How do I reset my PIN?", answer = "In settings."),
        FaqEntity(id = "f2", group = "Security", section = "Data", question = "Is my data safe?", answer = "Yes."),
    )

    @Test
    fun test_us_028_help_state_lists_faqs() {
        val s = HelpState(loading = false, faqs = faqs, error = null)
        assertEquals(2, s.faqs.size)
    }

    @Test
    fun test_us_028_help_error_state_keeps_message() {
        val s = HelpState(loading = false, faqs = emptyList(), error = "Help could not be loaded.")
        assertEquals("Help could not be loaded.", s.error)
    }
}
