package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.widget.TextView
import kotlin.math.min

/**
 * Native and accessible raised lettering on the wooden Skins nameplate.
 * Unlike a screenshot, this paints the currently selected pilot's REAL title.
 */
internal class SkinsEmbossedName(context:Context, label:String):TextView(context) {
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
        setTextColor(Color.WHITE)
        includeFontPadding=false
        isClickable=false
        isFocusable=false
    }
    override fun onDraw(canvas:Canvas){
        val caption=text.toString()
        if(width<=0 || height<=0 || caption.isEmpty())return
        val centerX=width*.5f
        ink.textSize=height*.55f
        val measured=ink.measureText(caption).coerceAtLeast(1f)
        ink.textSize*=min(1f,width*.90f/measured)
        val m=ink.fontMetrics
        val baseline=height*.48f-(m.ascent+m.descent)*.5f
        ink.shader=null
        ink.style=Paint.Style.STROKE
        ink.strokeWidth=ink.textSize*.24f
        ink.color=0xff4b190e.toInt()
        canvas.drawText(caption,centerX,baseline+ink.textSize*.09f,ink)
        ink.strokeWidth=ink.textSize*.16f
        ink.color=0xff743014.toInt()
        canvas.drawText(caption,centerX,baseline+ink.textSize*.04f,ink)
        ink.strokeWidth=ink.textSize*.085f
        ink.color=0xff371a10.toInt()
        canvas.drawText(caption,centerX,baseline,ink)
        ink.style=Paint.Style.FILL
        ink.color=Color.WHITE
        ink.shader=LinearGradient(0f,baseline-ink.textSize,0f,baseline,
            intArrayOf(0xffffffff.toInt(),0xfffff8e6.toInt(),
                0xffffdb9e.toInt()),null,Shader.TileMode.CLAMP)
        canvas.drawText(caption,centerX,baseline,ink)
        ink.shader=null
        val clip=canvas.save()
        canvas.clipRect(0f,0f,width.toFloat(),baseline-ink.textSize*.42f)
        ink.color=0x77ffffff
        canvas.drawText(caption,centerX,baseline,ink)
        canvas.restoreToCount(clip)
    }
}
