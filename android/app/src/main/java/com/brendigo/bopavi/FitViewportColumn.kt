package com.brendigo.bopavi

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * One-screen, non-scrolling home menu. Measures real interactive content first
 * and uniformly fits it inside the inset-safe viewport on short devices.
 * Large phones keep full-size controls and a weighted illustrated hero space.
 *
 * Touch/accessibility belongs to the actual child controls, never raster art.
 */
internal class FitViewportColumn(context: Context) : FrameLayout(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w=View.MeasureSpec.getSize(widthMeasureSpec)
        val h=View.MeasureSpec.getSize(heightMeasureSpec)
        val body=getChildAt(0)
        if(body==null) { setMeasuredDimension(w,h);return }
        val availableW=(w-paddingLeft-paddingRight).coerceAtLeast(1)
        val availableH=(h-paddingTop-paddingBottom).coerceAtLeast(1)
        val exactW=View.MeasureSpec.makeMeasureSpec(availableW,View.MeasureSpec.EXACTLY)
        // UNSPECIFIED detects intrinsic button/footer size before the weighted
        // hero spacer absorbs the remaining height.
        body.measure(exactW,View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED))
        val intrinsicH=body.measuredHeight.coerceAtLeast(1)
        val fit=minOf(1f,availableH.toFloat()/intrinsicH)
        if(fit==1f) body.measure(exactW,View.MeasureSpec.makeMeasureSpec(availableH,View.MeasureSpec.EXACTLY))
        body.pivotX=availableW*.5f
        body.pivotY=0f
        body.scaleX=fit
        body.scaleY=fit
        setMeasuredDimension(w,h)
    }
    override fun onLayout(changed:Boolean,left:Int,top:Int,right:Int,bottom:Int) {
        val body=getChildAt(0)?:return
        body.layout(paddingLeft,paddingTop,
                    paddingLeft+body.measuredWidth,paddingTop+body.measuredHeight)
    }
}
