package com.mobile360.banking

import com.mobile360.banking.ui.screens.PrivacyState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

// CR-007: permanent account deletion. The erase flow itself needs Room and DataStore (covered by .maestro/cr007_account_deletion.yaml).
class Cr007AccountErasureTest {
    @Test
    fun cr_007_erased_state_has_no_pending_closure() {
        val s = PrivacyState(
            loading = false, devices = emptyList(), consents = emptyList(), closureStatus = "NONE",
            loadError = null, actionError = null, retryBlockId = null, message = null
        )
        assertFalse(s.closurePending)
        assertEquals("NONE", s.closureStatus)
    }
}
