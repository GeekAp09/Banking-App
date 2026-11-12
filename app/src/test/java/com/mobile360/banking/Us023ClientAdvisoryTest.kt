package com.mobile360.banking

import com.mobile360.banking.ui.screens.ClientDetailUiState
import com.mobile360.banking.ui.screens.ClientListUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests: US-023 (REQ-017) */
class Us023ClientAdvisoryTest {
    private val three = listOf(
        Bolt03Data.client("c1", "Grace Mensah", 21_000_000L),
        Bolt03Data.client("c2", "Tom Alvarez", 9_650_000L),
        Bolt03Data.client("c3", "Nina Rossi", 15_430_000L),
    )

    @Test
    fun test_us_023_list_state_shows_assigned_clients_and_total_aum() {
        val s = ClientListUiState(
            loading = false, error = null, query = "", sortDesc = true, clients = three, allClients = three,
        )
        assertEquals(3, s.clients.size)
        assertEquals(three.sumOf { it.aumCents }, s.totalAumCents)
    }

    @Test
    fun test_us_023_empty_list_state_has_no_clients() {
        val s = ClientListUiState(
            loading = false, error = null, query = "", sortDesc = true, clients = emptyList(), allClients = emptyList(),
        )
        assertTrue(s.clients.isEmpty())
    }

    @Test
    fun test_us_023_advice_note_is_kept_in_detail_state() {
        val s = ClientDetailUiState(
            loading = false, error = null, client = three[0], holdings = emptyList(),
            risk = "Balanced", note = "Rebalance bonds", noteError = null, saved = null,
        )
        assertEquals("Rebalance bonds", s.note)
    }
}
