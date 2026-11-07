package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * Native independent decorative enamel layers for the owner's Shop.
 * No screenshot, prices, currency totals, purchase controls, or static
 * product text can be embedded in these drawables. Android Views own those.
 * Coordinates match the reference 270x312 cards and 870x307 offer panel.
 */
internal class ShopCreamCardDrawable:Drawable() {
    private val ink=Paint(Paint.ANTI_ALIAS_FLAG)
    private var requestedAlpha=255
    private var filter:ColorFilter?=null

    override fun draw(canvas:Canvas) {
        if(bounds.isEmpty)return
        val checkpoint=canvas.save()
        canvas.translate(bounds.left.toFloat(),bounds.top.toFloat())
        canvas.scale(bounds.width()/270f,bounds.height()/312f)
        fun face(l:Float,t:Float,r:Float,b:Float,rad:Float,color:Int) {
            ink.reset();ink.isAntiAlias=true
            ink.color=color;ink.alpha=requestedAlpha
            ink.colorFilter=filter
            canvas.drawRoundRect(l,t,r,b,rad,rad,ink)
        }
        // Offset wood-colored extrusion, warm bottom rim and ivory relief.
        face(4f,10f,267f,309f,28f,0xff824821.toInt())
        face(2f,4f,268f,303f,26f,0xffc88745.toInt())
        face(4f,2f,266f,299f,24f,0xffffe9bc.toInt())
        ink.reset();ink.isAntiAlias=true;ink.color=Color.WHITE
        ink.alpha=requestedAlpha;ink.colorFilter=filter
        ink.shader=LinearGradient(0f,7f,0f,298f,
            intArrayOf(0xfffffff1.toInt(),0xffffedcd.toInt(),
                0xffffe4ae.toInt(),0xfff8c78b.toInt()),
            floatArrayOf(0f,.45f,.84f,1f),Shader.TileMode.CLAMP)
        canvas.drawRoundRect(8f,7f,262f,295f,21f,21f,ink)
        ink.shader=null
        // Continuous white interior highlight with natural upper glint.
        ink.style=Paint.Style.STROKE
        ink.strokeWidth=3f;ink.color=0xffffffff.toInt()
        canvas.drawRoundRect(9f,8f,261f,292f,20f,20f,ink)
        ink.style=Paint.Style.FILL
        ink.color=0x55ffffff
        canvas.drawRoundRect(20f,14f,250f,21f,4f,4f,ink)
        canvas.restoreToCount(checkpoint)
    }
    override fun setAlpha(value:Int){requestedAlpha=value.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(value:ColorFilter?){filter=value;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}

internal class ShopFeaturedPanelDrawable:Drawable() {
    private val ink=Paint(Paint.ANTI_ALIAS_FLAG)
    private val shape=Path()
    private var requestedAlpha=255
    private var filter:ColorFilter?=null

    override fun draw(canvas:Canvas) {
        if(bounds.isEmpty)return
        val checkpoint=canvas.save()
        canvas.translate(bounds.left.toFloat(),bounds.top.toFloat())
        canvas.scale(bounds.width()/870f,bounds.height()/307f)
        ink.reset();ink.isAntiAlias=true;ink.colorFilter=filter
        ink.alpha=requestedAlpha
        ink.color=0xff2b076c.toInt()
        canvas.drawRoundRect(0f,8f,870f,307f,36f,36f,ink)
        ink.color=0xffffbf69.toInt()
        canvas.drawRoundRect(2f,1f,868f,301f,32f,32f,ink)
        ink.color=0xff480a9a.toInt()
        canvas.drawRoundRect(7f,6f,863f,295f,29f,29f,ink)
        ink.color=Color.WHITE
        ink.shader=LinearGradient(0f,6f,0f,295f,
            intArrayOf(0xffbd46fb.toInt(),0xff8127df.toInt(),0xff4c13a5.toInt()),
            floatArrayOf(0f,.52f,1f),Shader.TileMode.CLAMP)
        canvas.drawRoundRect(12f,10f,858f,290f,27f,27f,ink)
        ink.shader=null
        shape.reset()
        shape.addRoundRect(12f,10f,858f,290f,27f,27f,Path.Direction.CW)
        canvas.save()
        canvas.clipPath(shape)
        ink.color=Color.WHITE
        ink.shader=RadialGradient(677f,137f,240f,
            intArrayOf(0xeeffefa4.toInt(),0x668e42ff,0x00591eaa),
            floatArrayOf(0f,.48f,1f),Shader.TileMode.CLAMP)
        canvas.drawCircle(677f,137f,239f,ink)
        ink.shader=null
        // Golden stars around the authentic featured perk image, not pickups.
        for(i in 0..9){
            val x=433f+(i*83%407)
            val y=23f+(i*61%246)
            val rad=if(i%4==0)9f else 4.5f
            ink.color=0xffffe7a8.toInt();ink.alpha=requestedAlpha
            ink.strokeWidth=2f;ink.style=Paint.Style.STROKE
            canvas.drawLine(x-rad,y,x+rad,y,ink)
            canvas.drawLine(x,y-rad,x,y+rad,ink)
        }
        ink.style=Paint.Style.FILL
        canvas.restore()
        ink.color=0x88ffecfa.toInt()
        ink.style=Paint.Style.STROKE;ink.strokeWidth=2f
        canvas.drawRoundRect(14f,12f,856f,288f,25f,25f,ink)
        ink.style=Paint.Style.FILL;ink.shader=null
        canvas.restoreToCount(checkpoint)
    }
    override fun setAlpha(value:Int){requestedAlpha=value.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(value:ColorFilter?){filter=value;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
