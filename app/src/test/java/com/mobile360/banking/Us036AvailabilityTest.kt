// Tests: US-036 (NFR-003, NFR-004)
package com.mobile360.banking

import org.junit.Ignore
import org.junit.Test

class Us036AvailabilityTest {
    @Ignore("No remote API layer or retry/backoff exists in BOLT-01 (local Room only); see developer task")
    @Test
    fun test_us_036_retries_with_backoff_and_shows_downtime_notice() {
    }
}
