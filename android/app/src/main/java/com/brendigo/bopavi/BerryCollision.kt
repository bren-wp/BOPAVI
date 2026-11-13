package com.brendigo.bopavi


internal object BerryCollision {
    fun touched(startX:Float,startY:Float,endX:Float,endY:Float,contactRadius:Float):Boolean {
        if(!startX.isFinite() || !startY.isFinite() || !endX.isFinite() ||
            !endY.isFinite() || !contactRadius.isFinite() || contactRadius<=0f) return false
        val dx=endX-startX
        val dy=endY-startY
        val lengthSquared=dx*dx+dy*dy
        val t=if(lengthSquared>0f) ((-startX*dx-startY*dy)/lengthSquared).coerceIn(0f,1f) else 0f
        val nearestX=startX+dx*t
        val nearestY=startY+dy*t
        return nearestX*nearestX+nearestY*nearestY<=contactRadius*contactRadius
    }
}
