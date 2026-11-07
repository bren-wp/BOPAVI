package com.brendigo.bopavi

/**
 * The supplied 941×1672 "Tap to fly" gameplay reference visibly includes
 * stone obstacles, golden rings and crystals BEFORE the first flap.
 *
 * Render the SAME cached, physically real gate positions in both ready and
 * active views, without changing their geometry, pickups or collision rules.
 * GameSimulation.step() still refuses to advance or apply damage until the
 * first real flap. Finishing the game hides gameplay hazards over the result.
 */
internal object FlightHazardPresentation {
    @Suppress("UNUSED_PARAMETER")
    fun obstaclesVisible(firstFlapReceived:Boolean,finished:Boolean):Boolean =
        !finished
}
