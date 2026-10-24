package com.brendigo.bopavi

import kotlin.math.sin

/**
 * Shared front-facing flight pose for Bopi and every alternate sprite.
 *
 * All eleven original PNG body/wing layers stay unmodified; only their
 * Canvas rotation and flap are normalized to the same forward-facing feel.
 * Rendering poses never affect the fixed 18-unit collision sphere.
 */
internal object PilotFlightPose {
    fun pitchDegrees(velocity:Float,reducedMotion:Boolean):Float {
        if(reducedMotion || !velocity.isFinite())return 0f
        // A strong 48-degree turn looked side-on on Sova/Lisac and friends.
        // Keep body/goggles forward, while still giving readable rise/fall.
        return (velocity*.045f).coerceIn(-14f,22f)
    }

    fun wingDegrees(time:Float,flapPulse:Float,reducedMotion:Boolean):Float {
        if(reducedMotion || !time.isFinite() || !flapPulse.isFinite())return 0f
        return (sin(time*19f)*23f+
            (flapPulse/.24f).coerceIn(0f,1f)*17f).coerceIn(-23f,40f)
    }
}
