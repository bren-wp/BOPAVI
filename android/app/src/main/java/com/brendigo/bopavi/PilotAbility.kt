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
    val gravityMultiplier:Float=1f,
    val terminalFallSpeed:Float=365f
) {
    CLASSIC("Klasični let","Standardne kontrole"),
    BOPI("Brzi let","+6 % brzine; brži prolazak uz kraće vrijeme reakcije",1.06f),
    SOVA("Mirna krila","8 % slabija gravitacija i do 50 jedinica sporiji pad",
        gravityMultiplier=.92f,terminalFallSpeed=315f),
    LISAC("Zaštitni štit","Prvi udarac blokiran jednom tijekom leta");

    fun hudStatus(guardReady:Boolean):String = when(this) {
        BOPI -> "BOPI · BRZI LET"
        SOVA -> "SOVA · MIRNA KRILA"
        LISAC -> if(guardReady)"LISAC · ŠTIT SPREMAN" else "LISAC · ŠTIT POTROŠEN"
        CLASSIC -> "KLASIČNI LET"
    }

    /** Meaningful state colour for a small low-overhead Canvas HUD chip. */
    fun hudTint(guardReady:Boolean):Int = when(this) {
        BOPI -> 0xffa5e6ff.toInt()
        SOVA -> 0xffd9c8ff.toInt()
        LISAC -> if(guardReady)0xff9df6c8.toInt() else 0xffffb4be.toInt()
        CLASSIC -> 0xffffe7a1.toInt()
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
