package com.mobile360.banking

import com.mobile360.banking.data.SeedData
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-018 (REQ-025, REQ-026)
class Us018PermissionsTest {
    @Test
    fun test_us_018_app_contacts_are_available_without_device_contacts() {
        assertTrue(SeedData.contacts().isNotEmpty())
    }
}
