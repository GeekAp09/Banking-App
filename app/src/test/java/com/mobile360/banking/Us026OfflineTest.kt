// Tests: US-026 (REQ-032, NFR-017)
package com.mobile360.banking

import com.mobile360.banking.data.Times
import org.junit.Assert.assertEquals
import org.junit.Test

class Us026OfflineTest {
    @Test
    fun test_us_026_age_label_for_recent_data() {
        assertEquals("just now", Times.ago(System.currentTimeMillis()))
    }

    @Test
    fun test_us_026_age_label_twelve_minutes() {
        assertEquals("12 mins ago", Times.ago(System.currentTimeMillis() - 12 * 60_000L - 500L))
    }

    @Test
    fun test_us_026_age_label_hours_and_days() {
        val now = System.currentTimeMillis()
        assertEquals("2h ago", Times.ago(now - 2 * 3_600_000L - 500L))
        assertEquals("3d ago", Times.ago(now - 3 * 86_400_000L - 500L))
    }

    @Test
    fun test_us_026_future_timestamp_never_negative() {
        assertEquals("just now", Times.ago(System.currentTimeMillis() + 60_000L))
    }
}
