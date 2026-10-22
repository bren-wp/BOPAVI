package com.brendigo.bopavi

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeGesturePolicyTest {
    @Test fun keyboardAndAccessibilityAdjustmentsPersistWithoutTouchRelease() {
        assertTrue(VolumeGesturePolicy.commitAsProgressChanges(
            fromUser=true,touchTracking=false))
    }

    @Test fun dragEventsAreCoalescedUntilTrackingStops() {
        assertFalse(VolumeGesturePolicy.commitAsProgressChanges(
            fromUser=true,touchTracking=true))
    }

    @Test fun initialSeekBarBindingNeverWritesUserPreferences() {
        assertFalse(VolumeGesturePolicy.commitAsProgressChanges(
            fromUser=false,touchTracking=false))
        assertFalse(VolumeGesturePolicy.commitAsProgressChanges(
            fromUser=false,touchTracking=true))
    }
}
