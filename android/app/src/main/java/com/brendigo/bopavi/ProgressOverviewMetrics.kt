package com.brendigo.bopavi

/**
 * The Progress screen reports only genuine saved values. Every world may
 * contain up to 1,048,576 levels; the visible 60-star milestone deliberately
 * covers the first twenty levels only, never the entire world's completion.
 */
internal object ProgressOverviewMetrics {
    private val worldNames=listOf(
        "SKY MEADOWS","CRYSTAL RUINS","VOLCANO PEAKS","NIGHT CASTLES",
        "PEARL SEA","SKY TOWERS","MAGIC FOREST","STAR ABYSS"
    )

    fun worldName(world:Int):String = worldNames[world.coerceIn(0,worldNames.lastIndex)]
    fun completedLevels(frontier:Int):Int =
        (frontier.toLong()-1L).coerceIn(0L,LevelEngine.LEVELS_PER_WORLD.toLong()).toInt()

    fun firstTwentyStarPercent(earned:Int):Int =
        (earned.coerceIn(0,60)*100)/60

    fun detail(world:Int,frontier:Int,earned:Int):String {
        val complete=completedLevels(frontier)
        val stars=earned.coerceIn(0,60)
        return "${worldName(world)}, ${complete} completed levels, " +
            "${stars} of 60 possible stars on the first 20 levels"
    }
}
