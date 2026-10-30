package com.brendigo.bopavi

/** Stable 480×800 flight-camera HUD: only the real lives, at top left. */
internal data class FlightHudBox(val left:Float,val top:Float,val right:Float,val bottom:Float) {
    fun encloses(x:Float,y:Float):Boolean =
        x>=left && x<=right && y>=top && y<=bottom
}

internal object FlightHudLayout {
    // Top-left avatar and three hearts mirror the approved gameplay board.
    // No scoreboard, currency, abilities or on-screen pause hit target.
    val lives=FlightHudBox(12f,16f,244f,84f)
    val heartCenters=floatArrayOf(116f,169f,222f)
    const val heartHalfWidth=12f
    const val heartBaseline=52f
    const val avatarX=56f
    const val avatarY=50f
    const val avatarRadius=27f

    // Warnings belong inside the physical obstacle, never in the HUD.
    fun movingWarningVisible(movement:Float,x:Float,birdX:Float):Boolean =
        movement>0f && x>=birdX+48f && x<=480f
}
