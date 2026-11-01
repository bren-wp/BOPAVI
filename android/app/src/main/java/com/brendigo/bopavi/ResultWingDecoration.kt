package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/** Local decorative 3D wings behind result titles; native controls remain
 * separate and are never replaced by a flattened image hit target. */
internal class ResultWingDecoration(context:Context):View(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val wing=Path()
    init {importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}

    private fun drawWing(c:Canvas,mirror:Boolean){
        val saved=c.save()
        if(mirror){
            c.translate(650f,0f)
            c.scale(-1f,1f)
        }
        wing.reset()
        wing.moveTo(153f,43f)
        wing.cubicTo(119f,58f,81f,37f,31f,12f)
        wing.cubicTo(20f,58f,48f,78f,81f,93f)
        wing.cubicTo(44f,93f,23f,88f,10f,80f)
        wing.cubicTo(18f,117f,48f,127f,96f,117f)
        wing.cubicTo(65f,137f,52f,142f,37f,142f)
        wing.cubicTo(72f,164f,128f,135f,164f,97f)
        wing.close()
        c.save()
        c.translate(0f,8f)
        p.style=Paint.Style.FILL;p.color=0xff0c245d.toInt()
        c.drawPath(wing,p)
        c.restore()
        p.color=0xfff8fdff.toInt()
        c.drawPath(wing,p)
        p.style=Paint.Style.STROKE;p.strokeWidth=6f
        p.strokeJoin=Paint.Join.ROUND
        p.color=0xff0b2e73.toInt()
        c.drawPath(wing,p)
        p.strokeWidth=3f;p.color=0xffb8d6ff.toInt()
        c.drawLine(28f,46f,117f,92f,p)
        c.drawLine(18f,96f,104f,113f,p)
        c.drawLine(47f,142f,127f,120f,p)
        p.style=Paint.Style.FILL
        c.restoreToCount(saved)
    }
    override fun onDraw(canvas:Canvas){
        val saved=canvas.save()
        canvas.scale(width/650f,height/165f)
        drawWing(canvas,false)
        drawWing(canvas,true)
        canvas.restoreToCount(saved)
    }
}
