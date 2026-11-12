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

    /** Thin hollow GOLD rings recede behind Bopi's right wing.
     * The large striped candy hoops in the old version were absent from
     * the supplied Home reference.
     */
    private fun ring(canvas:Canvas,x:Float,y:Float,rx:Float,ry:Float,angle:Float){
        val saved=canvas.save()
        canvas.rotate(angle,x,y)
        val oval=RectF(x-rx,y-ry,x+rx,y+ry)
        paint.reset()
        paint.isAntiAlias=true
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=rx*.49f
        paint.color=0x77032453
        canvas.drawOval(RectF(oval.left+4f,oval.top+7f,
            oval.right+4f,oval.bottom+7f),paint)
        paint.strokeWidth=rx*.39f
        paint.color=0xff9c5011.toInt()
        canvas.drawOval(oval,paint)
        paint.strokeWidth=rx*.32f
        paint.color=0xffffc70a.toInt()
        canvas.drawOval(oval,paint)
        paint.strokeWidth=rx*.14f
        paint.color=0xfffff0a6.toInt()
        canvas.drawArc(oval,206f,141f,false,paint)
        paint.strokeWidth=rx*.065f
        paint.color=Color.WHITE
        canvas.drawArc(oval,229f,78f,false,paint)
        paint.style=Paint.Style.FILL
        canvas.restoreToCount(saved)
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
        // Three small metallic golden hoops, all to the right of Bopi.
        ring(canvas,761f,672f,18f,41f,-23f)
        ring(canvas,810f,705f,14f,31f,-17f)
        ring(canvas,852f,744f,10f,20f,-20f)
        sparkle(canvas,264f,417f,13f)
        sparkle(canvas,124f,726f,10f)
        sparkle(canvas,182f,791f,6f)
        canvas.restoreToCount(saved)
    }
}
