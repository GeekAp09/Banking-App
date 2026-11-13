package com.mobile360.banking

import com.mobile360.banking.ui.screens.shouldShowWhatsNew
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// CR-013: What's New is shown once per installed version code
class Us037WhatsNewTest {
    @Test fun freshInstallShows() = assertTrue(shouldShowWhatsNew(0, 5))
    @Test fun olderStoredShows() = assertTrue(shouldShowWhatsNew(4, 5))
    @Test fun equalStoredHides() = assertFalse(shouldShowWhatsNew(5, 5))
    @Test fun afterDismissHides() = assertFalse(shouldShowWhatsNew(5, 5))
    @Test fun unknownVersionHides() = assertFalse(shouldShowWhatsNew(0, 0))
}
