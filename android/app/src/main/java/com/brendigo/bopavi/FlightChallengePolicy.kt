package com.brendigo.bopavi

import kotlin.math.max
import kotlin.math.min

/**
 * Deterministic gate difficulty. Geometry is stored in Gate so rendering and
 * swept continuous collision always use the same real obstacle edges.
 */
internal object FlightChallengePolicy {
    /** The first world is about 6% quicker, with a bounded high-tier pace. */
    fun levelSpeed(zone:Int,world:Int,epoch:Int,type:Int):Float =
        min(228f,148f+8f*zone+world*2f+(epoch%6)*1.4f+
            (if(type==2)5f else 0f))

    /** The first two pillars of each batch remain introductory. */
    fun gateGap(original:Float,index:Int,zone:Int):Float {
        if(index<2)return original
        // Alternating narrow and recovery gates: tougher but never unfair.
        // The 164px opening floor preserves a 145px hard-mode corridor.
        val variation=when(index%6) {
            0 -> 10f // tightening
            1 -> 20f // narrow
            2 -> 28f // short reaction window
            3 -> -9f // clear recovery opening
            4 -> 24f // narrow again
            else -> 4f
        }
        // Cap difficulty at the established fair-mouth floor. The same exact
        // Gate gap drives both visual lip and swept physical collision.
        return max(164f,original-variation-zone.coerceIn(0,5)*2f)
    }

    /** Real physical pillar widths, deliberately different from each other.
     *  No decoration-only obstacles: Gate.width drives rendering and collision.
     *  Intro gates preserve the old width, later columns mix slim and wide.
     */
    fun gateWidth(introWidth:Float,index:Int,ordinal:Int,world:Int):Float {
        if(index<2)return introWidth
        return when((index+ordinal+world)%6) {
            0 -> 54f
            1 -> 74f
            2 -> 86f
            3 -> 98f
            4 -> 110f
            else -> 80f
        }
    }

    /** More active motion only once the course has introduced its rhythm. */
    fun gateMovement(original:Float,index:Int,zone:Int):Float =
        if(original<=0f || index<3)original
        else min(22f,original+zone.coerceIn(0,5)*1.1f)
}
