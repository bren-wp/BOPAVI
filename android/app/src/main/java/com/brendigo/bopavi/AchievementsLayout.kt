package com.brendigo.bopavi

internal object AchievementsLayout {
    const val width=941f
    const val height=1672f
    val cols=floatArrayOf(60f,487f)
    val rows=floatArrayOf(517f,743f,969f,1195f)
    const val cardWidth=394f
    const val cardHeight=209f
    fun supports(pxWidth:Int,pxHeight:Int,portrait:Boolean,fontScale:Float):Boolean =
        portrait && pxWidth>=860 && pxHeight>=1510 && fontScale<1.25f

    /**
     * The source card is 209 reference pixels high while its labels and
     * progress bar are measured in sp/dp. On high-density devices that can
     * truncate the goal description without any layout exception.
     * Fall back to the native scrolling gallery before that occurs.
     */
    fun supportsCardContent(pxWidth:Int,pxHeight:Int,density:Float):Boolean {
        if(pxWidth<=0 || pxHeight<=0 || !density.isFinite() || density<=0f)
            return false
        val scale=minOf(pxWidth/width,pxHeight/height)
        return cardHeight*scale>=88f*density &&
            (rows[1]-rows[0])*scale>=cardHeight*scale
    }

    fun valid()=cols.size==2 && rows.size==4 &&
        cols.all{it>=0 && it+cardWidth<=width} &&
        (0 until rows.lastIndex).all{rows[it+1]>=rows[it]+cardHeight} &&
        rows.last()+cardHeight<1481f
}
