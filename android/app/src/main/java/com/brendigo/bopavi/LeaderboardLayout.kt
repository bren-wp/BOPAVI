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

    fun valid():Boolean =
        firstRowY+9f*rowStep+rowHeight<homeY && homeY+120f<=height
}
