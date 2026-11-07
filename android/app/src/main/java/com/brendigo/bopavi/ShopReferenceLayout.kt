package com.brendigo.bopavi

/** Owner's 941×1672 Shop: a featured real item and a 3x2 native grid. */
internal object ShopReferenceLayout {
    const val width=941f
    const val height=1672f
    const val featureLeft=35f
    const val featureTop=562f
    const val featureWidth=870f
    const val featureHeight=307f
    // Measured ceramic card rims from the owner's 941x1672 screenshot.
    // One shared geometry contract drives both drawing and tap targets.
    val columns=floatArrayOf(52f,336f,619f)
    val rows=floatArrayOf(883f,1208f)
    const val cardWidth=270f
    const val cardHeight=312f

    fun supports(pxWidth:Int,pxHeight:Int,portrait:Boolean,fontScale:Float):Boolean =
        portrait && pxWidth>=860 && pxHeight>=1480 && fontScale<1.25f

    fun valid():Boolean=columns.size==3 && rows.size==2 &&
        columns.all{it>=0f && it+cardWidth<=width} &&
        rows.all{it>=0f && it+cardHeight<=height} &&
        featureTop+featureHeight<rows.first() &&
        rows[0]+cardHeight<rows[1] &&
        columns[0]+cardWidth<columns[1] &&
        columns[1]+cardWidth<columns[2]
}
