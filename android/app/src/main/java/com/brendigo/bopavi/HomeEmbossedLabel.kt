package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
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
    private val primary:Boolean,
    private val paintHeightFraction:Float=.66f,
    private val paintWidthFraction:Float=.87f,
    private val footer:Boolean=false
):TextView(context){
    private val ink=Paint(Paint.ANTI_ALIAS_FLAG).apply{
        textAlign=Paint.Align.CENTER
        typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
        strokeJoin=Paint.Join.ROUND
        strokeCap=Paint.Cap.ROUND
        isSubpixelText=true
    }

    init {
        text=caption
        setTextSize(TypedValue.COMPLEX_UNIT_SP,nominalSp)
        typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
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
        // The reference stage measures in image pixels, while SP uses device
        // density; clamp to the actual native label bounds on every screen.
        // The Home reference uses taller white 3D letters than the other
        // game's result dialogs. Optional fractions let Home match that art
        // without changing every reused native embossed label elsewhere.
        ink.textSize=min(textSize,height*paintHeightFraction)
        val measured=ink.measureText(caption).coerceAtLeast(1f)
        ink.textSize*=min(1f,(width*paintWidthFraction)/measured)
        val metrics=ink.fontMetrics
        val x=width/2f
        val y=height/2f-(metrics.ascent+metrics.descent)/2f
        val rim=if(primary)0xff9b3608.toInt() else
            if(footer)0xff0b1834.toInt() else 0xff11246c.toInt()
        val drop=if(primary)0xff6f2d06.toInt() else
            if(footer)0xff060e23.toInt() else 0xff08265d.toInt()

        ink.style=Paint.Style.STROKE
        ink.strokeWidth=ink.textSize*(if(footer).13f else .13f)
        ink.color=drop
        canvas.drawText(caption,x,y+height*(if(footer).035f else .058f),ink)
        ink.strokeWidth=ink.textSize*(if(footer).075f else .09f)
        ink.color=rim
        canvas.drawText(caption,x,y,ink)
        ink.style=Paint.Style.FILL
        ink.color=Color.WHITE
        // The owner's 3D capitals have a warm pearly lower bevel on gold
        // Play and a cool ice-blue lower bevel on the other Home actions.
        // Preserve real TextView text and parent accessibility navigation.
        ink.shader=LinearGradient(0f,y-ink.textSize,0f,
            y+ink.textSize*.13f,
            Color.WHITE,if(primary)0xffffe8aa.toInt() else
                if(footer)0xfff4fbff.toInt() else
                    0xffcdeaff.toInt(),Shader.TileMode.CLAMP)
        canvas.drawText(caption,x,y,ink)
        ink.shader=null

        val checkpoint=canvas.save()
        canvas.clipRect(0f,0f,width.toFloat(),y-ink.textSize*.17f)
        ink.color=0xfff9fff5.toInt()
        canvas.drawText(caption,x,y,ink)
        canvas.restoreToCount(checkpoint)
    }
}
