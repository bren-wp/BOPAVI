package com.brendigo.bopavi

/**
 * Stone and golden cone decoration for Sky Meadows' lower ground.
 *
 * The real player boundary is y=753 in GameSimulation: Bopi's centre cannot
 * cross y=753-radius. Never render a deadly-looking spike into the playable
 * corridor while retaining a different physical hitbox. Everything returned
 * here is wholly inside the EXISTING collision-solid lower half-plane.
 *
 * Units are the unchanged 480×800 logical gameplay camera, not screen dp.
 */
internal object FlightGroundSpikeGeometry {
    const val solidFloorTop = 753f
    const val cameraBottom = 800f
    const val firstCenterX = 142f
    const val centerStep = 55f
    const val spikeCount = 5
    const val spikeHalfWidth = 13f
    const val stoneTop = 783f
    const val spineBottom = 788f

    fun centerX(index:Int):Float {
        require(index in 0 until spikeCount)
        return firstCenterX+index*centerStep
    }

    fun tipY(index:Int):Float {
        require(index in 0 until spikeCount)
        return solidFloorTop+if(index%2==0)0f else 2f
    }

    fun completelyWithinSolidFloor():Boolean {
        for(i in 0 until spikeCount) {
            val x=centerX(i)
            if(x-spikeHalfWidth<0f || x+spikeHalfWidth>480f) return false
            if(tipY(i)<solidFloorTop || tipY(i)>=spineBottom) return false
        }
        return stoneTop>=solidFloorTop && spineBottom<=cameraBottom
    }
}
