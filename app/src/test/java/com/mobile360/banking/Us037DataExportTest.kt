package com.mobile360.banking

import com.mobile360.banking.ui.screens.exportToJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// CR-014: data export serialiser
class Us037DataExportTest {
    private val sections = linkedMapOf<String, List<Map<String, Any?>>>(
        "accounts" to listOf(mapOf("id" to "a1", "name" to "Main \"Joint\"", "balanceCents" to 1200L)),
        "transactions" to emptyList(),
        "consents" to listOf(mapOf("id" to "c1", "granted" to true)),
    )

    @Test
    fun json_includes_every_section() {
        val json = exportToJson(sections)
        sections.keys.forEach { assertTrue(json.contains("\"$it\":")) }
        assertTrue(json.contains("\"balanceCents\":1200"))
        assertTrue(json.contains("\"granted\":true"))
    }

    @Test
    fun json_escapes_quotes_and_omits_sensitive_keys() {
        val json = exportToJson(sections)
        assertTrue(json.contains("Main \\\"Joint\\\""))
        assertFalse(json.contains("code"))
        assertEquals('{', json.first())
        assertEquals('}', json.last())
    }
}
