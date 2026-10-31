package com.brendigo.bopavi

/** Approved 941×1672 reference, projected into the 480×800 game camera.
 * A stable relative camera position keeps the tutorial independent of display DPI.
 */
internal object FlightReadyOverlayLayout {
    const val left=145f
    const val top=596f
    const val right=335f
    const val bottom=650f
    const val textCenterX=258f
    const val textBaselineY=633f

    fun insideCamera():Boolean =
        left>=0f && top>=0f && right<=480f && bottom<=800f &&
        left<right && top<bottom

    // The 941px source pill spans about x=283..656, y=1245..1358.
    fun sourceMappedErrorPx():Float {
        val xScale=941f/480f
        val yScale=1672f/800f
        val leftError=kotlin.math.abs(left*xScale-283f)
        val rightError=kotlin.math.abs(right*xScale-656f)
        val topError=kotlin.math.abs(top*yScale-1245f)
        val bottomError=kotlin.math.abs(bottom*yScale-1358f)
        return maxOf(leftError,rightError,topError,bottomError)
    }
}
