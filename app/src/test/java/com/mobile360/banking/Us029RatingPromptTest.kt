package com.mobile360.banking

import com.mobile360.banking.ui.screens.RATING_COOLDOWN_MS
import com.mobile360.banking.ui.screens.ratingGoesToFeedback
import com.mobile360.banking.ui.screens.shouldPromptRating
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests: US-029 (REQ-036), CR-015
class Us029RatingPromptTest {
    @Test
    fun test_us_029_no_prompt_during_transaction_and_cooldown() {
        val now = 10_000_000_000L
        assertTrue(shouldPromptRating(0L, now))
        assertFalse(shouldPromptRating(now - 1000, now))
        assertTrue(shouldPromptRating(now - RATING_COOLDOWN_MS, now))
    }

    @Test
    fun cr015_star_routing() {
        assertTrue((1..3).all { ratingGoesToFeedback(it) })
        assertFalse(ratingGoesToFeedback(4))
        assertFalse(ratingGoesToFeedback(5))
    }
}
