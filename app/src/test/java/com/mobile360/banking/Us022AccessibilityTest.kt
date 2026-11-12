// Tests: US-022 (NFR-013)
package com.mobile360.banking

import androidx.compose.ui.graphics.Color
import com.mobile360.banking.ui.theme.AppColors
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class Us022AccessibilityTest {
    private fun lin(c: Float): Double =
        if (c <= 0.03928f) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

    private fun lum(c: Color): Double = 0.2126 * lin(c.red) + 0.7152 * lin(c.green) + 0.0722 * lin(c.blue)

    private fun contrast(a: Color, b: Color): Double {
        val l1 = maxOf(lum(a), lum(b))
        val l2 = minOf(lum(a), lum(b))
        return (l1 + 0.05) / (l2 + 0.05)
    }

    @Test
    fun test_us_022_primary_button_text_meets_aa_contrast() {
        assertTrue(contrast(AppColors.primary, AppColors.onPrimary) >= 4.5)
    }

    @Test
    fun test_us_022_error_text_meets_aa_contrast_on_error_container() {
        assertTrue(contrast(AppColors.onErrorContainer, AppColors.errorContainer) >= 4.5)
    }
}
