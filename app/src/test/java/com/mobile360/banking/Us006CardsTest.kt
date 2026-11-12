package com.mobile360.banking

import com.mobile360.banking.data.CardEntity
import com.mobile360.banking.data.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-006 (REQ-004)
class Us006CardsTest {
    private val visa = CardEntity(
        id = "c1", name = "Visa", last4 = "4821", limitCents = 500_000L,
        balanceCents = 64_010L, bankRecordCents = 64_010L, dueAt = 0L,
    )

    @Test
    fun test_us_006_balance_and_limit_are_formatted() {
        assertEquals("\$640.10", Money.format(visa.balanceCents))
        assertEquals("\$5,000", Money.formatWhole(visa.limitCents))
    }

    @Test
    fun test_us_006_number_is_masked_to_last_four() {
        assertTrue(visa.maskedNumber.endsWith("4821"))
    }

    @Test
    fun test_us_006_masked_text_never_contains_full_pan() {
        val pan = "4111111111114821"
        assertFalse(visa.maskedNumber.contains(pan))
        assertFalse(visa.toString().contains(pan))
        assertEquals(4, visa.last4.length)
    }
}
