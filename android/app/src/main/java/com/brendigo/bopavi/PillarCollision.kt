package com.brendigo.bopavi

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal object PillarCollision {
    fun bodyHitsPillar(birdX:Float,birdY:Float,radius:Float,gateX:Float,
                       gateWidth:Float,opening:LevelEngine.Opening):Boolean {
        if (!birdX.isFinite() || !birdY.isFinite() || !radius.isFinite() ||
            !gateX.isFinite() || !gateWidth.isFinite() || !opening.top.isFinite() ||
            !opening.bottom.isFinite() || radius<=0f || gateWidth<=0f || opening.top>=opening.bottom) return false
        val right=gateX+gateWidth
        return touchesRectangle(birdX,birdY,radius,gateX,-25f,right,opening.top) ||
            touchesRoundedBand(birdX,birdY,radius,gateX-7f,opening.top-28f,right+5f,opening.top) ||
            touchesRectangle(birdX,birdY,radius,gateX,opening.bottom,right,825f) ||
            touchesRoundedBand(birdX,birdY,radius,gateX-7f,opening.bottom,right+5f,opening.bottom+27f)
    }
    /** Conservative continuous circle sweep against translating/animated rock.
     * Shape movement is linearly interpolated *within* the <=120Hz physics slice.
     * Endpoint-only collision misses shallow cap-corner chords. Interval bounds
     * avoid sampling-only tunneling without expanding the r18 body.
     */
    fun sweptBodyHitsPillar(birdX:Float,fromY:Float,toY:Float,radius:Float,
                            fromGateX:Float,toGateX:Float,gateWidth:Float,
                            fromOpening:LevelEngine.Opening,toOpening:LevelEngine.Opening):Boolean {
        if(!birdX.isFinite() || !fromY.isFinite() || !toY.isFinite() ||
            !radius.isFinite() || radius<=0f || !fromGateX.isFinite() ||
            !toGateX.isFinite() || !gateWidth.isFinite() || gateWidth<=0f ||
            !fromOpening.top.isFinite() || !fromOpening.bottom.isFinite() ||
            !toOpening.top.isFinite() || !toOpening.bottom.isFinite() ||
            fromOpening.top>=fromOpening.bottom || toOpening.top>=toOpening.bottom) return false
        // A stationary tangency must not recursively subdivide 2^20+ times.
        // With no relative movement, the single-frame predicate is exact.
        if (fromY == toY && fromGateX == toGateX &&
            fromOpening.top == toOpening.top &&
            fromOpening.bottom == toOpening.bottom) {
            return bodyHitsPillar(birdX, fromY, radius, fromGateX, gateWidth, fromOpening)
        }
        // All four actual shapes lie in this swept horizontal extent.
        if(birdX<min(fromGateX,toGateX)-7f-radius ||
           birdX>max(fromGateX,toGateX)+gateWidth+5f+radius)return false
        // Conservative swept vertical broad phase: interpolation of both
        // bird centre and animated mouth stays inside this intersection.
        // Neither rounded lip nor shaft can touch the radius-r body here.
        // Improves CPU/frame pacing without changing near-miss/hit semantics.
        val safeTop=max(fromOpening.top,toOpening.top)+radius
        val safeBottom=min(fromOpening.bottom,toOpening.bottom)-radius
        if(fromY>=safeTop && fromY<=safeBottom &&
           toY>=safeTop && toY<=safeBottom)return false
        val motionBound=abs(toGateX-fromGateX)+abs(toY-fromY)+
            max(abs(toOpening.top-fromOpening.top),abs(toOpening.bottom-fromOpening.bottom))+.0001f
        fun clear(t:Float):Float {
            val x=fromGateX+(toGateX-fromGateX)*t
            val y=fromY+(toY-fromY)*t
            val top=fromOpening.top+(toOpening.top-fromOpening.top)*t
            val bottom=fromOpening.bottom+(toOpening.bottom-fromOpening.bottom)*t
            val right=x+gateWidth
            return min(min(
                signedRect(y=y,cx=birdX,left=x,top=-25f,right=right,bottom=top),
                signedRound(birdX,y,x-7f,top-28f,right+5f,top)),
                min(signedRect(y=y,cx=birdX,left=x,top=bottom,right=right,bottom=825f),
                    signedRound(birdX,y,x-7f,bottom,right+5f,bottom+27f))) - radius
        }
        fun search(a:Float,da:Float,b:Float,db:Float,depth:Int):Boolean {
            if(da<0f || db<0f)return true
            // A signed distance to a moving convex shape is Lipschitz;
            // min across four shapes keeps the same conservative bound.
            if(da+db>motionBound*(b-a))return false
            if(depth>=20)return false // <0.00001 logical-unit unresolved chord
            val mid=(a+b)*.5f
            val dm=clear(mid)
            if(dm<0f)return true
            return search(a,da,mid,dm,depth+1) || search(mid,dm,b,db,depth+1)
        }
        return search(0f,clear(0f),1f,clear(1f),0)
    }
    private fun signedRect(y:Float,cx:Float,left:Float,top:Float,
                           right:Float,bottom:Float):Float {
        val dx=abs(cx-(left+right)*.5f)-(right-left)*.5f
        val dy=abs(y-(top+bottom)*.5f)-(bottom-top)*.5f
        return kotlin.math.hypot(max(0f,dx),max(0f,dy))+min(max(dx,dy),0f)
    }
    private fun signedRound(cx:Float,cy:Float,left:Float,top:Float,
                            right:Float,bottom:Float):Float {
        val qx=abs(cx-(left+right)*.5f)-((right-left)*.5f-7f)
        val qy=abs(cy-(top+bottom)*.5f)-((bottom-top)*.5f-7f)
        return kotlin.math.hypot(max(0f,qx),max(0f,qy))+min(max(qx,qy),0f)-7f
    }
    /** Exact circle vs 7px-radius painted lip, not its transparent AABB corners. */
    private fun touchesRoundedBand(cx:Float,cy:Float,r:Float,left:Float,top:Float,
                                   right:Float,bottom:Float):Boolean {
        val corner=7f
        val hw=(right-left)*.5f; val hh=(bottom-top)*.5f
        val qx=abs(cx-(left+right)*.5f)-(hw-corner)
        val qy=abs(cy-(top+bottom)*.5f)-(hh-corner)
        return kotlin.math.hypot(max(0f,qx),max(0f,qy))+min(max(qx,qy),0f)-corner<r
    }
    private fun touchesRectangle(cx:Float,cy:Float,r:Float,left:Float,top:Float,
                                 right:Float,bottom:Float):Boolean {
        val dx=maxOf(0f,left-cx,cx-right)
        val dy=maxOf(0f,top-cy,cy-bottom)
        return dx*dx+dy*dy < r*r
    }
}

/** Segment-to-point contact of a moving berry relative to Bopi's body.
 * No heap allocations, sprite bounds, magnetic pickup or virtual currency.
 */
