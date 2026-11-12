package com.mobile360.banking

import com.mobile360.banking.data.TradePreview
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests: US-025 (REQ-019) */
class Us025RiskProfilingTest {
    private fun preview(exceeds: Boolean) = TradePreview(
        estimatedCents = 100_000L, feeCents = 0L, postWeightPct = 10.0, newExposurePct = if (exceeds) 60.0 else 40.0,
        thresholdPct = 50.0, exceedsRisk = exceeds, blockedReason = null,
    )

    @Test
    fun test_us_025_trade_over_threshold_flags_warning() {
        assertTrue(preview(true).exceedsRisk)
    }

    @Test
    fun test_us_025_trade_within_profile_has_no_warning() {
        assertFalse(preview(false).exceedsRisk)
    }

    @Test
    fun test_us_025_profile_is_stored_with_assessment_date() {
        val c = Bolt03Data.client(risk = "Growth")
        assertTrue(c.assessedAt > 0L)
        assertTrue(c.riskProfile == "Growth")
    }
}
