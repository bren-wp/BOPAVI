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

    /**
     * The owner's original 1080×1920 WebP has centred illustrated gutters.
     * The v1.0.87 native capture occupied almost the entire physical width.
     * Measured at its wooden card frame (y=1267): owner x=101..979,
     * live x=21..1062. The bounded x-camera factor is ~878/1041.
     * Other owner viewports already use nearly edge-to-edge cards, so they
     * MUST retain their existing horizontal framing.
     *
     * No design image is resized or used as a runtime control.
     */
    const val owner16x9HorizontalFactor=0.8435f
    fun horizontalFactor(pxWidth:Int,pxHeight:Int):Float =
        if(pxWidth==1080 && pxHeight==1920)owner16x9HorizontalFactor else 1f

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
