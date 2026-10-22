package com.brendigo.bopavi

/**
 * Logical 480x800 HUD geometry. The Canvas view scales these fixed anchors
 * using the same safe-inset transform as the real native Pause button.
 *
 * No allocations happen in the frame loop: the boxes and heart centres are
 * built once, and they never affect physical pillar or bird collisions.
 */
internal data class FlightHudBox(
    val left:Float, val top:Float, val right:Float, val bottom:Float
) {
    fun intersects(other:FlightHudBox):Boolean =
        left<other.right && right>other.left && top<other.bottom && bottom>other.top

    fun encloses(x:Float,y:Float):Boolean =
        x>=left && x<=right && y>=top && y<=bottom
}

internal object FlightHudLayout {
    val score = FlightHudBox(18f,16f,165f,68f)
    val food = FlightHudBox(172f,16f,297f,68f)
    val lives = FlightHudBox(304f,16f,462f,68f)
    // Power info is intentionally below the score and fruit, never beneath
    // the native Pause control on the right.
    val ability = FlightHudBox(18f,75f,310f,119f)
    val heartCenters = floatArrayOf(326f,370f,414f)
    const val heartHalfWidth = 12f
    const val heartBaseline = 54f
    const val pauseLaneStart = 366f

    fun movingWarningVisible(movement:Float,x:Float,birdX:Float):Boolean =
        movement>0f && x>=birdX+48f && x<=480f
}
