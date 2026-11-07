package com.brendigo.bopavi

/**
 * The reference ready-to-fly state shows only scenery, Bopi, lives and
 * the first-tap instruction. Gameplay-only obstacles must not appear
 * before the first real flap.
 *
 * This is a presentation rule, not a physics flag: GameSimulation remains
 * authoritative and continues to compute collisions from its own state.
 */
internal object FlightHazardPresentation {
    fun obstaclesVisible(firstFlapReceived:Boolean,finished:Boolean):Boolean =
        firstFlapReceived && !finished
}
