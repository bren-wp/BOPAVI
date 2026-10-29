package com.brendigo.bopavi

import kotlin.math.sin

/**
 * Shared frontal flight rig for every pilot. Keep eyes/ears centered towards
 * the player: strong full-body pitch had made alternate faces look side-on.
 * Wings still move with the same real flap phase; collider remains unchanged.
 */
internal object PilotFlightPose {
    fun pitchDegrees(velocity:Float,reducedMotion:Boolean):Float {
        if(reducedMotion || !velocity.isFinite())return 0f
        return (velocity*.018f).coerceIn(-7f,9f)
    }

    fun wingDegrees(time:Float,flapPulse:Float,reducedMotion:Boolean):Float {
        if(reducedMotion || !time.isFinite() || !flapPulse.isFinite())return 0f
        return (sin(time*19f)*23f+
            (flapPulse/.24f).coerceIn(0f,1f)*17f).coerceIn(-23f,40f)
    }
}
