package com.brendigo.bopavi

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View

internal class HomeOrbitDecoration(host:Activity):View(host){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}

    private fun sparkle(canvas:Canvas,x:Float,y:Float,r:Float){
        path.reset()
        path.moveTo(x,y-r*1.6f)
        path.quadTo(x+r*.18f,y-r*.19f,x+r,y)
        path.quadTo(x+r*.18f,y+r*.19f,x,y+r*1.6f)
        path.quadTo(x-r*.18f,y+r*.19f,x-r,y)
        path.quadTo(x-r*.18f,y-r*.19f,x,y-r*1.6f)
        path.close()
        paint.reset()
        paint.isAntiAlias=true
        paint.color=0x99b6eeff.toInt()
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=7f
        canvas.drawPath(path,paint)
        paint.style=Paint.Style.FILL
        paint.color=Color.WHITE
        canvas.drawPath(path,paint)
    }

    override fun onDraw(canvas:Canvas){
        val saved=canvas.save()
        canvas.scale(width/941f,height/1672f)
        paint.reset()
        paint.isAntiAlias=true
        paint.color=0x88e7ffff.toInt()
        paint.style=Paint.Style.STROKE
        paint.strokeCap=Paint.Cap.ROUND
        paint.strokeWidth=10f
        path.reset()
        path.moveTo(90f,848f)
        path.cubicTo(208f,708f,255f,857f,380f,820f)
        canvas.drawPath(path,paint)
        paint.color=0x88ffffff.toInt()
        paint.strokeWidth=4f
        canvas.drawArc(RectF(110f,475f,830f,911f),25f,81f,false,paint)
        // The latest owner Home renders sparkles and flight streaks, NOT
        // three golden hoops (nor phantom collectible rings).
        sparkle(canvas,729f,603f,14f)
        sparkle(canvas,681f,542f,11f)
        sparkle(canvas,264f,417f,13f)
        sparkle(canvas,124f,726f,10f)
        sparkle(canvas,182f,791f,6f)
        canvas.restoreToCount(saved)
    }
}
