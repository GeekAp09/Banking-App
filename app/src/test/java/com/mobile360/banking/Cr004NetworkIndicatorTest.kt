package com.mobile360.banking

import com.mobile360.banking.ui.components.networkLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class Cr004NetworkIndicatorTest {
    // CR-004
    @Test
    fun onlineLabel() = assertEquals("Online", networkLabel(true))

    // CR-004
    @Test
    fun offlineLabel() = assertEquals("Offline", networkLabel(false))
}
