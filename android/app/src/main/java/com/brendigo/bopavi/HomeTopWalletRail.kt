package com.brendigo.bopavi

import android.content.Context
import android.graphics.Rect
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
    // A centered punch-hole/cutout must affect only a wallet it actually
    // intersects, rather than shifting BOTH balances down by 60–90 pixels.
    private var cutouts:List<Rect> = emptyList()

    fun attach(view:View,bounds:HomeBounds){
        entries.add(Entry(view,bounds))
        addView(view,ViewGroup.LayoutParams(-2,-2))
    }

    fun setSafe(left:Int,top:Int,right:Int,
                displayCutouts:List<Rect> = emptyList()){
        val newCutouts=displayCutouts.map { Rect(it) }
        if(safeLeft!=left || safeTop!=top || safeRight!=right ||
            newCutouts!=cutouts){
            safeLeft=left;safeTop=top;safeRight=right
            cutouts=newCutouts
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
            var y=maxOf(safeTop,(e.b.y*scale).toInt())
            // The Home reference is full-screen. With hidden status bars
            // the wallets belong at y≈21, except if a physical cutout
            // actually covers this particular native clickable control.
            for(cutout in cutouts) {
                val candidate=Rect(x,y,x+e.child.measuredWidth,
                    y+e.child.measuredHeight)
                if(Rect.intersects(candidate,cutout)) {
                    y=maxOf(y,cutout.bottom+
                        (4f*resources.displayMetrics.density).toInt())
                }
            }
            y=y.coerceAtMost((b-t-e.child.measuredHeight).coerceAtLeast(0))
            e.child.layout(x,y,x+e.child.measuredWidth,y+e.child.measuredHeight)
        }
    }
}
