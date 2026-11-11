package com.brendigo.bopavi

/** Actual owner 941×1672 composition: a hero above four genuine pilots. */
internal object PilotSelectionLayout {
    const val referenceWidth=941f
    const val referenceHeight=1672f
    const val bannerLeft=223f
    const val bannerTop=166f
    const val bannerWidth=495f
    const val bannerHeight=128f
    // Increase the complete winged hero silhouette toward the owner's
    // dominant central character, without moving interactive skin cards.
    // The full-size transparency/wing layers add substantial silhouettes.
    // Refine the real hero toward the owner's centred character without
    // overlapping the native left/right navigation or skin cards.
    // The previous 760px actor obscured the carousel arrows and stone rim.
    // Owner reference (941x1672): wingtip-to-wingtip character lives roughly
    // between x=165..735, y=355..790, above the stone dais at y=811.
    const val heroLeft=127f
    const val heroTop=244f
    const val heroSize=688f
    const val platformLeft=166f
    const val platformTop=811f
    const val platformWidth=612f
    const val platformHeight=129f
    val cardLefts=floatArrayOf(23f,252f,481f,710f)
    const val cardTop=973f
    const val cardWidth=207f
    const val cardHeight=307f
    const val activeSelectionTop=1299f
    fun fitsReference():Boolean=cardLefts.size==4 &&
        cardLefts.all{it>=0f && it+cardWidth<=referenceWidth} &&
        (0 until cardLefts.lastIndex).all{index->
            cardLefts[index]+cardWidth+10f<=cardLefts[index+1]
        } &&
        bannerLeft>=0f && bannerLeft+bannerWidth<=referenceWidth &&
        heroLeft>=0f && heroLeft+heroSize<=referenceWidth &&
        heroTop+heroSize<cardTop &&
        platformTop+platformHeight<cardTop &&
        cardTop+cardHeight<activeSelectionTop &&
        activeSelectionTop+193f<=referenceHeight
}
