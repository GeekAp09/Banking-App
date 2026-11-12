// Tests: US-033 (NFR-005, NFR-007)
package com.mobile360.banking

import com.mobile360.banking.data.AuditEventEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class Us033DataProtectionTest {
    @Test
    fun test_us_033_audit_event_records_type_and_detail() {
        val e = AuditEventEntity(id = 0L, type = "TRADE", detail = "BUY 10", at = 1L)
        assertEquals("TRADE", e.type)
        assertEquals("BUY 10", e.detail)
    }

    @Test
    fun test_us_033_card_number_is_masked_in_display_form() {
        val masked = TestData.card().maskedNumber
        assertEquals("4821", masked.takeLast(4))
        assertEquals(false, masked.contains("4821".repeat(2)))
    }
}
