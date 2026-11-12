package com.brendigo.bopavi


internal object WorldCoordinates {
    fun screenX(gateX:Float, origin:Double, travelled:Double):Float =
        (gateX.toDouble() + origin - travelled).toFloat()
}
