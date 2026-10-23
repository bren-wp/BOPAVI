package com.brendigo.bopavi

/**
 * Small and deterministic presentation-only event state.
 *
 * Compare the actual lives/one-use guard from GameSimulation, not inferred
 * impact flashes or sound effects. Event instances are created only on a
 * meaningful change, never on every 60/90/120 Hz draw.
 */
internal data class FlightHudNotice(
    val label:String,
    val tint:Int,
    val startedAt:Float
)

internal class FlightHudFeedback {
    private var previousLives:Int?=null
    private var previousGuard:Boolean?=null
    private var notice:FlightHudNotice?=null

    companion object {
        const val DURATION_SECONDS=1.1f
        const val LOST_LIFE="−1 ŽIVOT"
        const val RESTORED_LIFE="+1 ŽIVOT"
        const val USED_SHIELD="ŠTIT POTROŠEN"
    }

    fun update(lives:Int,guardReady:Boolean,time:Float):FlightHudNotice? {
        if(!time.isFinite()) {
            notice=null
            return null
        }
        val beforeLives=previousLives
        val beforeGuard=previousGuard
        if(beforeLives!=null) {
            // The first observation is baseline, not a fictitious health event.
            notice=when {
                lives<beforeLives ->
                    FlightHudNotice(LOST_LIFE,0xffffbdc9.toInt(),time)
                lives>beforeLives ->
                    FlightHudNotice(RESTORED_LIFE,0xffa4f7c8.toInt(),time)
                beforeGuard==true && !guardReady ->
                    FlightHudNotice(USED_SHIELD,0xffffd99b.toInt(),time)
                else -> notice
            }
        }
        previousLives=lives
        previousGuard=guardReady
        val shown=notice ?: return null
        // Freeze naturally when the simulation pauses. Do not flash/animate.
        if(time>=shown.startedAt && time-shown.startedAt<DURATION_SECONDS)
            return shown
        notice=null
        return null
    }
}
