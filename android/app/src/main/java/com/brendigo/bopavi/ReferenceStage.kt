package com.brendigo.bopavi

import android.content.Context
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import kotlin.math.min

/** Shared 941×1672 layout camera retained for Home, Shop, Skins and Settings. */
internal class LevelMapStage(context:Context):ViewGroup(context){
    private data class Slot(val view:View,val bounds:RectF,val full:Boolean,
                            val minTouch:Boolean,val referenceAligned:Boolean,
                            val verticalAnchor:Float)
    private val slots=ArrayList<Slot>()
    // Opt-in for native portrait reference scenes (Shop, Skins, Settings):
    // art fills the original 941x1672 camera; actual tappable controls
    // still fall back to safe-area positioning around physical cutouts.
    private var shopFullBleed=false
    fun useShopFullBleedReferenceCamera() {
        shopFullBleed=true
        requestLayout()
    }
    private var safeLeft=0
    private var safeTop=0
    private var safeRight=0
    private var safeBottom=0

    /**
     * Decorative art is allowed to use the exact full owner reference grid.
     * A native touch target may do so only when its entire reference rectangle
     * fits within the real device's safe window insets. Otherwise it falls
     * back to the previously existing safe-area transform.
     */
    fun place(view:View,x:Float,y:Float,w:Float,h:Float,
              full:Boolean=false,minTouch:Boolean=false,
              referenceAligned:Boolean=false,verticalAnchor:Float=.5f){
        // Default center anchoring leaves every non-Home map unchanged.
        slots.add(Slot(view,RectF(x,y,x+w,y+h),full,minTouch,
            referenceAligned,verticalAnchor.coerceIn(0f,1f)))
        addView(view,ViewGroup.LayoutParams(-2,-2))
    }
    fun setSafe(left:Int,top:Int,right:Int,bottom:Int) {
        if(left!=safeLeft || top!=safeTop || right!=safeRight || bottom!=safeBottom){
            safeLeft=left;safeTop=top;safeRight=right;safeBottom=bottom
            requestLayout()
        }
    }
    private fun transform(width:Int,height:Int):Triple<Float,Float,Float>{
        val availableW=(width-safeLeft-safeRight).coerceAtLeast(1).toFloat()
        val availableH=(height-safeTop-safeBottom).coerceAtLeast(1).toFloat()
        val scale=min(availableW/HomeReferenceLayout.width,
            availableH/HomeReferenceLayout.height)
        return Triple(scale,safeLeft+(availableW-HomeReferenceLayout.width*scale)/2f,
            safeTop+(availableH-HomeReferenceLayout.height*scale)/2f)
    }
    private fun referenceTransform(width:Int,height:Int):Triple<Float,Float,Float>{
        val scale=min(width/HomeReferenceLayout.width,
            height/HomeReferenceLayout.height)
        return Triple(scale,(width-HomeReferenceLayout.width*scale)/2f,
            (height-HomeReferenceLayout.height*scale)/2f)
    }
    private fun placement(slot:Slot,width:Int,height:Int,
                          safe:Triple<Float,Float,Float>,
                          reference:Triple<Float,Float,Float>
    ):Triple<Float,Float,Float>{
        // Keep the illustrated Shop kiosk, its Bopi art, the featured
        // panel and six product cards at the owner's original coordinates.
        // Only actionable native controls fall back into safe insets when
        // an actual display cutout would make the target unreachable.
        if(!slot.referenceAligned && !shopFullBleed)return safe
        val extraHeight=(height-HomeReferenceLayout.height*reference.first)
            .coerceAtLeast(0f)
        val aligned=Triple(reference.first,reference.second,
            extraHeight*slot.verticalAnchor)
        if(!slot.minTouch)return aligned
        val scale=aligned.first
        val x=aligned.second
        val y=aligned.third
        val b=slot.bounds
        val inside=x+b.left*scale>=safeLeft &&
            x+b.right*scale<=width-safeRight &&
            y+b.top*scale>=safeTop &&
            y+b.bottom*scale<=height-safeBottom
        return if(inside)aligned else safe
    }
    override fun onMeasure(widthMeasureSpec:Int,heightMeasureSpec:Int){
        val w=View.MeasureSpec.getSize(widthMeasureSpec)
        val h=View.MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w,h)
        val safe=transform(w,h)
        val reference=referenceTransform(w,h)
        val minimum=(48f*resources.displayMetrics.density).toInt()
        for(slot in slots){
            val (scale,_,_)=placement(slot,w,h,safe,reference)
            val cw=if(slot.full)w else maxOf((slot.bounds.width()*scale).toInt(),
                if(slot.minTouch)minimum else 1)
            val ch=if(slot.full)h else maxOf((slot.bounds.height()*scale).toInt(),
                if(slot.minTouch)minimum else 1)
            slot.view.measure(View.MeasureSpec.makeMeasureSpec(cw,View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(ch,View.MeasureSpec.EXACTLY))
        }
    }
    override fun onLayout(changed:Boolean,left:Int,top:Int,right:Int,bottom:Int){
        val w=right-left
        val h=bottom-top
        val safe=transform(w,h)
        val reference=referenceTransform(w,h)
        for(slot in slots){
            val child=slot.view
            val (scale,ox,oy)=placement(slot,w,h,safe,reference)
            val x=if(slot.full)0 else (ox+slot.bounds.left*scale).toInt()
            val y=if(slot.full)0 else (oy+slot.bounds.top*scale).toInt()
            child.layout(x,y,x+child.measuredWidth,y+child.measuredHeight)
        }
    }
}

