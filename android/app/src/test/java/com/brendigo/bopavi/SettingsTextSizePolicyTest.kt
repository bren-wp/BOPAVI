package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class SettingsTextSizePolicyTest {
    @Test fun compactModeAndVersionLabelsNeverCrash() {
        assertEquals(10,SettingsTextSizePolicy.minSp(12))
        assertEquals(11,SettingsTextSizePolicy.minSp(13))
        assertEquals(13,SettingsTextSizePolicy.minSp(16))
        assertEquals(13,SettingsTextSizePolicy.minSp(57))
    }

    @Test fun everySupportedSettingsFontHasStrictlyAscendingBounds() {
        for(maximum in 2..100) {
            val minimum=SettingsTextSizePolicy.minSp(maximum)
            assertTrue("minimum must remain positive for $maximum",minimum>0)
            assertTrue("invalid Android auto-size interval $minimum..$maximum",
                minimum<maximum)
        }
    }

    @Test(expected=IllegalArgumentException::class)
    fun invalidOneSpMaximumIsRejected() {
        SettingsTextSizePolicy.minSp(1)
    }
}
