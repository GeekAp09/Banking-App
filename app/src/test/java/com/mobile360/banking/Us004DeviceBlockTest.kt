package com.mobile360.banking

import com.mobile360.banking.ui.screens.PrivacyState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests: US-004 (REQ-031) */
class Us004DeviceBlockTest {
    private fun state(devices: List<com.mobile360.banking.data.DeviceEntity>, error: String? = null, retry: String? = null) =
        PrivacyState(
            loading = false, devices = devices, consents = emptyList(), closureStatus = "NONE",
            loadError = null, actionError = error, retryBlockId = retry, message = null,
        )

    @Test
    fun test_us_004_blocked_device_is_not_active_and_this_device_is_unaffected() {
        val s = state(listOf(
            Bolt03Data.device("d1", "Pixel 8", current = true),
            Bolt03Data.device("d2", "Galaxy S21", blocked = true),
        ))
        assertEquals(1, s.activeCount)
        assertTrue(s.devices.first { it.isCurrent }.blocked.not())
    }

    @Test
    fun test_us_004_failed_block_keeps_error_and_retry_target() {
        val s = state(listOf(Bolt03Data.device("d2", "Galaxy S21")), "Could not block the device: network error.", "d2")
        assertEquals("d2", s.retryBlockId)
        assertTrue(s.actionError!!.contains("network error"))
    }
}
