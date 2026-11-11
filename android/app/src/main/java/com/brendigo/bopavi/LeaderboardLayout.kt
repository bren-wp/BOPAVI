package com.brendigo.bopavi

/** Reference coordinates are also used for automated screenshot capture. */
internal object LeaderboardLayout {
    const val width=941f
    const val height=1672f
    const val firstRowY=627f
    const val rowStep=83f
    const val rowHeight=80f
    const val homeY=1492f
    fun supports(pxWidth:Int,pxHeight:Int,portrait:Boolean,fontScale:Float):Boolean =
        portrait && pxWidth>=860 && pxHeight>=1510 && fontScale<1.25f

    /**
     * Two score lines (19sp and 12sp) must fit the 80px reference row.
     * Otherwise use the scrolling native card layout instead of clipping.
     */
    fun supportsScoreRows(pxWidth:Int,pxHeight:Int,density:Float):Boolean {
        if(pxWidth<=0 || pxHeight<=0 || !density.isFinite() || density<=0f)
            return false
        val scale=minOf(pxWidth/width,pxHeight/height)
        return rowHeight*scale>=36f*density &&
            rowStep*scale>=rowHeight*scale
    }

    fun valid():Boolean =
        firstRowY+9f*rowStep+rowHeight<homeY && homeY+120f<=height
}
