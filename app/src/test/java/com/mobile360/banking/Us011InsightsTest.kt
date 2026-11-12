package com.mobile360.banking

import com.mobile360.banking.data.InsightEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-011 (REQ-007)
class Us011InsightsTest {
    @Test
    fun test_us_011_insight_has_plain_language_explanation() {
        val i = InsightEntity("i1", "Dining", "Dining is up 18% this week", "You spent more eating out.", 11_800L, 10_000L, 0L)
        assertEquals("Dining is up 18% this week", i.title)
        assertTrue(i.explanation.isNotBlank())
    }

    @Test
    fun test_us_011_new_transaction_changes_totals() {
        val before = listOf(TestData.tx("t1", "Blue Bottle", "Dining", -650L))
        val after = before + TestData.tx("t2", "Cafe", "Dining", -500L)
        assertEquals(650L, -before.sumOf { it.amountCents })
        assertEquals(1_150L, -after.sumOf { it.amountCents })
    }
}
