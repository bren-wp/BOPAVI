package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import kotlin.math.min

/** 3D-stroked reference lettering; parent button owns TalkBack semantics. */
internal class HomeEmbossedLabel(
    context:Context,
    private val caption:String,
    private val nominalSp:Float,
    private val primary:Boolean
):View(context){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{
        textAlign=Paint.Align.CENTER
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        strokeJoin=Paint.Join.ROUND
        strokeCap=Paint.Cap.ROUND
        isSubpixelText=true
    }
    init {
        importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable=false
        isFocusable=false
    }

    override fun onDraw(canvas:Canvas){
        super.onDraw(canvas)
        if(width<=0 || height<=0)return
        val scale=resources.displayMetrics.scaledDensity
        val intended=nominalSp*scale
        p.textSize=intended
        // On smaller displays or large system fonts, fit only the decorative
        // text. The parent retains a real, separate native 48dp+ hit target.
        val textWidth=p.measureText(caption).coerceAtLeast(1f)
        val factor=min(1f,(width*.92f)/textWidth)
        p.textSize=intended*factor
        val metrics=p.fontMetrics
        val x=width/2f
        val y=height/2f-(metrics.ascent+metrics.descent)/2f
        val navy=if(primary)0xff9b3608.toInt() else 0xff11246c.toInt()
        val drop=if(primary)0xff6f2d06.toInt() else 0xff08265d.toInt()
        p.strokeWidth=p.textSize*.16f
        p.style=Paint.Style.STROKE
        p.color=drop
        canvas.drawText(caption,x,y+height*.073f,p)
        p.strokeWidth=p.textSize*.115f
        p.color=navy
        canvas.drawText(caption,x,y,p)
        p.style=Paint.Style.FILL
        p.color=Color.WHITE
        canvas.drawText(caption,x,y,p)
        val checkpoint=canvas.save()
        canvas.clipRect(0f,0f,width.toFloat(),y-p.textSize*.17f)
        p.color=0xfff9fff5.toInt()
        canvas.drawText(caption,x,y,p)
        canvas.restoreToCount(checkpoint)
    }
}
