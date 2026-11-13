package com.mobile360.banking

import com.mobile360.banking.ui.components.shouldShowScrollToTop
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// CR-006
class ScrollToTopVisibilityTest {
    @Test fun hiddenAtFirstItem() = assertFalse(shouldShowScrollToTop(0))

    @Test fun visibleOnceScrolledPastFirstItem() = assertTrue(shouldShowScrollToTop(1))
}
