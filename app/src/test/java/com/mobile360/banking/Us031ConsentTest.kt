package com.mobile360.banking

import com.mobile360.banking.ui.screens.PrivacyState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** Tests: US-031 (REQ-038, NFR-009) */
class Us031ConsentTest {
    @Test
    fun test_us_031_state_lists_contacts_and_external_consents_and_withdrawn_flag() {
        val s = PrivacyState(
            loading = false, devices = emptyList(),
            consents = listOf(
                Bolt03Data.consent("cn-1", "Contacts sync", true),
                Bolt03Data.consent("cn-2", "External accounts", false),
            ),
            closureStatus = "NONE", loadError = null, actionError = null, retryBlockId = null, message = null,
        )
        assertEquals(listOf("Contacts sync", "External accounts"), s.consents.map { it.title })
        assertFalse(s.consents[1].granted)
    }
}
