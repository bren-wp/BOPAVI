package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * Owner-reference three-dimensional skins carousel arrows. Pure background
 * drawables: the parent native Buttons own focus, accessibility and clicks.
 */
internal class SkinsCarouselArrowDrawable(private val right:Boolean):Drawable() {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val shape=Path()
    private var opacity=255
    private var filter:ColorFilter?=null
    private fun color(argb:Int) {
        p.reset();p.isAntiAlias=true;p.color=argb
        p.alpha=opacity;p.colorFilter=filter
    }
    override fun draw(c:Canvas) {
        if(bounds.isEmpty)return
        val checkpoint=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        val edge=minOf(bounds.width(),bounds.height()).toFloat()
        c.translate((bounds.width()-edge)*.5f,(bounds.height()-edge)*.5f)
        c.scale(edge/112f,edge/112f)
        color(0x9903214e.toInt());c.drawOval(4f,10f,110f,112f,p)
        color(0xff6e3a1b.toInt());c.drawCircle(56f,55f,54f,p)
        color(0xffffdf7d.toInt());c.drawCircle(56f,52f,53f,p)
        color(0xff092e77.toInt());c.drawCircle(56f,53f,48f,p)
        color(Color.WHITE)
        p.shader=LinearGradient(0f,7f,0f,102f,
            intArrayOf(0xff7ee9ff.toInt(),0xff0d9bfa.toInt(),
                0xff0a5bbd.toInt()),null,Shader.TileMode.CLAMP)
        c.drawCircle(56f,51f,44f,p);p.shader=null
        color(0x99ffffff.toInt());p.style=Paint.Style.STROKE
        p.strokeWidth=3.5f
        c.drawArc(20f,13f,92f,84f,196f,137f,false,p)
        p.style=Paint.Style.FILL
        // Source SVG-style chevron path points left for +1: reverse the sign
        // so the semantic 'right' carousel action actually points RIGHT.
        val dir=if(right)-1f else 1f
        shape.reset()
        shape.moveTo(56f+dir*17f,28f)
        shape.lineTo(56f-dir*11f,54f)
        shape.lineTo(56f+dir*17f,81f)
        shape.lineTo(56f+dir*7f,88f)
        shape.quadTo(56f+dir*1f,90f,56f-dir*4f,83f)
        shape.lineTo(56f-dir*34f,58f)
        shape.quadTo(56f-dir*39f,53f,56f-dir*34f,47f)
        shape.lineTo(56f-dir*4f,22f)
        shape.quadTo(56f+dir*1f,15f,56f+dir*7f,21f)
        shape.close()
        color(0xff0d286a.toInt())
        p.style=Paint.Style.STROKE;p.strokeWidth=9f;p.strokeJoin=Paint.Join.ROUND
        c.drawPath(shape,p)
        color(0xffffffff.toInt());p.style=Paint.Style.FILL
        c.drawPath(shape,p)
        c.restoreToCount(checkpoint)
    }
    override fun setAlpha(value:Int){opacity=value.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(value:ColorFilter?){filter=value;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}

/** Real selected-info card background; no raster text or static EQUIP button. */
/**
 * Keep selected-info 3D relief aligned with the proven existing SKY enamel.
 * A real 941x1672 comparison of the former custom panel increased the owner's
 * info-region RGB-MAE from ~34.48% to ~37.72%; the original shared surface
 * is measurably closer. Text and EQUIP stay native sibling components.
 */
internal class SkinsSelectedInfoDrawable:Drawable() {
    private val enamel=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
    private var opacity=255
    private var filter:ColorFilter?=null
    override fun draw(c:Canvas) {
        if(bounds.isEmpty)return
        enamel.bounds=bounds
        enamel.alpha=opacity
        enamel.colorFilter=filter
        enamel.draw(c)
    }
    override fun setAlpha(value:Int){opacity=value.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(value:ColorFilter?){filter=value;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}

/** Gold-rimmed green selected check, shown only for a genuinely selected pilot. */
internal class SkinsEquippedBadgeDrawable:Drawable() {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val check=Path()
    private var opacity=255
    override fun draw(c:Canvas) {
        if(bounds.isEmpty)return
        val saved=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        c.scale(bounds.width()/55f,bounds.height()/55f)
        p.reset();p.isAntiAlias=true;p.color=0xff183a20.toInt()
        c.drawCircle(27f,30f,27f,p)
        p.color=0xffffe885.toInt();c.drawCircle(27f,25f,27f,p)
        p.shader=LinearGradient(0f,5f,0f,50f,
            0xff81ff4b.toInt(),0xff19aa31.toInt(),Shader.TileMode.CLAMP)
        c.drawCircle(27f,25f,23f,p);p.shader=null
        check.reset()
        check.moveTo(12f,26f);check.lineTo(23f,37f)
        check.lineTo(43f,16f)
        p.color=0xff115627.toInt();p.style=Paint.Style.STROKE
        p.strokeCap=Paint.Cap.ROUND;p.strokeJoin=Paint.Join.ROUND
        p.strokeWidth=9f;c.drawPath(check,p)
        p.color=Color.WHITE;p.strokeWidth=5.5f;c.drawPath(check,p)
        p.style=Paint.Style.FILL
        c.restoreToCount(saved)
    }
    override fun setAlpha(value:Int){opacity=value.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(value:ColorFilter?){invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
