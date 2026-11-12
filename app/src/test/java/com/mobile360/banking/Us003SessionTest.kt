// Tests: US-003 (REQ-029, REQ-030)
package com.mobile360.banking

import com.mobile360.banking.data.DemoAccounts
import com.mobile360.banking.data.SessionState
import com.mobile360.banking.ui.screens.TradeStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Us003SessionTest {
    private fun session(signedIn: Boolean, role: String) =
        SessionState(signedIn = signedIn, phone = "p", name = "Amara Obi", role = role, lastSignInAt = 0L)

    @Test
    fun test_us_003_signed_out_state_is_not_signed_in() {
        assertFalse(session(false, DemoAccounts.ROLE_CUSTOMER).signedIn)
    }

    @Test
    fun test_us_003_wealth_manager_role_is_detected() {
        assertTrue(session(true, DemoAccounts.ROLE_WEALTH_MANAGER).isWealthManager)
        assertFalse(session(true, DemoAccounts.ROLE_CUSTOMER).isWealthManager)
    }

    @Test
    fun test_us_003_first_name_is_first_word() {
        assertEquals("Amara", session(true, DemoAccounts.ROLE_CUSTOMER).firstName)
    }

    @Test
    fun test_us_003_trade_flow_has_confirm_then_verify_stage() {
        val order = TradeStage.values().toList()
        assertTrue(order.indexOf(TradeStage.Confirm) < order.indexOf(TradeStage.Verify))
    }
}
