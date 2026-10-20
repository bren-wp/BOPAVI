package com.brendigo.bopavi

/**
 * Single monotonic physics clock for the actively flying viewport.
 *
 * The idle preview, a paused/backgrounded activity, and a QA still capture
 * must never accumulate simulated flight time. We intentionally establish
 * a fresh baseline on their first subsequent frame rather than simulating
 * a full 100 ms catch-up step (or a sudden lurch) on the first tap.
 *
 * Kept free of Android dependencies so JVM tests can exercise real time
 * boundaries without sleeps, emulator scheduling or wall-clock races.
 */
internal class FlightFrameClock {
    private var previousNanos:Long?=null

    fun reset() {
        previousNanos=null
    }

    /** Returns a valid delta only for successive *active* frames. */
    fun tick(nowNanos:Long,running:Boolean):Float? {
        if(!running) {
            reset()
            return null
        }
        val previous=previousNanos
        if(previous==null || nowNanos<previous) {
            previousNanos=nowNanos
            return null
        }
        val nanos=nowNanos-previous
        if(nanos<1_000_000L) return null
        previousNanos=nowNanos
        return nanos/1_000_000_000.0f
    }
}
