package com.mobile360.banking

import com.mobile360.banking.data.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-016 (REQ-012)
class Us016ContactsTest {
    @Test
    fun test_us_016_seeded_app_contacts_are_daniel_priya_maria() {
        val names = SeedData.contacts().map { it.name }
        assertEquals(listOf("Daniel Okafor", "Priya Shah", "Maria Lopez"), names)
    }

    @Test
    fun test_us_016_contacts_have_phone_and_initials() {
        SeedData.contacts().forEach {
            assertTrue(it.phone.isNotBlank())
            assertTrue(it.initials.isNotBlank())
        }
    }

    @Test
    fun test_us_016_search_by_name_filters() {
        val hits = SeedData.contacts().filter { it.name.contains("pri", ignoreCase = true) }
        assertEquals("Priya Shah", hits.single().name)
    }
}
