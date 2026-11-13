package com.mobile360.banking

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.mobile360.banking.data.Appearance
import com.mobile360.banking.ui.theme.AccentOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: CR-008 accent colour and system colours
class Us037AccentThemeTest {
    @Test
    fun appearance_defaults_keep_current_look() {
        assertEquals(0, Appearance().accent)
        assertFalse(Appearance().dynamicColor)
    }

    @Test
    fun unknown_index_falls_back_to_default() {
        assertEquals(AccentOptions.all[0], AccentOptions.at(99))
    }

    @Test
    fun every_accent_keeps_white_text_at_aa_contrast() {
        AccentOptions.all.forEach {
            val ratio = (Color.White.luminance() + 0.05f) / (it.color.luminance() + 0.05f)
            assertTrue("${it.name} contrast $ratio", ratio >= 4.5f)
        }
    }
}
