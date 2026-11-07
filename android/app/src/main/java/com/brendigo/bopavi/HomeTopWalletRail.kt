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
    /** Actual lower edge of both notch-safe currency wallets, in root pixels. */
    var onRowBottomChanged:((Int)->Unit)?=null
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
        // Both native wallet chips form ONE horizontal owner HUD row.
        // A cutout intersecting either chip must shift the pair together:
        // moving only COINS or only GEM causes the exact logo overlap seen
        // in the supplied failure screenshot.
        var sharedTop=entries.maxOfOrNull{
            maxOf(safeTop,(it.b.y*scale).toInt())
        } ?: safeTop
        for(pass in 0..cutouts.size){
            var revised=sharedTop
            for(e in entries){
                val x=(safeLeft+e.b.x*scale).toInt()
                for(cutout in cutouts){
                    val area=Rect(x,sharedTop,x+e.child.measuredWidth,
                        sharedTop+e.child.measuredHeight)
                    if(Rect.intersects(area,cutout)){
                        revised=maxOf(revised,cutout.bottom+
                            (4f*resources.displayMetrics.density).toInt())
                    }
                }
            }
            if(revised==sharedTop)break
            sharedTop=revised
        }
        val minBottomSpace=entries.maxOfOrNull{
            it.child.measuredHeight
        } ?: 0
        sharedTop=sharedTop.coerceAtMost(
            (b-t-minBottomSpace).coerceAtLeast(0))
        for(e in entries){
            val x=(safeLeft+e.b.x*scale).toInt()
            e.child.layout(x,sharedTop,x+e.child.measuredWidth,
                sharedTop+e.child.measuredHeight)
        }
        onRowBottomChanged?.invoke(sharedTop+minBottomSpace)
    }
}
