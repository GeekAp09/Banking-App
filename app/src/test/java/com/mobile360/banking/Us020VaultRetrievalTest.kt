package com.mobile360.banking

import com.mobile360.banking.data.AuditEventEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-020 (REQ-015)
class Us020VaultRetrievalTest {
    @Test
    fun test_us_020_access_event_records_type_and_document() {
        val e = AuditEventEntity(1L, "VAULT_ACCESS", "d1", 0L)
        assertEquals("VAULT_ACCESS", e.type)
        assertTrue(e.detail.isNotBlank())
    }
}
