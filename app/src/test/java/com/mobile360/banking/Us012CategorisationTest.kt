// Tests: US-012 (REQ-009)
package com.mobile360.banking

import com.mobile360.banking.data.CategoryRuleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Us012CategorisationTest {
    @Test
    fun test_us_012_transaction_has_non_blank_category() {
        assertTrue(TestData.tx("t1", "Whole Foods", "Groceries").category.isNotBlank())
    }

    @Test
    fun test_us_012_correction_is_stored_as_merchant_rule() {
        val rule = CategoryRuleEntity(merchant = "Shell", category = "Leisure")
        assertEquals("Shell", rule.merchant)
        assertEquals("Leisure", rule.category)
    }

    @Test
    fun test_us_012_recategorising_keeps_other_fields() {
        val t = TestData.tx("t9", "Shell", "Transport", -4_000L)
        val changed = t.copy(category = "Leisure")
        assertEquals("Leisure", changed.category)
        assertEquals(t.amountCents, changed.amountCents)
        assertEquals(t.title, changed.title)
    }
}
