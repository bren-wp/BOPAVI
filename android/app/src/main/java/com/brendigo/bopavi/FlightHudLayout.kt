package com.brendigo.bopavi

/** Stable 480×800 flight-camera HUD: only the real lives, at top left. */
internal data class FlightHudBox(val left:Float,val top:Float,val right:Float,val bottom:Float) {
    fun encloses(x:Float,y:Float):Boolean =
        x>=left && x<=right && y>=top && y<=bottom
}

internal object FlightHudLayout {
    // Top-left avatar and three hearts mirror the approved gameplay board.
    // No scoreboard, currency, abilities or on-screen pause hit target.
    // Reference artwork: 941×1672, anchored across the left ~35% of the width.
    // On the 480-wide HUD canvas the badge is a 163×51 panel, not 232×68.
    val lives=FlightHudBox(18f,14f,181f,65f)
    val heartCenters=floatArrayOf(83f,119f,155f)
    const val heartHalfWidth=12f
    const val heartBaseline=43f
    const val avatarX=44f
    const val avatarY=39f
    const val avatarRadius=22f

    /** A centered selfie-camera cutout must NOT push the left-side HUD down.
     * Edge-overlapping cutouts still get their full top safe inset. */
    fun intersectsHudX(cutoutLeft:Int,cutoutRight:Int,hudLeft:Float,hudRight:Float):Boolean =
        cutoutRight>hudLeft && cutoutLeft<hudRight

    // Warnings belong inside the physical obstacle, never in the HUD.
    fun movingWarningVisible(movement:Float,x:Float,birdX:Float):Boolean =
        movement>0f && x>=birdX+48f && x<=480f
}
