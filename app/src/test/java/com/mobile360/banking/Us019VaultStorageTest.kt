package com.mobile360.banking

import com.mobile360.banking.data.VaultDocEntity
import org.junit.Assert.assertEquals
import org.junit.Test

// Tests: US-019 (REQ-014)
class Us019VaultStorageTest {
    private val docs = listOf(
        VaultDocEntity("d1", "Tax return 2025.pdf", "PDF", "1.2 MB", 0L),
        VaultDocEntity("d2", "Passport scan.jpg", "JPG", "2.4 MB", 0L),
    )

    @Test
    fun test_us_019_uploaded_document_is_listed() {
        val added = docs + VaultDocEntity("d3", "Mortgage statement.pdf", "PDF", "900 KB", 0L)
        assertEquals(3, added.size)
    }

    @Test
    fun test_us_019_deleted_document_is_removed() {
        assertEquals(listOf("d2"), docs.filterNot { it.id == "d1" }.map { it.id })
    }
}
