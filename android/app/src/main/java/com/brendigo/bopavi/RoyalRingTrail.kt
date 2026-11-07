package com.brendigo.bopavi

/** Golden ring sequence leading into a genuine Sky Meadows gate.
 *
 * The same pure geometry is used by the Canvas renderer and swept gameplay
 * collection. The ring art never creates an untouchable fake collectible:
 * every visible ring is backed by one persisted-within-run collection bit.
 * These rings supplement the existing central gate coin and do not affect
 * the obstacle collision shape or the player's 18px physical body radius.
 */
internal object RoyalRingTrail {
    const val count=6
    const val leadingDistance=165f
    const val spacing=24f
    const val contactRadius=13f

    fun enabled(gate:LevelEngine.Gate):Boolean=gate.kind==0 && gate.coin

    fun x(gateScreenX:Float,ringIndex:Int):Float {
        require(ringIndex in 0 until count)
        return gateScreenX-leadingDistance+ringIndex*spacing
    }

    fun y(opening:LevelEngine.Opening,ringIndex:Int):Float {
        require(ringIndex in 0 until count)
        val mid=(opening.top+opening.bottom)*.5f
        val offset=when(ringIndex){
            0 -> 55f
            1 -> 38f
            2 -> 21f
            3 -> 4f
            4 -> -13f
            else -> -30f
        }
        // Preserve a safe 24px margin from the moving cap; a reward must
        // remain within the real opening at each animation phase.
        return (mid+offset).coerceIn(opening.top+24f,opening.bottom-24f)
    }
}
