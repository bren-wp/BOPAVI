package com.brendigo.bopavi

/**
 * Three real BOPAVI gameplay pilots. This UI does NOT sell or unlock them:
 * all three have always been available. Historical cosmetic skin purchases
 * remain stored separately in ProgressStore, never overwritten here.
 */
internal object PilotSelectionModel {
    data class Pilot(val id:Int,val title:String,val shortName:String,
                     val ability:PilotAbility)
    val pilots=listOf(
        Pilot(0,"CLASSIC AVIATOR","BOPI",PilotAbility.BOPI),
        Pilot(1,"WISE OWL","SOVA",PilotAbility.SOVA),
        Pilot(2,"SKY FOX","LISAC",PilotAbility.LISAC)
    )
    fun safeChoice(index:Int):Int=index.coerceIn(0,2)
    fun selected(index:Int,candidate:Int):Boolean = safeChoice(index)==candidate
    fun accessibility(index:Int,candidate:Int):String =
        (if(selected(index,candidate))"Selected " else "Choose ")+pilots[candidate].title
    fun priceLabel():String="FREE • UNLOCKED"
}
