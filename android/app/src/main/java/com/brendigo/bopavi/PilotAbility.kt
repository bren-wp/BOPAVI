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

    companion object {
        fun fromChoice(choice:Int):PilotAbility = when(choice) {
            0 -> BOPI
            1 -> SOVA
            2 -> LISAC
            else -> BOPI
        }
    }
}
