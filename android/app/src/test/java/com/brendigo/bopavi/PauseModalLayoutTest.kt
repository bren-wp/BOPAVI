package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PauseModalLayoutTest {
    @Test fun compactDeviceAndLargeTextKeepFullSizeControls() {
        assertTrue(PauseModalLayout.compact(600,1f))
        assertTrue(PauseModalLayout.compact(850,1.5f))
        assertFalse(PauseModalLayout.compact(850,1f))
        assertTrue(PauseModalLayout.buttonHeightDp(true)>=PauseModalLayout.minTouchDp)
        assertTrue(PauseModalLayout.buttonHeightDp(false)>=PauseModalLayout.minTouchDp)
    }

    @Test fun bothModalModesFitTheirOwnHeroAndButtons() {
        assertEquals(112,PauseModalLayout.heroHeightDp(true))
        assertEquals(168,PauseModalLayout.heroHeightDp(false))
        assertTrue(PauseModalLayout.headerHeightDp(true)<PauseModalLayout.headerHeightDp(false))
        assertTrue(PauseModalLayout.dialogHeightDp(true)<PauseModalLayout.dialogHeightDp(false))
    }
}
