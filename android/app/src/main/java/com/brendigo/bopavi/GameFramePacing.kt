package com.brendigo.bopavi

/**
 * Native drawing cadence. This deliberately does NOT modify the 120 Hz
 * physics substeps or any obstacle/score calculation.
 *
 * Android's display sync should pace animation on ordinary devices. A reduced
 * motion preference or OS power saver limits redraw requests to roughly 30fps,
 * avoiding needless full-screen drawing when battery is constrained.
 *
 * Kept Android-free for deterministic JVM testing.
 */
internal object GameFramePacing {
    const val POWER_SAVER_DELAY_MS = 33L

    /** Null means schedule on the next display-synchronized animation frame. */
    fun redrawDelayMillis(reducedMotion:Boolean, powerSaveMode:Boolean):Long? =
        if(reducedMotion || powerSaveMode) POWER_SAVER_DELAY_MS else null
}
