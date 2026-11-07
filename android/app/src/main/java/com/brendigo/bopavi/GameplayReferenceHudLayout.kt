package com.brendigo.bopavi

/**
 * 480x800 native gameplay HUD fitted to the reference composition. Currency
 * is real saved coins; the violet badge counts actual crystals in this run,
 * not an invented gem wallet. All controls live in screen coordinates.
 */
internal object GameplayReferenceHudLayout {
    val coinWallet=FlightHudBox(8f,9f,119f,39f)
    val crystalWallet=FlightHudBox(126f,9f,237f,39f)
    val coinPlus=FlightHudBox(96f,11f,117f,37f)
    val crystalPlus=FlightHudBox(214f,11f,235f,37f)
    val score=FlightHudBox(154f,48f,325f,124f)
    val pause=FlightHudBox(426f,10f,475f,63f)

    fun hitArea(pixelX:Float,pixelY:Float,screenW:Int,screenH:Int,
                safeTop:Int):Int {
        if(screenW<=0 || screenH<=0)return 0
        val scale=minOf(screenW/480f,screenH/640f)
        if(scale<=0f)return 0
        val x=(pixelX-(screenW-480f*scale)*.5f)/scale
        val y=(pixelY-safeTop-scale)/scale
        if(pause.encloses(x,y))return 1
        if(coinPlus.encloses(x,y) || crystalPlus.encloses(x,y))return 2
        return 0
    }
}
