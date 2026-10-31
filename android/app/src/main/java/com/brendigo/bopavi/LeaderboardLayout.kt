package com.brendigo.bopavi

/** Reference coordinates are also used for automated screenshot capture. */
internal object LeaderboardLayout {
    const val width=941f
    const val height=1672f
    const val firstRowY=627f
    const val rowStep=83f
    const val rowHeight=80f
    const val homeY=1492f
    fun valid():Boolean =
        firstRowY+9f*rowStep+rowHeight<homeY && homeY+120f<=height
}
