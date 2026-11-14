package com.brendigo.bopavi

/**
 * Native owner Shop geometry. The upper merchandise stays on a centred
 * 941×1672 camera; only decorative wood/foliage uses the bottom anchor.
 */
internal object ShopBottomLayout {
    const val woodTop=1320f
    const val frameBottom=1571f
    const val bottomAnchor=1f

    /** Verifies no uncovered gap between central frame and bottom extension. */
    fun spansViewport(width:Int,height:Int):Boolean {
        if(width<=0 || height<=0)return false
        val scale=minOf(width/ShopReferenceLayout.width,
            height/ShopReferenceLayout.height)
        val extra=(height-ShopReferenceLayout.height*scale).coerceAtLeast(0f)
        val centeredEnd=frameBottom*scale+extra*.5f
        val footerStart=woodTop*scale+extra*bottomAnchor
        return footerStart<=centeredEnd+1f &&
            footerStart>=0f &&
            height-extra>=0f
    }
}
