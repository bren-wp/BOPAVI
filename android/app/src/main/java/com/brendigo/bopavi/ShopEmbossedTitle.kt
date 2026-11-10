package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import kotlin.math.min

/**
 * Real native, screen-reader-visible SHOP title, with raised golden enamel.
 * The passive wooden sign and separate white wings are behind this TextView.
 * No title bitmap or screenshot is stretched across different device ratios.
 */
internal class ShopEmbossedTitle(context:Context):TextView(context){
    private val ink=Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign=Paint.Align.CENTER
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        strokeJoin=Paint.Join.ROUND
        strokeCap=Paint.Cap.ROUND
        isSubpixelText=true
    }

    init {
        text="SHOP"
        setTextSize(TypedValue.COMPLEX_UNIT_SP,91f)
        setTextColor(0xffffc11d.toInt())
        gravity=Gravity.CENTER
        includeFontPadding=false
        isFocusable=false
        isClickable=false
        contentDescription="SHOP"
    }

    override fun onDraw(c:Canvas) {
        if(width<=0 || height<=0)return
        val wanted=min(textSize,height*.86f)
        ink.textSize=wanted
        val measured=ink.measureText("SHOP").coerceAtLeast(1f)
        ink.textSize*=min(1f,(width*.94f)/measured)
        val x=width*.5f
        val metrics=ink.fontMetrics
        val y=height*.5f-(metrics.ascent+metrics.descent)*.5f

        ink.style=Paint.Style.STROKE
        ink.shader=null
        ink.strokeWidth=ink.textSize*.20f
        ink.color=0xff2d133e.toInt()
        c.drawText("SHOP",x,y+height*.09f,ink)
        ink.strokeWidth=ink.textSize*.14f
        ink.color=0xff612313.toInt()
        c.drawText("SHOP",x,y+height*.055f,ink)
        ink.strokeWidth=ink.textSize*.09f
        ink.color=0xff8d2e0c.toInt()
        c.drawText("SHOP",x,y,ink)

        ink.style=Paint.Style.FILL
        ink.color=Color.WHITE
        ink.shader=LinearGradient(0f,y-ink.textSize,0f,
            y+ink.textSize*.23f,
            intArrayOf(0xfffffaaa.toInt(),0xffffd932.toInt(),
                0xffff9112.toInt(),0xffff5810.toInt()),
            floatArrayOf(0f,.28f,.70f,1f),Shader.TileMode.CLAMP)
        c.drawText("SHOP",x,y,ink)
        ink.shader=null
        val clip=c.save()
        c.clipRect(0f,0f,width.toFloat(),y-ink.textSize*.25f)
        ink.color=0x77ffffff
        c.drawText("SHOP",x,y,ink)
        c.restoreToCount(clip)
    }
}
