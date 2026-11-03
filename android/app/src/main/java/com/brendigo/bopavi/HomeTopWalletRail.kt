package com.brendigo.bopavi

import android.content.Context
import android.view.View
import android.view.ViewGroup

/**
 * Anchors real interactive top wallets to the screen's safe top, not to the
 * letterboxed center of a 941x1672 stage on tall Android displays.
 */
internal class HomeTopWalletRail(context:Context):ViewGroup(context) {
    private data class Entry(val child:View,val b:HomeBounds)
    private val entries=ArrayList<Entry>(2)
    private var safeLeft=0
    private var safeTop=0
    private var safeRight=0

    fun attach(view:View,bounds:HomeBounds){
        entries.add(Entry(view,bounds))
        addView(view,ViewGroup.LayoutParams(-2,-2))
    }

    fun setSafe(left:Int,top:Int,right:Int){
        if(safeLeft!=left || safeTop!=top || safeRight!=right){
            safeLeft=left;safeTop=top;safeRight=right
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec:Int,heightMeasureSpec:Int){
        val w=MeasureSpec.getSize(widthMeasureSpec)
        val h=MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w,h)
        val scale=(w-safeLeft-safeRight).coerceAtLeast(1)/
            HomeReferenceLayout.width
        for(e in entries){
            e.child.measure(
                MeasureSpec.makeMeasureSpec((e.b.w*scale).toInt().coerceAtLeast(1),
                    MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((e.b.h*scale).toInt().coerceAtLeast(1),
                    MeasureSpec.EXACTLY))
        }
    }

    override fun onLayout(changed:Boolean,l:Int,t:Int,r:Int,b:Int){
        val scale=(r-l-safeLeft-safeRight).coerceAtLeast(1)/
            HomeReferenceLayout.width
        for(e in entries){
            val x=(safeLeft+e.b.x*scale).toInt()
            // Avoid double-counting the reference's 21px top margin.
            // Status bar/cutout safety remains mandatory if it is visible.
            val y=maxOf(safeTop,(e.b.y*scale).toInt())
            e.child.layout(x,y,x+e.child.measuredWidth,y+e.child.measuredHeight)
        }
    }
}
