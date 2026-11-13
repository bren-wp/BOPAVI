package com.brendigo.bopavi

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

internal class GameOverCrown(host:Activity):View(host) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val shape=Path()
    init {importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onDraw(canvas:Canvas){
        val save=canvas.save()
        canvas.scale(width/79f,height/76f)
        shape.reset()
        shape.moveTo(8f,18f);shape.lineTo(22f,31f)
        shape.lineTo(40f,4f);shape.lineTo(56f,31f)
        shape.lineTo(72f,18f);shape.lineTo(65f,63f)
        shape.lineTo(16f,63f);shape.close()
        // Raised black/bronze extrusion follows the same crown silhouette.
        val extrusion=canvas.save()
        canvas.translate(0f,6f)
        p.style=Paint.Style.FILL
        p.color=0xff4f260b.toInt()
        canvas.drawPath(shape,p)
        canvas.restoreToCount(extrusion)
        p.color=0xfff18018.toInt()
        canvas.drawPath(shape,p)
        p.color=0xffffbf16.toInt()
        canvas.drawPath(shape,p)
        p.style=Paint.Style.STROKE;p.strokeWidth=5f
        p.color=0xff072957.toInt();canvas.drawPath(shape,p)
        // Gold upper facets and specular light; purely decorative artwork.
        p.strokeWidth=3f
        p.color=0xfffff0ad.toInt()
        canvas.drawLine(13f,20f,25f,36f,p)
        canvas.drawLine(25f,36f,40f,10f,p)
        canvas.drawLine(40f,10f,54f,34f,p)
        p.style=Paint.Style.FILL
        p.color=0xfffff7d0.toInt()
        canvas.drawCircle(39f,13f,3f,p)
        canvas.drawCircle(10f,20f,2.6f,p)
        canvas.drawCircle(71f,20f,2.6f,p)
        canvas.restoreToCount(save)
    }
}
