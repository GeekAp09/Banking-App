package com.mobile360.banking

import com.mobile360.banking.data.TransferEntity
import com.mobile360.banking.ui.screens.ConfirmUiState
import org.junit.Assert.assertEquals
import org.junit.Test
import com.mobile360.banking.ui.components.stepProgressLabel

// Tests: CR-010 step progress
class Us037StepProgressTest {
    @Test
    fun cr010_step_is_two_while_editing() {
        assertEquals(2, ConfirmUiState().step)
    }

    @Test
    fun cr010_step_is_three_once_receipt_exists() {
        val receipt = TransferEntity("t1", "Amara", "5551234567", 1000L, 0L, "a1", "DONE", 0L, "REF1")
        assertEquals(3, ConfirmUiState(receipt = receipt).step)
    }

    @Test
    fun cr010_step_progress_label_reads_step_n_of_total() {
        assertEquals("Step 1 of 3", stepProgressLabel(1, 3))
        assertEquals("Step 3 of 3", stepProgressLabel(3, 3))
    }

}
