package com.mobile360.banking

import com.mobile360.banking.ui.screens.recordPolicyAcceptance
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// CR-005: first-visit policy consent
class Us037PolicyConsentTest {
    @Test
    fun acceptSavesThenAudits() = runBlocking {
        val calls = mutableListOf<String>()
        val ok = recordPolicyAcceptance(
            save = { calls += "save" },
            audit = { type, _ -> calls += type },
        )
        assertTrue(ok)
        assertEquals(listOf("save", "POLICY_ACCEPTED"), calls)
    }

    @Test
    fun saveFailureReportsErrorAndSkipsAudit() = runBlocking {
        var audited = false
        val ok = recordPolicyAcceptance(
            save = { throw IllegalStateException("disk") },
            audit = { _, _ -> audited = true },
        )
        assertFalse(ok)
        assertFalse(audited)
    }
}
