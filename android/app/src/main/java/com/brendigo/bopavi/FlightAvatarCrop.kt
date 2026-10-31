package com.brendigo.bopavi

/**
 * Pure Kotlin crop coordinates for JVM regression tests. Android Canvas Rect
 * is constructed once by GameView; no Android graphics stubs in local tests.
 */
internal data class FlightAvatarBox(
    val left:Int,val top:Int,val right:Int,val bottom:Int
) {
    val width:Int get()=right-left
    val height:Int get()=bottom-top
}
internal object FlightAvatarCrop {
    val source=FlightAvatarBox(88,62,424,398)
}
