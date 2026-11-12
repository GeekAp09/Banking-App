package com.mobile360.banking

import com.mobile360.banking.data.Limits
import com.mobile360.banking.data.Money
import com.mobile360.banking.data.TransferResult.OverDailyLimit
import com.mobile360.banking.data.TransferResult.OverPerTransferLimit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-017 (REQ-013)
class Us017LimitsTest {
    private val perTransfer = Limits.PER_TRANSFER_CENTS.toLong()
    private val daily = Limits.DAILY_CENTS.toLong()

    @Test
    fun test_us_017_per_transfer_limit_is_2000_dollars() {
        assertEquals(200_000L, perTransfer)
        assertEquals("\$2,000", Money.formatWhole(perTransfer))
    }

    @Test
    fun test_us_017_one_cent_over_limit_is_over_and_exact_limit_is_not() {
        assertTrue(200_001L > perTransfer)
        assertTrue(200_000L <= perTransfer)
    }

    @Test
    fun test_us_017_daily_limit_is_at_least_per_transfer_limit() {
        assertTrue(daily >= perTransfer)
    }

    @Test
    fun test_us_017_limit_results_carry_the_limit() {
        assertEquals(perTransfer, OverPerTransferLimit(perTransfer).limitCents)
        assertEquals(daily, OverDailyLimit(daily).limitCents)
    }
}
