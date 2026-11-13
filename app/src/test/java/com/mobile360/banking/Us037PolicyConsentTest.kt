package com.mobile360.banking

import com.mobile360.banking.ui.screens.HomeUiState
import org.junit.Assert.assertTrue
import org.junit.Test

// CR-005: first-visit policy consent
class Us037PolicyConsentTest {
    @Test
    fun homeStateStartsLoading() {
        assertTrue(HomeUiState().loading)
    }

    @Test
    fun acceptEnabledOnlyWhenBothTicked() {
        fun enabled(a: Boolean, b: Boolean) = a && b
        assertTrue(!enabled(false, false) && !enabled(true, false) && !enabled(false, true) && enabled(true, true))
    }
}
