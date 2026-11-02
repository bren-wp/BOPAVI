package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * White feather wings behind the real native PLAY button on Home.
 * They never intercept taps or simulate any action.
 */
internal class HomePlayWings(context:Context):View(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    init {
        importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable=false
        isFocusable=false
    }

    private fun drawWing(canvas:Canvas,mirror:Boolean){
        val save=canvas.save()
        if(mirror){
            canvas.translate(719f,0f)
            canvas.scale(-1f,1f)
        }
        path.reset()
        path.moveTo(84f,48f)
        path.cubicTo(57f,56f,36f,35f,16f,27f)
        path.cubicTo(11f,67f,26f,95f,66f,104f)
        path.cubicTo(37f,101f,24f,98f,7f,91f)
        path.cubicTo(21f,131f,45f,132f,80f,111f)
        path.cubicTo(48f,137f,41f,141f,30f,145f)
        path.cubicTo(60f,158f,87f,132f,110f,101f)
        path.close()
        canvas.save()
        canvas.translate(0f,6f)
        p.style=Paint.Style.FILL
        p.color=0xff09265a.toInt()
        canvas.drawPath(path,p)
        canvas.restore()
        p.color=0xfff6fcff.toInt()
        canvas.drawPath(path,p)
        p.style=Paint.Style.STROKE
        p.strokeWidth=5f
        p.strokeJoin=Paint.Join.ROUND
        p.color=0xff17396c.toInt()
        canvas.drawPath(path,p)
        p.strokeWidth=2.5f
        p.color=0xffb2d2fa.toInt()
        canvas.drawLine(21f,54f,83f,92f,p)
        canvas.drawLine(18f,105f,73f,115f,p)
        canvas.drawLine(43f,141f,91f,117f,p)
        p.style=Paint.Style.FILL
        canvas.restoreToCount(save)
    }

    override fun onDraw(canvas:Canvas){
        super.onDraw(canvas)
        if(width==0||height==0)return
        val save=canvas.save()
        canvas.scale(width/719f,height/171f)
        drawWing(canvas,false)
        drawWing(canvas,true)
        canvas.restoreToCount(save)
    }
}
