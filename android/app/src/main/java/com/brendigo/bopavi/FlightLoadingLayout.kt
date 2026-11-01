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
    // Winged crown wordmark is proportionate to original 780 x 236 logo PNG.
    val logo=LoadingArtBox(28f,96f,913f,364f)
    // Bopi's BODY and both animated-gameplay wing layers are all square
    // 512x512 canvases. Paint them in the same rectangle, no distortion.
    val pilot=LoadingArtBox(90f,381f,850f,1141f)
    // Supplied Splash: Loading caption ABOVE the thin cyan/blue glass track.
    val track=LoadingArtBox(222f,1482f,719f,1563f)
    const val captionCenterX=470f
    const val captionBaselineY=1468f

    fun aspectError(a:LoadingArtBox,sourceWidth:Float,sourceHeight:Float):Float =
        kotlin.math.abs(a.width/a.height-sourceWidth/sourceHeight)
}
