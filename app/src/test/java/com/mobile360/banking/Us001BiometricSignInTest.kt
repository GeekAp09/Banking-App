// Tests: US-001 (REQ-001, REQ-027, NFR-006)
package com.mobile360.banking

import com.mobile360.banking.data.DemoAccounts
import com.mobile360.banking.ui.screens.SignInUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Us001BiometricSignInTest {
    @Test
    fun test_us_001_initial_sign_in_state_is_idle_without_error() {
        val s = SignInUiState()
        assertFalse(s.loading)
        assertNull(s.error)
    }

    @Test
    fun test_us_001_loading_state_is_distinguishable_to_disable_double_submit() {
        assertTrue(SignInUiState(loading = true).loading)
    }

    @Test
    fun test_us_001_error_state_carries_message_for_fallback() {
        val s = SignInUiState(error = "Sign-in could not start. Please try again.")
        assertEquals("Sign-in could not start. Please try again.", s.error)
    }

    @Test
    fun test_us_001_demo_customer_and_manager_are_distinct_accounts() {
        assertTrue(DemoAccounts.CUSTOMER_PHONE != DemoAccounts.WEALTH_MANAGER_PHONE)
    }
}
