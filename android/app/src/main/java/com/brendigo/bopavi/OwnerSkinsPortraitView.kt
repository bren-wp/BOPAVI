package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View

/**
 * Crisp native portrait art for the four real Skins cards.
 *
 * The old owner-reference atlas held just four 76x77 crops and became visibly
 * pixelated on the 207x307 reference cards. All four high-resolution 512px
 * transparent portraits already ship with the app and are mapped by the
 * existing PilotSelectionScreen. They contain NO selection, purchase or lock
 * badges: those states are still rendered by real native sibling views.
 *
 * Cropping clips only this illustration; the card's actual click target,
 * accessibility text and saved purchase behaviour stay unchanged.
 */
internal class OwnerSkinsPortraitView(
    context:Context,
    private val skinIndex:Int,
    fallbackDrawable:Int
):View(context) {
    private val portrait:Drawable? = context.getDrawable(fallbackDrawable)

    init {
        importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable=false
        isFocusable=false
        isFocusableInTouchMode=false
    }

    override fun onDraw(c:Canvas) {
        super.onDraw(c)
        if(skinIndex !in 0..3) return
        val art=portrait ?: return
        if(width<=0 || height<=0) return
        val saved=c.save()
        c.clipRect(0,0,width,height)
        // Preserve the entire winged silhouette rather than stretching an
        // 80px face crop. Apply the same scale for every hero so the gallery
        // does not jump in size when the selected pilot changes.
        val scale=1.16f
        c.scale(scale,scale,width*.5f,height*.52f)
        art.setBounds(0,0,width,height)
        art.draw(c)
        c.restoreToCount(saved)
    }
}
