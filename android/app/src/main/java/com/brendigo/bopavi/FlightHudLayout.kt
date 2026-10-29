package com.brendigo.bopavi

/** Stable 480x800 logical-space HUD: a single centered lives card. */
internal data class FlightHudBox(val left:Float,val top:Float,val right:Float,val bottom:Float) {
    fun intersects(other:FlightHudBox):Boolean =
        left<other.right && right>other.left && top<other.bottom && bottom>other.top
    fun encloses(x:Float,y:Float):Boolean =
        x>=left && x<=right && y>=top && y<=bottom
}

internal object FlightHudLayout {
    val lives=FlightHudBox(122f,16f,358f,92f)
    val heartCenters=floatArrayOf(178f,240f,302f)
    const val heartHalfWidth=12f
    const val heartBaseline=75f
    const val lifeCountRight=334f

    // Pillar warnings are inside the obstacle, not a separate HUD card.
    fun movingWarningVisible(movement:Float,x:Float,birdX:Float):Boolean =
        movement>0f && x>=birdX+48f && x<=480f
}
