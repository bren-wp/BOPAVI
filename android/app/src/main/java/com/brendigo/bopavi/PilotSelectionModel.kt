package com.brendigo.bopavi

/**
 * Four real BOPAVI appearances: the three legacy playable pilots keep their
 * save IDs, while the fourth uses the existing Orion cosmetic (skin index 8).
 * Orion becomes available ONLY after the legitimate local-coin purchase has
 * been recorded by ProgressStore. Nothing is a fake gallery tile.
 */
internal object PilotSelectionModel {
    data class Pilot(val id:Int,val title:String,val shortName:String,
                     val ability:PilotAbility)
    val pilots=listOf(
        Pilot(0,"CLASSIC AVIATOR","BOPI",PilotAbility.BOPI),
        Pilot(1,"SKY OWL","SOVA",PilotAbility.SOVA),
        Pilot(2,"SKY FOX","LISAC",PilotAbility.LISAC),
        Pilot(3,"STAR PILOT","ORION",PilotAbility.BOPI)
    )
    // Legacy enum SOVA and sprite id 9 are preserved for saved games;
    // the owner-facing aviator is presented as SOVA / SKY OWL.
    // Sprite IDs differ from the three selectable pilot indices.
    // Splash, Home and Flight must resolve the very same playable character.
    fun spriteIdForChoice(index:Int):Int = when(safeChoice(index)){
        0 -> 0
        1 -> 9
        2 -> 10
        else -> 8 // Orion: existing collectible skin, not a fabricated sprite
    }
    fun safeChoice(index:Int):Int=index.coerceIn(0,3)
    fun selected(index:Int,candidate:Int):Boolean = safeChoice(index)==candidate
    fun locked(candidate:Int,orionOwned:Boolean):Boolean =
        candidate==3 && !orionOwned
    fun accessibility(index:Int,candidate:Int,orionOwned:Boolean=true):String =
        if(locked(candidate,orionOwned))"Locked STAR PILOT. Unlock with earned coins"
        else (if(selected(index,candidate))"Selected " else "Choose ")+pilots[candidate].title
    fun priceLabel():String="THREE FREE PILOTS • STAR PILOT EARNED WITH COINS"
}
