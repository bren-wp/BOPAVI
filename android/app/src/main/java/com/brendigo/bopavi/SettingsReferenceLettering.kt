package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.widget.TextView
import android.widget.Button
import android.view.View
import kotlin.math.min

/**
 * Owner-style embossed SETTINGS lettering, drawn by an accessible TextView
 * above its separate gold enamel/winged background. No raster button or
 * hardcoded pretend preference state is used.
 */
internal class SettingsEmbossedTitle(context:Context):TextView(context){
    private val ink=Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign=Paint.Align.CENTER
        typeface=Typeface.create("sans-serif-condensed-black",Typeface.BOLD)
        strokeJoin=Paint.Join.ROUND
        strokeCap=Paint.Cap.ROUND
        isSubpixelText=true
    }
    init {
        text="SETTINGS"
        contentDescription="SETTINGS"
        setTextColor(Color.WHITE)
        includeFontPadding=false
        isClickable=false
        isFocusable=false
        background=SettingsGoldTitleDrawable()
    }
    override fun onDraw(c:Canvas) {
        if(width<=0 || height<=0)return
        val label=text.toString()
        ink.textSize=height*.62f
        ink.textSize*=min(1f,width*.80f/ink.measureText(label).coerceAtLeast(1f))
        val x=width*.5f
        val fm=ink.fontMetrics
        val y=height*.52f-(fm.ascent+fm.descent)*.5f
        ink.shader=null;ink.style=Paint.Style.STROKE
        ink.strokeWidth=ink.textSize*.23f
        ink.color=0xff48210f.toInt()
        c.drawText(label,x,y+ink.textSize*.13f,ink)
        ink.strokeWidth=ink.textSize*.16f
        ink.color=0xff793211.toInt()
        c.drawText(label,x,y+ink.textSize*.065f,ink)
        ink.strokeWidth=ink.textSize*.085f
        ink.color=0xff361a25.toInt()
        c.drawText(label,x,y,ink)
        ink.style=Paint.Style.FILL
        ink.shader=LinearGradient(0f,y-ink.textSize,0f,y,
            intArrayOf(0xffffffff.toInt(),0xfffff9ec.toInt(),
                0xffffe2bd.toInt()),null,Shader.TileMode.CLAMP)
        c.drawText(label,x,y,ink)
        ink.shader=null
    }
}

/** Native accessible legal-navigation control with a true 3D blue chevron. */
internal class SettingsLegalArrow(context:Context,label:String,onTap:()->Unit):Button(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val line=Path()
    init {
        // Expose a real Button class to TalkBack and UIAutomator.
        // A plain View with onClick had no native button semantics, so the
        // Android 15 font-scale audit could not find either legal action.
        contentDescription=label
        text=""
        minWidth=0
        minimumWidth=0
        minHeight=0
        minimumHeight=0
        background=null
        isClickable=true
        isFocusable=true
        setOnClickListener { onTap() }
    }
    override fun onDraw(c:Canvas) {
        if(width<=0 || height<=0)return
        val centerX=width*.49f
        val centerY=height*.5f
        val radius=min(width*.19f,height*.22f)
        line.reset()
        line.moveTo(centerX-radius*.37f,centerY-radius)
        line.lineTo(centerX+radius*.56f,centerY)
        line.lineTo(centerX-radius*.37f,centerY+radius)
        p.reset();p.isAntiAlias=true;p.style=Paint.Style.STROKE
        p.strokeWidth=radius*.66f
        p.strokeCap=Paint.Cap.ROUND
        p.strokeJoin=Paint.Join.ROUND
        p.color=0xff90b4e5.toInt()
        c.drawPath(line,p)
        p.strokeWidth=radius*.47f
        p.color=0xff173e7d.toInt()
        c.drawPath(line,p)
    }
}
