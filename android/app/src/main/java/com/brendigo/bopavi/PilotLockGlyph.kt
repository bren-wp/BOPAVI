package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import kotlin.math.min

/** Platform-independent closed padlock, not an emoji that changes with fonts. */
internal class PilotLockGlyph(context:Context):View(context){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onDraw(canvas:Canvas) {
        val scale=min(width/40f,height/46f)
        val saved=canvas.save()
        canvas.scale(scale,scale)
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=5f
        paint.strokeCap=Paint.Cap.ROUND
        paint.color=0xff203451.toInt()
        canvas.drawArc(10f,4f,30f,31f,180f,180f,false,paint)
        paint.style=Paint.Style.FILL
        paint.color=0xffecf4ff.toInt()
        canvas.drawRoundRect(5f,19f,35f,44f,6f,6f,paint)
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=3f
        paint.color=0xff293f5f.toInt()
        canvas.drawRoundRect(5f,19f,35f,44f,6f,6f,paint)
        paint.style=Paint.Style.FILL
        canvas.drawCircle(20f,30f,3f,paint)
        canvas.drawRoundRect(18f,31f,22f,38f,1f,1f,paint)
        canvas.restoreToCount(saved)
    }
}
