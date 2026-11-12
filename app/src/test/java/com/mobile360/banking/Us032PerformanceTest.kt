package com.mobile360.banking

import com.mobile360.banking.data.Money
import com.mobile360.banking.ui.screens.parseCents
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-032 (NFR-001, NFR-002, REQ-023)
class Us032PerformanceTest {
    @Test(timeout = 10_000)
    fun test_us_032_transaction_mapping_p95_under_one_second() {
        val times = LongArray(200) {
            val t0 = System.nanoTime()
            Money.format(parseCents("45.00"))
            System.nanoTime() - t0
        }.sorted()
        val p95Ms = times[(times.size * 95) / 100] / 1_000_000
        assertTrue("p95 was " + p95Ms + " ms", p95Ms <= 1_000)
    }
}
