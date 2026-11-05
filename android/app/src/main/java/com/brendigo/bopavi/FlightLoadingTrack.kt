package com.brendigo.bopavi

/**
 * No reliable fractional completion is known during level generation.
 * Draw a moving highlight until preparation is complete, not a fake 60% bar.
 */
internal object FlightLoadingTrack {
    data class Segment(val offset:Float,val width:Float)
    private const val SWEEP_MS=1550L

    fun segment(availableWidth:Float,elapsedMs:Long,ready:Boolean):Segment {
        val width=availableWidth.coerceAtLeast(0f)
        if(ready)return Segment(0f,width)
        val cursor=width*.24f
        val phase=(elapsedMs%SWEEP_MS).toFloat()/SWEEP_MS
        val position=(.5f-.5f*kotlin.math.cos(
            phase*2f*kotlin.math.PI.toFloat()))*(width-cursor)
        return Segment(position,cursor)
    }
}
