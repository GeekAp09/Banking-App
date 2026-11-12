package com.mobile360.banking

import com.mobile360.banking.ui.screens.PrivacyState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests: US-030 (REQ-037) */
class Us030AccountClosureTest {
    private fun state(status: String) = PrivacyState(
        loading = false, devices = emptyList(), consents = emptyList(), closureStatus = status,
        loadError = null, actionError = null, retryBlockId = null, message = null,
    )

    @Test
    fun test_us_030_pending_status_after_request() {
        assertTrue(state("PENDING").closurePending)
    }

    @Test
    fun test_us_030_no_request_is_not_pending() {
        assertFalse(state("NONE").closurePending)
    }
}
