package com.mobile360.banking

import com.mobile360.banking.ui.screens.ConfirmUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// CR-011: live inline field errors
class Cr011ValidationTest {
    @Test
    fun blank_name_has_error() {
        assertEquals("Enter the recipient's name.", ConfirmUiState(name = "").nameError)
        assertNull(ConfirmUiState(name = "Daniel").nameError)
    }

    @Test
    fun bad_phone_has_error() {
        assertEquals("Enter a valid recipient phone number.", ConfirmUiState(phone = "12").phoneError)
        assertNull(ConfirmUiState(phone = "5551234567").phoneError)
    }

    @Test
    fun zero_amount_has_error_and_valid_clears() {
        assertEquals("Enter an amount greater than \$0.00.", ConfirmUiState(amountText = "0").amountError)
        assertNull(ConfirmUiState(amountText = "50").amountError)
        assertNull(ConfirmUiState(amountText = "").amountError)
    }
}
