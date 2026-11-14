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
    @Test fun tallOwnerRowPitchAndWoodPanelMatchRealScreenshotGeometry() {
        val s=SettingsReferenceLayout
        assertEquals(0f,s.rowOffset(0,1080,1920),.01f)
        assertEquals(0f,s.panelExtra(1080,1920),.01f)
        assertTrue(s.rowOffset(0,1080,2400)>20f)
        assertTrue(s.rowOffset(8,1080,2400)>110f)
        assertTrue(s.panelExtra(1080,2400)>230f)
        assertTrue(s.rows[0]+s.rowOffset(0,1080,2400)+s.rowHeight
            <s.rows[1]+s.rowOffset(1,1080,2400))
        assertTrue(s.rows[8]+s.rowOffset(8,1080,2400)+s.rowHeight
            <s.homeTop+(s.restoreAnchor-s.contentAnchor)*418f)
        for(w to h in listOf(1080 to 2280,1080 to 2340,
                            1080 to 2400,1440 to 3040)) {
            val starts=s.rows.indices.map{idx->s.rows[idx]+s.rowOffset(idx,w,h)}
            assertTrue(starts.zipWithNext().all{it.second-it.first>=s.rowHeight})
        }
    }
    @Test fun preservedNativeVolumeGesturePolicy() {
        assertFalse(VolumeGesturePolicy.commitAsProgressChanges(false,false))
        assertFalse(VolumeGesturePolicy.commitAsProgressChanges(true,true))
        assertTrue(VolumeGesturePolicy.commitAsProgressChanges(true,false))
    }
}
