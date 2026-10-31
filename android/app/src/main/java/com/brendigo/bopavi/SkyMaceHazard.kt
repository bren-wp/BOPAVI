package com.brendigo.bopavi

import kotlin.math.cos
import kotlin.math.sin

/**
 * A small, physically real swinging mace attached to selected Sky Meadows lips.
 * Both the renderer and collision use these same coordinates and the same radius.
 * No saved state, random frame updates, or new collision-only decoration.
 */
internal object SkyMaceHazard {
    const val radius = 18f

    // Keep the first four gates free of extra hazards. Alternate the two lips,
    // leaving a recoverable, clear center flight lane at every difficulty.
    fun enabled(gate: LevelEngine.Gate, index: Int): Boolean =
        gate.kind == 0 && index >= 4 && index % 3 == 1

    fun bobX(gate: LevelEngine.Gate, gateX: Float, time: Float): Float =
        gateX + gate.width * .5f + sin(time * 1.7f + gate.phase) * 12f

    fun bobY(gate: LevelEngine.Gate, opening: LevelEngine.Opening,
             time: Float, index: Int): Float {
        val extension = 26f + cos(time * 1.7f + gate.phase) * 3f
        return if (index % 2 == 0) opening.top + extension
               else opening.bottom - extension
    }

    /**
     * Two swept half-steps follow both the bird and the moving mace.
     * The 120 Hz simulation bounds curvature to a tiny subpixel deviation;
     * no discrete endpoint-only collision tunneling.
     */
    fun sweptHit(gate: LevelEngine.Gate, index: Int, birdX: Float,
                 fromBirdY: Float, toBirdY: Float, birdRadius: Float,
                 fromGateX: Float, toGateX: Float,
                 fromOpening: LevelEngine.Opening, toOpening: LevelEngine.Opening,
                 fromTime: Float, toTime: Float): Boolean {
        if (!enabled(gate, index) || !birdX.isFinite() ||
            !fromBirdY.isFinite() || !toBirdY.isFinite() ||
            !birdRadius.isFinite() || birdRadius <= 0f ||
            !fromGateX.isFinite() || !toGateX.isFinite()) return false
        val midTime = (fromTime + toTime) * .5f
        val midGateX = (fromGateX + toGateX) * .5f
        val midTop = (fromOpening.top + toOpening.top) * .5f
        val midBottom = (fromOpening.bottom + toOpening.bottom) * .5f
        if (!midTop.isFinite() || !midBottom.isFinite() ||
            midTop >= midBottom) return false
        // The interpolated lip is only used to calculate this intermediate pose;
        // avoid a LevelEngine.Opening allocation on every physics slice.
        val x0 = bobX(gate, fromGateX, fromTime) - birdX
        val x1 = bobX(gate, toGateX, toTime) - birdX
        val xm = midGateX + gate.width * .5f +
            sin(midTime * 1.7f + gate.phase) * 12f - birdX
        val extension0 = 26f + cos(fromTime * 1.7f + gate.phase) * 3f
        val extension1 = 26f + cos(toTime * 1.7f + gate.phase) * 3f
        val extensionMid = 26f + cos(midTime * 1.7f + gate.phase) * 3f
        val upper = index % 2 == 0
        val y0 = (if (upper) fromOpening.top + extension0
                  else fromOpening.bottom - extension0) - fromBirdY
        val y1 = (if (upper) toOpening.top + extension1
                  else toOpening.bottom - extension1) - toBirdY
        val ym = (if (upper) midTop + extensionMid
                  else midBottom - extensionMid) - (fromBirdY + toBirdY) * .5f
        val contact = radius + birdRadius
        return BerryCollision.touched(x0,y0,xm,ym,contact) ||
            BerryCollision.touched(xm,ym,x1,y1,contact)
    }
}
