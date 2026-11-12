package com.brendigo.bopavi


internal object WorldCoordinates {
    fun screenX(gateX:Float, origin:Double, travelled:Double):Float =
        (gateX.toDouble() + origin - travelled).toFloat()
}

/** Exact circle-vs-painted-rectangle contact, shared geometrically with Swift.
 * The wide overhang exists only in the 28px/27px visible lip bands, never
 * all the way to the top or bottom of the world.
 */
