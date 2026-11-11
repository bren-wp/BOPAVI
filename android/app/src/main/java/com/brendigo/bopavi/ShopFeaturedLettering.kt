package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.widget.TextView
import kotlin.math.min

/**
 * Native featured product name: outlines and metallic extrusion follow the
 * reference, but the string always comes from the genuinely purchasable SKU.
 * No static "Golden Avi" offer or counterfeit gem purchase is inserted.
 */
internal class ShopFeaturedName(context:Context,label:String):TextView(context) {
    private val ink=Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign=Paint.Align.CENTER
        typeface=Typeface.create("sans-serif-condensed-black",Typeface.BOLD)
        strokeJoin=Paint.Join.ROUND
        strokeCap=Paint.Cap.ROUND
        isSubpixelText=true
    }
    init {
        text=label
        contentDescription=label
        isClickable=false
        isFocusable=false
        includeFontPadding=false
        setTextColor(Color.WHITE)
    }
    override fun onDraw(c:Canvas) {
        val label=text.toString()
        if(width<=0 || height<=0 || label.isEmpty())return
        ink.textSize=height*.89f
        ink.textSize*=min(1f,width*.91f/ink.measureText(label).coerceAtLeast(1f))
        val m=ink.fontMetrics
        val y=height*.51f-(m.ascent+m.descent)*.5f
        val x=width*.5f
        ink.shader=null;ink.style=Paint.Style.STROKE
        ink.strokeWidth=ink.textSize*.20f
        ink.color=0xff4a163e.toInt()
        c.drawText(label,x,y+ink.textSize*.12f,ink)
        ink.strokeWidth=ink.textSize*.14f
        ink.color=0xff72240d.toInt()
        c.drawText(label,x,y+ink.textSize*.07f,ink)
        ink.strokeWidth=ink.textSize*.075f
        ink.color=0xff4a210f.toInt()
        c.drawText(label,x,y,ink)
        ink.style=Paint.Style.FILL
        ink.color=Color.WHITE
        ink.shader=LinearGradient(0f,y-ink.textSize,0f,y,
            intArrayOf(0xfffff9a7.toInt(),0xffffdf3a.toInt(),
                0xffffa10d.toInt(),0xffff7111.toInt()),
            floatArrayOf(0f,.30f,.72f,1f),Shader.TileMode.CLAMP)
        c.drawText(label,x,y,ink);ink.shader=null
        val clip=c.save()
        c.clipRect(0f,0f,width.toFloat(),y-ink.textSize*.38f)
        ink.color=0x88ffffff.toInt()
        c.drawText(label,x,y,ink)
        c.restoreToCount(clip)
    }
}

/** A folded crimson enamel FEATURED ribbon; text remains a native TextView. */
internal class ShopFeaturedRibbonDrawable:Drawable(){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val shape=Path()
    private var opacity=255
    private var filter:ColorFilter?=null
    override fun draw(c:Canvas) {
        if(bounds.isEmpty)return
        val saved=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        c.scale(bounds.width()/275f,bounds.height()/62f)
        shape.reset()
        shape.moveTo(8f,12f)
        shape.lineTo(251f,1f)
        shape.lineTo(263f,29f)
        shape.lineTo(252f,36f)
        shape.lineTo(274f,56f)
        shape.lineTo(8f,61f)
        shape.lineTo(1f,47f)
        shape.lineTo(1f,18f)
        shape.close()
        paint.reset();paint.isAntiAlias=true
        paint.color=0xff681130.toInt()
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=7f
        paint.strokeJoin=Paint.Join.ROUND
        paint.alpha=opacity;paint.colorFilter=filter
        c.drawPath(shape,paint)
        paint.style=Paint.Style.FILL
        paint.shader=LinearGradient(0f,6f,0f,60f,
            intArrayOf(0xffff6485.toInt(),0xffff1657.toInt(),
                0xffc40e42.toInt()),null,Shader.TileMode.CLAMP)
        c.drawPath(shape,paint)
        paint.shader=null
        paint.color=0xaaffdbe6.toInt()
        paint.strokeWidth=3f;paint.style=Paint.Style.STROKE
        c.drawLine(17f,17f,246f,7f,paint)
        paint.style=Paint.Style.FILL
        c.restoreToCount(saved)
    }
    override fun setAlpha(value:Int){opacity=value.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(value:ColorFilter?){filter=value;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
