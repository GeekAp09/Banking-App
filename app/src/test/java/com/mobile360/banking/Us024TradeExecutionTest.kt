package com.mobile360.banking

import com.mobile360.banking.data.TradeResult
import com.mobile360.banking.ui.screens.TradeStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests: US-024 (REQ-018, REQ-030) */
class Us024TradeExecutionTest {
    @Test
    fun test_us_024_stages_confirm_before_verify() {
        val order = TradeStage.values().toList()
        assertTrue(order.indexOf(TradeStage.Confirm) < order.indexOf(TradeStage.Verify))
        assertEquals(TradeStage.None, order.first())
    }

    @Test
    fun test_us_024_result_types_cover_reverification_block_and_invalid() {
        val results: List<TradeResult> = listOf(
            TradeResult.NeedsReverification, TradeResult.Invalid, TradeResult.Blocked("client has not authorised trades"),
        )
        assertEquals(3, results.map { it::class }.toSet().size)
        assertEquals("client has not authorised trades", (results[2] as TradeResult.Blocked).reason)
    }
}
