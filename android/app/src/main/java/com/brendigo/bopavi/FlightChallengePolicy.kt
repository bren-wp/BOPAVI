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
    fun gateGap(original:Float,index:Int,zone:Int):Float =
        if(index<2)original else max(164f,original-4f-1.5f*zone.coerceIn(0,5))

    /** More active motion only once the course has introduced its rhythm. */
    fun gateMovement(original:Float,index:Int,zone:Int):Float =
        if(original<=0f || index<3)original
        else min(22f,original+zone.coerceIn(0,5)*1.1f)
}
