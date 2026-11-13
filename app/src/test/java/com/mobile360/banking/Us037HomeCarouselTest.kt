package com.mobile360.banking

import com.mobile360.banking.ui.screens.CarouselPlaceholders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// CR-003: home carousel placeholders
class Us037HomeCarouselTest {
    @Test
    fun hasAtLeastThreeLabelledItems() {
        assertTrue(CarouselPlaceholders.size >= 3)
        assertTrue(CarouselPlaceholders.all { it.first.isNotBlank() })
    }

    @Test
    fun firstItemIsImageOne() {
        assertEquals("Carousel image 1", CarouselPlaceholders.first().first)
    }
}
