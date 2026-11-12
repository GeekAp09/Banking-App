// Tests: US-002 (REQ-002, NFR-006)
package com.mobile360.banking

import com.mobile360.banking.data.DemoAccounts
import com.mobile360.banking.data.VerifyResult
import com.mobile360.banking.ui.screens.VerifyUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Us002MfaTest {
    @Test
    fun test_us_002_demo_code_is_six_digits() {
        assertTrue(Regex("\\d{6}").matches(DemoAccounts.DEMO_CODE))
    }

    @Test
    fun test_us_002_verify_results_distinguish_wrong_locked_expired_success() {
        val results: List<VerifyResult> = listOf(
            VerifyResult.Success, VerifyResult.Wrong(2), VerifyResult.Locked(1_000L), VerifyResult.Expired
        )
        assertEquals(4, results.map { it::class }.toSet().size)
    }

    @Test
    fun test_us_002_wrong_code_reports_attempts_left() {
        assertEquals(2, (VerifyResult.Wrong(2) as VerifyResult.Wrong).attemptsLeft)
    }

    @Test
    fun test_us_002_lock_carries_unlock_time() {
        assertEquals(900_000L, VerifyResult.Locked(900_000L).untilMillis)
    }

    @Test
    fun test_us_002_verify_state_copy_keeps_error_and_clears_loading() {
        val s = VerifyUiState(secondsLeft = 300, error = null, info = null, loading = true)
        val done = s.copy(loading = false, error = "This code has expired. Request a new code.")
        assertEquals(300, done.secondsLeft)
        assertEquals("This code has expired. Request a new code.", done.error)
        assertNull(done.info)
    }
}
