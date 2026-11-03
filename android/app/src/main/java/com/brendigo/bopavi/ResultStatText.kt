package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import kotlin.math.min

/**
 * Real Android TextView: reference-style 3D strokes for Game Over stat rows.
 * Text/content are genuine saved result data, never sample screenshot values.
 */
internal class ResultStatText(
    context:Context,caption:String,private val isValue:Boolean
):TextView(context){
    private val ink=Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        strokeJoin=Paint.Join.ROUND
        isSubpixelText=true
    }
    init{
        text=caption
        setTextSize(TypedValue.COMPLEX_UNIT_SP,if(isValue)30f else 23f)
        setTextColor(if(isValue)0xffffb934.toInt() else Color.WHITE)
        gravity=(if(isValue)Gravity.END else Gravity.START) or Gravity.CENTER_VERTICAL
        maxLines=1
        includeFontPadding=false
    }
    override fun onDraw(canvas:Canvas){
        if(width<=0 || height<=0)return
        val title=text.toString()
        ink.textSize=min(textSize,height*.56f)
        val measured=ink.measureText(title).coerceAtLeast(1f)
        ink.textSize*=min(1f,width*.92f/measured)
        val fm=ink.fontMetrics
        val y=height/2f-(fm.ascent+fm.descent)/2f
        ink.textAlign=if(isValue)Paint.Align.RIGHT else Paint.Align.LEFT
        val x=if(isValue)width*.97f else width*.03f
        ink.style=Paint.Style.STROKE
        ink.strokeWidth=ink.textSize*.16f
        ink.color=if(isValue)0xff63300d.toInt() else 0xff062257.toInt()
        canvas.drawText(title,x,y+height*.042f,ink)
        ink.strokeWidth=ink.textSize*.105f
        ink.color=if(isValue)0xffa34b10.toInt() else 0xff123b7b.toInt()
        canvas.drawText(title,x,y,ink)
        ink.style=Paint.Style.FILL
        ink.color=if(isValue)0xffffc43b.toInt() else Color.WHITE
        canvas.drawText(title,x,y,ink)
    }
}
