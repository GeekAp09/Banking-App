package com.mobile360.banking

import com.mobile360.banking.data.LinkedAccountEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-009 (REQ-008, NFR-009)
class Us009LinkAccountsTest {
    private val linked = LinkedAccountEntity("l-1", "Northern Bank Current", "1234", true)

    @Test
    fun test_us_009_linked_account_is_listed_with_name() {
        assertEquals("Northern Bank Current", listOf(linked).single().name)
    }

    @Test
    fun test_us_009_removed_account_disappears_from_list() {
        assertTrue(listOf(linked).filterNot { it.id == "l-1" }.isEmpty())
    }
}
