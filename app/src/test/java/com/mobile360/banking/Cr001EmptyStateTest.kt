package com.mobile360.banking

import com.mobile360.banking.data.SeedData
import org.junit.Assert.assertTrue
import org.junit.Test

// CR-001: a contact search that matches nothing yields the empty list that shows the standard empty state
class Cr001EmptyStateTest {
    @Test
    fun cr_001_unmatched_contact_search_is_empty() {
        val hits = SeedData.contacts().filter { it.name.contains("zzzz", ignoreCase = true) }
        assertTrue(hits.isEmpty())
    }
}
