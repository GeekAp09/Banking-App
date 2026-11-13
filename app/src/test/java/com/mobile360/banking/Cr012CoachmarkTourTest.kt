package com.mobile360.banking

import com.mobile360.banking.ui.components.TourSteps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// CR-012: coachmark tour step model
class Cr012CoachmarkTourTest {
    @Test
    fun stepsFollowTabOrder() {
        assertEquals(listOf("Home", "Money", "Pay", "Coach", "More"), TourSteps.all.map { it.tab })
        assertEquals(5, TourSteps.count)
    }

    @Test
    fun nextAdvancesUntilLastThenFinishes() {
        assertEquals(1, TourSteps.next(0))
        assertEquals(4, TourSteps.next(3))
        assertTrue(TourSteps.isLast(4))
        assertNull(TourSteps.next(4))
    }
}
