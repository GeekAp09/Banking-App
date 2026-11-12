package com.mobile360.banking

import com.mobile360.banking.data.HoldingEntity
import com.mobile360.banking.data.Money
import org.junit.Assert.assertEquals
import org.junit.Test

// Tests: US-007 (REQ-005)
class Us007PortfolioTest {
    private val holdings = listOf(
        HoldingEntity("h1", "Global Index Fund", "Fund", 1_440_000L),
        HoldingEntity("h2", "Apple Inc.", "Shares", 960_000L),
        HoldingEntity("h3", "Treasury Bond 2030", "Bond", 480_000L),
    )

    @Test
    fun test_us_007_holdings_list_values_formatted() {
        assertEquals("\$14,400", Money.formatWhole(holdings[0].valueCents))
        assertEquals("\$9,600", Money.formatWhole(holdings[1].valueCents))
        assertEquals("\$4,800", Money.formatWhole(holdings[2].valueCents))
    }

    @Test
    fun test_us_007_total_value_is_sum_of_holdings() {
        assertEquals(2_880_000L, holdings.sumOf { it.valueCents })
    }

    @Test
    fun test_us_007_empty_portfolio_has_zero_total() {
        assertEquals(0L, emptyList<HoldingEntity>().sumOf { it.valueCents })
    }
}
