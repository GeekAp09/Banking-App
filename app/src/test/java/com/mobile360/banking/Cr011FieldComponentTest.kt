package com.mobile360.banking

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.mobile360.banking.ui.components.eyeToggleDescription
import com.mobile360.banking.ui.components.fieldTransformation
import com.mobile360.banking.ui.components.validatedFieldMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

// CR-011: ValidatedTextField rules
class Cr011FieldComponentTest {
    @Test
    fun error_wins_over_helper() {
        assertEquals("bad", validatedFieldMessage("bad", "hint"))
        assertEquals("hint", validatedFieldMessage(null, "hint"))
        assertNull(validatedFieldMessage(null, null))
    }

    @Test
    fun secret_is_masked_until_toggled() {
        assertTrue(fieldTransformation(true, false) is PasswordVisualTransformation)
        assertSame(VisualTransformation.None, fieldTransformation(true, true))
        assertSame(VisualTransformation.None, fieldTransformation(false, false))
    }

    @Test
    fun eye_toggle_description_follows_state() {
        assertEquals("Show code", eyeToggleDescription(false))
        assertEquals("Hide code", eyeToggleDescription(true))
    }
}
