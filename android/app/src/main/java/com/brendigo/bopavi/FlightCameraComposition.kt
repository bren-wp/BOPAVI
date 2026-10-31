package com.brendigo.bopavi

/**
 * Physical flight composition in the fixed 480x800 logical camera.
 *
 * In the owner's flight reference, Bopi's body is near 37% of the width;
 * the historical game placed him at 26%. Translate BOTH his physical body and
 * every projected obstacle/pickup by the same amount, without altering
 * simulation speed, opening geometry, reward positions, or the gap cadence.
 */
internal object FlightCameraComposition {
    const val legacyBirdX=126f
    const val birdX=175f
    const val horizontalShift=birdX-legacyBirdX

    fun gateX(worldX:Float,origin:Double,travelled:Double):Float =
        WorldCoordinates.screenX(worldX,origin,travelled)+horizontalShift
}
