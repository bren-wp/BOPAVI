package com.brendigo.bopavi

/**
 * A SeekBar's progress callbacks are also invoked by keyboard, D-pad and
 * accessibility services. Those changes have no drag-end callback.
 *
 * Persist non-touch user changes immediately; coalesce a touch drag into the
 * existing onStopTrackingTouch write. Programmatic initialization never writes.
 */
internal object VolumeGesturePolicy {
    fun commitAsProgressChanges(fromUser:Boolean,touchTracking:Boolean):Boolean =
        fromUser && !touchTracking
}
