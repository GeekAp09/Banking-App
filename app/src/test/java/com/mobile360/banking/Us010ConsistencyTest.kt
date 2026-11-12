// Tests: US-010 (REQ-024)
package com.mobile360.banking

import com.mobile360.banking.data.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Us010ConsistencyTest {
    @Test
    fun test_us_010_money_formats_cents_exactly() {
        assertEquals("$12,480.25", Money.format(1_248_025L))
    }

    @Test
    fun test_us_010_money_keeps_sub_dollar_cents() {
        assertEquals("$0.05", Money.format(5L))
    }

    @Test
    fun test_us_010_card_without_discrepancy() {
        assertFalse(TestData.card(balance = 100_000, bankRecord = 100_000).hasDiscrepancy)
    }

    @Test
    fun test_us_010_card_with_differing_sources_is_flagged() {
        assertTrue(TestData.card(balance = 100_000, bankRecord = 99_000).hasDiscrepancy)
    }

    @Test
    fun test_us_010_card_masks_number_to_last4() {
        assertTrue(TestData.card().maskedNumber.endsWith("4821"))
    }
}
