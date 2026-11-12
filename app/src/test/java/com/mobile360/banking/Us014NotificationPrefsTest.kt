package com.mobile360.banking

import com.mobile360.banking.data.AppPrefs
import com.mobile360.banking.data.PrefFlag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests: US-014 (REQ-028) */
class Us014NotificationPrefsTest {
    private fun prefs(t: Boolean, f: Boolean, r: Boolean) = AppPrefs(
        easyMode = false, roundUps = false, transactionAlerts = t, fraudAlerts = f,
        roundAlerts = r, closureStatus = "NONE",
    )

    @Test
    fun test_us_014_each_alert_type_has_its_own_flag() {
        val keys = listOf(PrefFlag.TRANSACTION_ALERTS, PrefFlag.FRAUD_ALERTS, PrefFlag.ROUND_ALERTS).map { it.key }
        assertEquals(3, keys.toSet().size)
    }

    @Test
    fun test_us_014_toggling_one_type_leaves_others() {
        val p = prefs(t = true, f = true, r = true).copy(fraudAlerts = false)
        assertTrue(p.transactionAlerts)
        assertFalse(p.fraudAlerts)
        assertTrue(p.roundAlerts)
    }
}
