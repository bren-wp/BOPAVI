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
    // Give the life meter the most width: its hearts must remain readable
    // even on narrow screens. The three cards share one top/bottom baseline.
    val score = FlightHudBox(18f,16f,146f,82f)
    val food = FlightHudBox(154f,16f,276f,82f)
    val lives = FlightHudBox(284f,16f,462f,82f)
    // Power info is intentionally below the score and fruit, never beneath
    // the native Pause control on the right.
    val ability = FlightHudBox(18f,91f,340f,140f)
    // Short-lived, nonblinking in-flight feedback below the real power row.
    val notice = FlightHudBox(18f,149f,275f,184f)
    // Wider, centred hearts have a dedicated lower lane. The count sits on
    // the header lane, right-aligned with ample interior padding.
    val heartCenters = floatArrayOf(320f,373f,426f)
    const val heartHalfWidth = 12f
    const val heartBaseline = 69f
    const val lifeCountRight = 445f
    const val heartHeaderClearance = 12f
    const val pauseLaneStart = 366f
    // Power pictogram and bounded status share the lower HUD row.
    // These anchors leave room for readable text without encroaching on Pause.
    const val powerIconCenterX = 43f
    const val powerIconCenterY = 125f
    const val powerIconRadius = 11f
    const val powerTextLeft = 62f
    const val hudTextRightInset = 12f


    fun movingWarningVisible(movement:Float,x:Float,birdX:Float):Boolean =
        movement>0f && x>=birdX+48f && x<=480f
}
