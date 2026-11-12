package com.mobile360.banking

import com.mobile360.banking.data.TransferResult.InsufficientFunds
import com.mobile360.banking.data.TransferResult.Invalid
import com.mobile360.banking.data.TransferResult
import com.mobile360.banking.ui.screens.parseCents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-015 (REQ-011, REQ-023, NFR-016)
class Us015QrTransferTest {
    @Test
    fun test_us_015_amount_text_parses_to_exact_cents() {
        assertEquals(4_500L, parseCents("45.00"))
        assertEquals(4_550L, parseCents("45.5"))
    }

    @Test
    fun test_us_015_zero_amount_is_rejected_by_validation() {
        assertTrue(parseCents("0") <= 0L)
        assertTrue(parseCents("") <= 0L)
    }

    @Test
    fun test_us_015_result_types_are_distinct() {
        val a: TransferResult = InsufficientFunds
        val b: TransferResult = Invalid
        assertTrue(a != b)
    }
}
