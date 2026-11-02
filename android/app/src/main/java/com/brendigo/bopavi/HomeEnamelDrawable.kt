package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.min

/**
 * Fully local, scalable, 3D enamel button art for the owner's Home reference.
 * Geometry is vector Canvas drawing; no emoji, fonts masquerading as icons,
 * external images, services, or baked-in clickable screenshot regions.
 */
internal class HomeEnamelDrawable(
    private val highlight:Int,
    private val middle:Int,
    private val shadow:Int,
    private val edge:Int
):Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private var opacity=255
    private var filter:ColorFilter?=null
    private var pressed=false
    private var selected=false
    private var enabled=true

    // Drawable state comes from the actual Android View/3D button, including
    // disabled level-map nodes, rather than from a decorative Canvas overlay.
    override fun isStateful():Boolean=true

    override fun onStateChange(state:IntArray):Boolean {
        val nextPressed=android.R.attr.state_pressed in state
        val nextSelected=android.R.attr.state_selected in state
        val nextEnabled=android.R.attr.state_enabled in state
        if(pressed==nextPressed && selected==nextSelected && enabled==nextEnabled)
            return false
        pressed=nextPressed
        selected=nextSelected
        enabled=nextEnabled
        invalidateSelf()
        return true
    }

    override fun draw(canvas:Canvas){
        val w=bounds.width().toFloat()
        val h=bounds.height().toFloat()
        if(w<=0f || h<=0f)return
        val checkpoint=canvas.save()
        canvas.translate(bounds.left.toFloat(),bounds.top.toFloat())
        // Only actual disabled views are dimmed; the visible button remains
        // readable and physically separate from its live accessibility label.
        if(!enabled)canvas.saveLayerAlpha(0f,0f,w,h,145)
        val radius=min(h*.29f,w*.16f)
        val drop=min(15f,h*.085f)*(if(pressed).48f else 1f)
        val inset=min(8f,h*.041f)
        fun coat(color:Int){
            p.reset()
            p.isAntiAlias=true
            p.color=Color.argb(Color.alpha(color)*opacity/255,
                Color.red(color),Color.green(color),Color.blue(color))
            p.colorFilter=filter
            p.style=Paint.Style.FILL
        }
        fun rounded(l:Float,t:Float,r:Float,b:Float,rad:Float){
            canvas.drawRoundRect(RectF(l,t,r,b),rad,rad,p)
        }
        // Dark offset extrusion, visible even against white clouds.
        coat(0x77001b4e)
        rounded(3f,drop+3f,w-2f,h, radius)
        coat(shadow)
        rounded(1f,drop*.65f,w-1f,h-3f,radius)
        // Raised outer gold/ice rim with a light lip.
        coat(edge)
        rounded(0f,0f,w-2f,h-drop*.72f,radius)
        coat(0xfffaffff.toInt())
        rounded(inset*.34f,inset*.34f,w-inset*.34f-2f,
            h-drop*.85f-inset*.28f,radius*.96f)
        // Inset glossy face, deliberately non-flat across all button sizes.
        val face=RectF(inset,inset,w-inset-2f,h-drop-inset*.42f)
        coat(middle)
        p.shader=LinearGradient(0f,face.top,0f,face.bottom,
            intArrayOf(highlight,middle,shadow),floatArrayOf(0f,.46f,1f),
            Shader.TileMode.CLAMP)
        canvas.drawRoundRect(face,radius*.85f,radius*.85f,p)
        p.shader=null
        if(pressed || selected) {
            coat(if(pressed)0x38001848 else 0x28ffffff)
            canvas.drawRoundRect(face,radius*.85f,radius*.85f,p)
        }
        // Upper curved white specular reflection, with dark lower bevel.
        val upper=RectF(face.left+10f,face.top+7f,
            face.right-10f,face.top+(face.height()*.39f))
        coat(0x77ffffff)
        rounded(upper.left,upper.top,upper.right,upper.bottom,
            min(radius*.72f,upper.height()*.58f))
        coat(0x55ffffff)
        p.style=Paint.Style.STROKE
        p.strokeWidth=min(5f,h*.025f)
        canvas.drawArc(RectF(face.left+14f,face.top+6f,
            face.right-15f,face.bottom-9f),202f,124f,false,p)
        coat(0x5500224e)
        p.style=Paint.Style.STROKE
        p.strokeWidth=min(4f,h*.025f)
        canvas.drawArc(RectF(face.left+8f,face.top+7f,
            face.right-8f,face.bottom-4f),18f,144f,false,p)
        canvas.restoreToCount(checkpoint)
    }
    override fun setAlpha(alpha:Int){opacity=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(colorFilter:ColorFilter?){filter=colorFilter;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
