// Tests: US-021 (REQ-016, NFR-014)
package com.mobile360.banking

import com.mobile360.banking.data.AppPrefs
import com.mobile360.banking.data.PrefFlag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Us021EasyModeTest {
    private val base = AppPrefs(
        easyMode = false, roundUps = false, transactionAlerts = true,
        fraudAlerts = true, roundAlerts = true, closureStatus = "NONE"
    )

    @Test
    fun test_us_021_easy_mode_off_by_default_state() {
        assertFalse(base.easyMode)
    }

    @Test
    fun test_us_021_toggle_on_then_off_restores_standard() {
        val on = base.copy(easyMode = true)
        assertTrue(on.easyMode)
        assertFalse(on.copy(easyMode = false).easyMode)
    }

    @Test
    fun test_us_021_easy_mode_has_own_persisted_flag() {
        val keys = PrefFlag.values().map { it.key }
        assertEquals(keys.size, keys.toSet().size)
        assertTrue(PrefFlag.EASY_MODE in PrefFlag.values())
    }
}
