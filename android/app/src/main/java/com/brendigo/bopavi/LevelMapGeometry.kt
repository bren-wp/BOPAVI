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
    // Native accessible nodes follow the curving rope bridge on Sky Meadows.
    val nodes=listOf(
        Point(88f,1112f),
        Point(257f,1224f),
        Point(428f,1132f),
        Point(602f,1170f),
        Point(768f,1012f),
        Point(603f,865f)
    )
    val plaques=listOf(
        Point(65f,764f),    // Sky Meadows (green lower island)
        Point(626f,581f),   // Crystal Ruins (middle-right)
        Point(192f,149f)    // Volcano Peaks (top)
    )

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
