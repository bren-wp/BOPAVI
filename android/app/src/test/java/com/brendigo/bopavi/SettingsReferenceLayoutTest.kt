package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class SettingsReferenceLayoutTest {
    @Test fun approvedPortraitRowsDoNotOverlap() {
        assertTrue(SettingsReferenceLayout.fits())
        assertEquals(8,SettingsReferenceLayout.rows.size)
        assertEquals(941f,SettingsReferenceLayout.width,0f)
        assertEquals(1672f,SettingsReferenceLayout.height,0f)
    }
    @Test fun preservedNativeVolumeGesturePolicy() {
        assertFalse(VolumeGesturePolicy.commitAsProgressChanges(false,false))
        assertFalse(VolumeGesturePolicy.commitAsProgressChanges(true,true))
        assertTrue(VolumeGesturePolicy.commitAsProgressChanges(true,false))
    }
}
