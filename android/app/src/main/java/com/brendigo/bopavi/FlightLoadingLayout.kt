package com.brendigo.bopavi

/**
 * Source artwork anchor geometry for the owner's 941 x 1672 Loading reference.
 * Keep the native drawable's intrinsic aspect ratio: stretching a winged logo
 * or the 512 x 512 Bopi layers changes their silhouette and is not 1:1.
 */
internal data class LoadingArtBox(
    val left:Float,val top:Float,val right:Float,val bottom:Float
) {
    val width:Float get()=right-left
    val height:Float get()=bottom-top
    fun isInsideReference():Boolean =
        left>=0f && top>=0f && right<=941f && bottom<=1672f &&
        width>0f && height>0f
}

internal object FlightLoadingLayout {
    const val referenceWidth=941f
    const val referenceHeight=1672f
    // The source vector is now 780×297 with a TALLER wordmark, matching
    // the crown/raised enamel letter height of the owner's actual reference.
    // Rasterize the original SVG at its natural ratio; do not stretch PNGs.
    val logo=LoadingArtBox(91f,95f,850f,384f)
    // The approved sprite has painted alpha x=13..499 and y=81..430
    // in a square 512px source. The prior anchor put Bopi's first visible
    // feather at y≈476 and feet near 957: too low compared with the owner's
    // flying hero. A 670-square rig below the wordmark paints at about
    // x=127..773, y=386..843, while preserving the original sprite ratios.
    val pilot=LoadingArtBox(110f,280f,780f,950f)
    val pilotPaintedTop:Float get()=pilot.top+81f*pilot.height/512f
    // The owner's 941x1672 splash puts a broad gold/blue winged track near
    // the lower edge and the real "Loading..." caption BELOW the track.
    val track=LoadingArtBox(161f,1350f,780f,1454f)
    const val captionCenterX=470f
    const val captionBaselineY=1515f

    fun aspectError(a:LoadingArtBox,sourceWidth:Float,sourceHeight:Float):Float =
        kotlin.math.abs(a.width/a.height-sourceWidth/sourceHeight)
}
