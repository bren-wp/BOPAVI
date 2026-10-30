package com.brendigo.bopavi

/** Progress-derived goals, not spendable or paid rewards. */
internal data class AchievementSnapshot(
    val wins:Int, val runs:Int, val flaps:Long, val food:Int,
    val obstacles:Int, val score:Int, val meadows:Int, val deaths:Int
)
internal data class AchievementGoal(
    val id:String, val title:String, val detail:String,
    val current:Long, val target:Long, val icon:String
) {
    val achieved:Boolean get()=current>=target
    val percent:Int get()=(current.coerceIn(0L,target)*100L/target).toInt()
    val accessibility:String get()=
        "$title: $current of $target. "+(if(achieved)"Goal reached" else "In progress")
}
internal object AchievementCatalog {
    fun goals(s:AchievementSnapshot):List<AchievementGoal> = listOf(
        AchievementGoal("win1","FIRST VICTORY","Win a finite level",s.wins.toLong(),1,"STAR"),
        AchievementGoal("runs10","SKY EXPLORER","10 endless runs",s.runs.toLong(),10,"FLIGHT"),
        AchievementGoal("flaps100","WING MASTER","100 flaps",s.flaps,100,"WING"),
        AchievementGoal("food25","BERRY HUNTER","25 berries in one flight",s.food.toLong(),25,"BERRY"),
        AchievementGoal("gates20","GATE RUNNER","20 obstacles in a flight",s.obstacles.toLong(),20,"GATE"),
        AchievementGoal("score1000","HIGH FLYER","Best score 1,000",s.score.toLong(),1000,"RECORDS"),
        AchievementGoal("meadow10","MEADOW FINDER","10 pickups in Sky Meadows",s.meadows.toLong(),10,"COIN"),
        AchievementGoal("deaths5","KEEP FLYING","5 failed finished flights",s.deaths.toLong(),5,"RESTART")
    )
}
