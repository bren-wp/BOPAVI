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
import android.view.View
import android.widget.TextView
import kotlin.math.min

/**
 * Accessible native TextView with the original BOPAVI layered 3D title style.
 * Never replace this control with uninspectable Canvas-only lettering.
 */
internal class ResultEmblemLabel(
    context:Context,
    private val caption:String,
    sizeSp:Float,
    private val tone:Tone
):TextView(context) {
    internal enum class Tone { ICE, CORAL, GOLD }

    private val ink=Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign=Paint.Align.CENTER
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        strokeJoin=Paint.Join.ROUND
        isSubpixelText=true
    }

    init {
        text=caption
        setTextSize(TypedValue.COMPLEX_UNIT_SP,sizeSp)
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        gravity=Gravity.CENTER
        includeFontPadding=false
        setTextColor(Color.WHITE)
        isClickable=false
        isFocusable=false
        importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    override fun onDraw(canvas:Canvas) {
        if(width<=0 || height<=0)return
        ink.shader=null
        ink.style=Paint.Style.FILL
        ink.textSize=textSize
        val measured=ink.measureText(caption).coerceAtLeast(1f)
        val metrics=ink.fontMetrics
        val lineHeight=metrics.descent-metrics.ascent
        ink.textSize*=min(1f,min(width*.88f/measured,height*.69f/lineHeight))
        val resized=ink.fontMetrics
        val x=width*.5f
        val y=height*.5f-(resized.ascent+resized.descent)*.5f
        val dark=0xff06184b.toInt()
        val trim=if(tone==Tone.CORAL)0xff752008.toInt() else dark
        ink.style=Paint.Style.STROKE
        ink.strokeWidth=ink.textSize*.19f
        ink.color=dark
        canvas.drawText(caption,x,y+height*.07f,ink)
        ink.strokeWidth=ink.textSize*.12f
        ink.color=trim
        canvas.drawText(caption,x,y,ink)
        val top=when(tone){
            Tone.ICE->0xffffffff.toInt()
            Tone.CORAL->0xffffa04c.toInt()
            Tone.GOLD->0xfffff27e.toInt()
        }
        val bottom=when(tone){
            Tone.ICE->0xffb8c9ff.toInt()
            Tone.CORAL->0xfffa3128.toInt()
            Tone.GOLD->0xffff880f.toInt()
        }
        ink.style=Paint.Style.FILL
        ink.color=Color.WHITE
        ink.shader=LinearGradient(0f,y-ink.textSize,0f,y+ink.textSize*.24f,
            top,bottom,Shader.TileMode.CLAMP)
        canvas.drawText(caption,x,y,ink)
        ink.shader=null
        // Small white upper reflection over the authentic gradient.
        val save=canvas.save()
        canvas.clipRect(0f,0f,width.toFloat(),y-ink.textSize*.17f)
        ink.color=if(tone==Tone.CORAL)0xffffe7c1.toInt() else Color.WHITE
        canvas.drawText(caption,x,y,ink)
        canvas.restoreToCount(save)
    }
}
