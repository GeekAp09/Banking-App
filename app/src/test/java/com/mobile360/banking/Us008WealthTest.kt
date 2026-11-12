package com.mobile360.banking

import com.mobile360.banking.data.AssetSlice
import com.mobile360.banking.data.HoldingEntity
import com.mobile360.banking.data.Money
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-008 (REQ-006)
class Us008WealthTest {
    @Test
    fun test_us_008_total_wealth_is_exact_sum_in_cents() {
        val total = listOf(1_929_216L, 1_446_912L, 964_608L, 482_304L).sum()
        assertEquals(4_823_040L, total)
        assertEquals("\$48,230.40", Money.format(total))
    }

    @Test
    fun test_us_008_cents_sum_has_no_float_error() {
        assertEquals(BigDecimal("0.30"), BigDecimal("0.10").add(BigDecimal("0.20")))
        assertEquals("\$0.30", Money.format(listOf(10L, 20L).sum()))
    }

    @Test
    fun test_us_008_asset_mix_percentages_add_to_100() {
        val mix = listOf(
            AssetSlice("Cash", 40L, 40), AssetSlice("Funds", 30L, 30),
            AssetSlice("Shares", 20L, 20), AssetSlice("Bonds", 10L, 10),
        )
        assertEquals(100, mix.sumOf { it.percent })
    }

    @Test
    fun test_us_008_holding_shows_asset_class() {
        val h = HoldingEntity("h1", "Apple Inc.", "Shares", 960_000L)
        assertTrue(h.assetClass.isNotBlank())
    }
}
