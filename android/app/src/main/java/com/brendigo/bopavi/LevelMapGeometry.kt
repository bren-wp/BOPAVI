package com.brendigo.bopavi

/**
 * Pixel-coordinate layout measured against the owner's 941x1672 map.
 * Small windows letterbox this composition without distorting the artwork.
 * Actual buttons retain at least a 48dp native touch target.
 */
internal object LevelMapGeometry {
    const val width=941f
    const val height=1672f
    const val starsPerWorld=60

    data class Point(val x:Float,val y:Float)
    // Measured reference centers: three stepping stones on Sky Meadows,
    // two in Crystal Ruins and one on Volcano Peaks (not six on one island).
    // Nodes are actual selected-world stages, so disabled = truly locked.
    val nodes=listOf(
        Point(45f,969f),     // Meadow blue stage 1
        Point(129f,1117f),   // Meadow blue stage 2
        Point(299f,1107f),   // Meadow golden stage 3
        Point(421f,596f),    // Crystal stage 4
        Point(533f,681f),    // Crystal stage 5
        Point(508f,207f)     // Volcano locked stage 6
    )
    val plaques=listOf(
        Point(49f,739f),    // Sky Meadows wooden sign (lower-left)
        Point(642f,572f),   // Crystal Ruins stone sign (middle-right)
        Point(185f,133f)    // Volcano Peaks lava sign (upper-left)
    )
    const val nodeWidth=108f
    const val nodeHeight=107f
    fun starStrip(index:Int):Point {
        require(index in 0 until 3)
        val p=nodes[index]
        return Point(p.x-24f,p.y+nodeHeight-6f)
    }
    fun nodesInsideReference():Boolean=nodes.all{
        it.x>=0f && it.y>=0f &&
        it.x+nodeWidth<=width && it.y+nodeHeight<=height
    }
    fun starStripsInsideReference():Boolean=(0..2).all{
        val p=starStrip(it)
        p.x>=0f && p.y>=0f && p.x+156f<=width && p.y+42f<=height
    }
    fun firstThreeMarkersAndStarsDoNotOverlap():Boolean=(0..2).all{index->
        val strip=starStrip(index)
        val starRight=strip.x+156f
        val starBottom=strip.y+42f
        (0..2).filter{it!=index}.all{other->
            val marker=nodes[other]
            val separate=starRight<=marker.x ||
                strip.x>=marker.x+nodeWidth ||
                starBottom<=marker.y ||
                strip.y>=marker.y+nodeHeight
            separate
        }
    }

    fun pageStart(selected:Int):Int=(selected.coerceIn(0,7)/3)*3
    fun displayedWorlds(selected:Int):List<Int> {
        val start=pageStart(selected)
        return (start until minOf(start+3,LevelEngine.WORLD_COUNT)).toList()
    }
    fun nearestLevels(frontier:Int):List<Int> {
        val cap=LevelEngine.LEVELS_PER_WORLD
        val safe=frontier.coerceIn(1,cap)
        val first=(safe-2).coerceAtLeast(1).coerceAtMost(cap-5)
        return (first..first+5).toList()
    }
    fun playableFrontier(frontier:Int):Int=frontier.coerceIn(1,LevelEngine.LEVELS_PER_WORLD)
    fun isUnlocked(level:Int,frontier:Int):Boolean =
        level in 1..LevelEngine.LEVELS_PER_WORLD && level<=frontier
}
