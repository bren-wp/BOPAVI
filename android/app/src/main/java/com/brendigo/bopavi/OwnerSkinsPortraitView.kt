package com.brendigo.bopavi

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View

/**
 * Portrait artwork cropped exclusively from the four user-supplied Skins
 * reference portraits (not a screenshot of an interactive menu).
 *
 * 4 atlas cells, each 76x77 px, in a local no-DPI WebP. Only the artwork
 * region is drawn. Native LinearLayout cards continue to supply the real
 * selected state, accessible labels, transactions and touch targets.
 *
 * Static selection/check/lock decorations in the source artwork are masked
 * from the top-right corners so they never lie about a different save state.
 * The actual native checkmark/lock indicator is drawn by the parent screen.
 */
internal class OwnerSkinsPortraitView(
    context:Context,
    private val skinIndex:Int,
    private val fallbackDrawable:Int
):View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val atlas=BitmapFactory.decodeResource(
        resources,R.drawable.bopavi_owner_skins_atlas)
    private val src=Rect()
    private val dst=RectF()

    init {
        importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable=false
        isFocusable=false
    }

    override fun onDraw(c:Canvas){
        super.onDraw(c)
        val bitmap=atlas
        if(bitmap==null || bitmap.width<4 || bitmap.height<1) {
            context.getDrawable(fallbackDrawable)?.apply {
                setBounds(0,0,width,height)
                draw(c)
            }
            return
        }
        val cellWidth=bitmap.width/4
        val id=skinIndex.coerceIn(0,3)
        src.set(id*cellWidth,0,(id+1)*cellWidth,bitmap.height)
        dst.set(0f,0f,width.toFloat(),height.toFloat())
        val saved=c.save()
        if(id==0 || id==3) {
            // First portrait's pre-existing check and fourth portrait's
            // lock are NOT authoritative. Real live status overlays follow.
            val start=if(id==0) .71f else .63f
            c.clipOutRect(width*start,0f,width.toFloat(),height*.29f)
        }
        c.drawBitmap(bitmap,src,dst,paint)
        c.restoreToCount(saved)
    }
}
