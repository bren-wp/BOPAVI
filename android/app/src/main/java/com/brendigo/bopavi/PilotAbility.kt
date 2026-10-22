package com.brendigo.bopavi

/**
 * Three real offline pilot skills, selectable without payments or unlocks.
 * CLASSIC is the unchanged compatibility mode for legacy gameplay tests and
 * callers that do not explicitly choose an arcade pilot.
 */
enum class PilotAbility(
    val shortTitle:String,
    val description:String,
    val speedMultiplier:Float=1f,
    val gravityMultiplier:Float=1f
) {
    CLASSIC("Klasični let","Standardne kontrole"),
    BOPI("Brzi let","Brži prolazak kroz prepreke",1.06f),
    SOVA("Mirna krila","Sporije propadanje između zamaha",gravityMultiplier=.92f),
    LISAC("Zaštitni štit","Jednom tijekom leta blokira udarac");

    fun hudStatus(guardReady:Boolean):String = when(this) {
        BOPI -> "BOPI · BRZI LET"
        SOVA -> "SOVA · MIRNA KRILA"
        LISAC -> if(guardReady)"LISAC · ŠTIT SPREMAN" else "LISAC · ŠTIT POTROŠEN"
        CLASSIC -> "KLASIČNI LET"
    }

    companion object {
        fun fromChoice(choice:Int):PilotAbility = when(choice) {
            0 -> BOPI
            1 -> SOVA
            2 -> LISAC
            else -> BOPI
        }
    }
}
