package com.brendigo.bopavi

/** Actual owner 941×1672 composition: a hero above three real pilots. */
internal object PilotSelectionLayout {
    const val referenceWidth=941f
    const val referenceHeight=1672f
    const val bannerLeft=223f
    const val bannerTop=166f
    const val bannerWidth=495f
    const val bannerHeight=128f
    // Increase the complete winged hero silhouette toward the owner's
    // dominant central character, without moving interactive skin cards.
    const val heroLeft=100f
    const val heroTop=220f
    const val heroSize=740f
    const val platformLeft=166f
    const val platformTop=811f
    const val platformWidth=612f
    const val platformHeight=129f
    val cardLefts=floatArrayOf(34f,339f,644f)
    const val cardTop=974f
    const val cardWidth=263f
    const val cardHeight=324f
    const val activeSelectionTop=1315f
    fun fitsReference():Boolean=cardLefts.size==3 &&
        cardLefts.all{it>=0f && it+cardWidth<=referenceWidth} &&
        bannerLeft>=0f && bannerLeft+bannerWidth<=referenceWidth &&
        heroLeft>=0f && heroLeft+heroSize<=referenceWidth &&
        heroTop+heroSize<cardTop &&
        platformTop+platformHeight<cardTop &&
        cardTop+cardHeight<activeSelectionTop &&
        activeSelectionTop+150f<=referenceHeight
}
