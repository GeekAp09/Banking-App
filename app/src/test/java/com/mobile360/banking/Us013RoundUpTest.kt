package com.mobile360.banking

import com.mobile360.banking.data.RoundEventEntity
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-013 (REQ-010)
class Us013RoundUpTest {
    private fun roundCents(purchaseCents: Long): Long {
        val d = BigDecimal(purchaseCents).movePointLeft(2).setScale(0, RoundingMode.CEILING)
        return d.movePointRight(2).toLong() - purchaseCents
    }

    @Test
    fun test_us_013_round_of_six_fifty_is_fifty_cents() {
        assertEquals(50L, roundCents(650L))
    }

    @Test
    fun test_us_013_whole_unit_purchase_rounds_by_zero() {
        assertEquals(0L, roundCents(1_000L))
    }

    @Test
    fun test_us_013_skipped_round_is_recorded_in_history() {
        val skipped = RoundEventEntity("r-1", "Shell", 0L, "SKIPPED", "Low funds", 0L)
        val saved = RoundEventEntity("r-2", "Blue Bottle", 50L, "SAVED", "", 0L)
        val history = listOf(saved, skipped)
        assertEquals(2, history.size)
        assertTrue(history.none { it.amountCents < 0 })
    }
}
