package com.brendigo.bopavi

/**
 * No reliable fractional completion is known during level generation.
 * Display the owner's wide cyan indeterminate sweep until actual preparation
 * finishes. Its width and slow glimmer have no relation to a percentage.
 */
internal object FlightLoadingTrack {
    data class Segment(val offset:Float,val width:Float)
    private const val SWEEP_MS=1550L

    fun segment(availableWidth:Float,elapsedMs:Long,ready:Boolean):Segment {
        val width=availableWidth.coerceAtLeast(0f)
        if(ready)return Segment(0f,width)
        // A broad, luminous sweep resembles the source's long cyan bar.
        // This is STILL INDETERMINATE: its moving position deliberately
        // does not correlate with stream-generation progress or percent.
        val cursor=width*.70f
        val phase=(elapsedMs%SWEEP_MS).toFloat()/SWEEP_MS
        // Owner's source has a broad cyan sweep rooted at the left edge.
        // A small repeating glimmer is visibly indeterminate without a
        // misleading jump to the far-right side at screenshot time.
        val travel=(width-cursor).coerceAtMost(width*.045f)
        val position=(.5f-.5f*kotlin.math.cos(
            phase*2f*kotlin.math.PI.toFloat()))*travel
        return Segment(position,cursor)
    }
}
