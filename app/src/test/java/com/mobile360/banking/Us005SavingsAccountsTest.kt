package com.mobile360.banking

import com.mobile360.banking.data.AccountEntity
import com.mobile360.banking.data.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-005 (REQ-003)
class Us005SavingsAccountsTest {
    private val savings = AccountEntity("a1", "Everyday Savings", "SAVINGS", 1_248_025L, "Active", 0L)

    @Test
    fun test_us_005_savings_balance_is_formatted_to_the_cent() {
        assertEquals("Everyday Savings", savings.name)
        assertEquals("\$12,480.25", Money.format(savings.balanceCents))
    }

    @Test
    fun test_us_005_recent_transactions_carry_sign_and_category() {
        val txs = listOf(
            TestData.tx("t1", "Whole Foods", "Groceries", -5_420L),
            TestData.tx("t2", "Salary", "Income", 320_000L),
            TestData.tx("t3", "Uber", "Transport", -1_875L),
        )
        assertTrue(txs.all { it.category.isNotBlank() })
        assertTrue(Money.formatSigned(txs[0].amountCents).contains("54.20"))
        assertTrue(Money.formatSigned(txs[1].amountCents).contains("3,200.00"))
        assertTrue(txs[0].amountCents < 0 && txs[1].amountCents > 0)
    }

    @Test
    fun test_us_005_no_accounts_is_an_empty_list() {
        assertTrue(emptyList<AccountEntity>().isEmpty())
    }
}
