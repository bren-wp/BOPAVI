package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import kotlin.math.min

/**
 * Real native TextView for the 3D-stroked Home captions.
 *
 * UIAutomator/TalkBack must see the visible button label as actual text,
 * while its clickable parent remains the only navigation action. A bare
 * Canvas View paints the same pixels but exposes an empty accessibility text.
 */
internal class HomeEmbossedLabel(
    context:Context,
    private val caption:String,
    nominalSp:Float,
    private val primary:Boolean
):TextView(context){
    private val ink=Paint(Paint.ANTI_ALIAS_FLAG).apply{
        textAlign=Paint.Align.CENTER
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        strokeJoin=Paint.Join.ROUND
        strokeCap=Paint.Cap.ROUND
        isSubpixelText=true
    }

    init {
        text=caption
        setTextSize(TypedValue.COMPLEX_UNIT_SP,nominalSp)
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        setTextColor(Color.WHITE)
        gravity=Gravity.CENTER
        maxLines=1
        includeFontPadding=false
        importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES
        isClickable=false
        isFocusable=false
    }

    override fun onDraw(canvas:Canvas){
        if(width<=0 || height<=0)return
        ink.textSize=textSize
        // Fit the decorative lettering; do not clip the real Home action on
        // narrower screens or when Android increases system font scaling.
        val measured=ink.measureText(caption).coerceAtLeast(1f)
        ink.textSize*=min(1f,(width*.92f)/measured)
        val metrics=ink.fontMetrics
        val x=width/2f
        val y=height/2f-(metrics.ascent+metrics.descent)/2f
        val rim=if(primary)0xff9b3608.toInt() else 0xff11246c.toInt()
        val drop=if(primary)0xff6f2d06.toInt() else 0xff08265d.toInt()

        ink.style=Paint.Style.STROKE
        ink.strokeWidth=ink.textSize*.16f
        ink.color=drop
        canvas.drawText(caption,x,y+height*.073f,ink)
        ink.strokeWidth=ink.textSize*.115f
        ink.color=rim
        canvas.drawText(caption,x,y,ink)
        ink.style=Paint.Style.FILL
        ink.color=Color.WHITE
        canvas.drawText(caption,x,y,ink)

        val checkpoint=canvas.save()
        canvas.clipRect(0f,0f,width.toFloat(),y-ink.textSize*.17f)
        ink.color=0xfff9fff5.toInt()
        canvas.drawText(caption,x,y,ink)
        canvas.restoreToCount(checkpoint)
    }
}
