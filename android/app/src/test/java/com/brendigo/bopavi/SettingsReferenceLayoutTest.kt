package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class SettingsReferenceLayoutTest {
    @Test fun approvedPortraitRowsDoNotOverlap() {
        assertTrue(SettingsReferenceLayout.fits())
        assertEquals(9,SettingsReferenceLayout.rows.size)
        assertEquals(941f,SettingsReferenceLayout.width,0f)
        assertEquals(1672f,SettingsReferenceLayout.height,0f)
        assertTrue(SettingsReferenceLayout.supportsNativeTouchTargets(941,1672,2f))
        assertFalse(SettingsReferenceLayout.supportsNativeTouchTargets(941,1672,3f))
        assertFalse(SettingsReferenceLayout.supportsNativeTouchTargets(720,1280,2f))
        assertFalse(SettingsReferenceLayout.supportsNativeTouchTargets(1080,1700,3.5f))
        assertTrue(SettingsReferenceLayout.supportsDenseOwnerCamera(1080,1920,2.625f))
        assertTrue(SettingsReferenceLayout.supportsDenseOwnerCamera(1080,2280,2.625f))
        assertTrue(SettingsReferenceLayout.supportsDenseOwnerCamera(1080,2340,2.625f))
        assertTrue(SettingsReferenceLayout.supportsDenseOwnerCamera(1080,2400,2.625f))
        assertTrue(SettingsReferenceLayout.supportsDenseOwnerCamera(1440,3040,3.5f))
        assertFalse(SettingsReferenceLayout.supportsDenseOwnerCamera(720,1280,2f))
        assertFalse(SettingsReferenceLayout.supportsDenseOwnerCamera(941,1672,3f))
        assertFalse(SettingsReferenceLayout.supportsDenseOwnerCamera(1080,1700,3.5f))
    }
    @Test fun preservedNativeVolumeGesturePolicy() {
        assertFalse(VolumeGesturePolicy.commitAsProgressChanges(false,false))
        assertFalse(VolumeGesturePolicy.commitAsProgressChanges(true,true))
        assertTrue(VolumeGesturePolicy.commitAsProgressChanges(true,false))
    }
}
