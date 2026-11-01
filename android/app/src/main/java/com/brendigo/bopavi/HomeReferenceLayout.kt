package com.brendigo.bopavi

/** The two supplied Home references share a portrait 941×1672 design grid. */
internal data class HomeBounds(val x:Float,val y:Float,val w:Float,val h:Float){
    val right:Float get()=x+w
    val bottom:Float get()=y+h
    fun inside():Boolean=x>=0f && y>=0f && w>0f && h>0f &&
        right<=HomeReferenceLayout.width && bottom<=HomeReferenceLayout.height
    fun overlaps(other:HomeBounds):Boolean=
        x<other.right && right>other.x && y<other.bottom && bottom>other.y
}
internal object HomeReferenceLayout {
    const val width=941f
    const val height=1672f

    // Wallets occupy opposite corners of the image. Values are never baked
    // into the artwork: they come from the user's local ProgressStore.
    val coins=HomeBounds(37f,18f,329f,87f)
    val stars=HomeBounds(607f,18f,299f,87f)
    val logo=HomeBounds(108f,111f,725f,240f)
    val hero=HomeBounds(99f,352f,743f,518f)
    val play=HomeBounds(139f,908f,664f,213f)

    // First row: three individually tappable, glossy embossed controls.
    val skins=HomeBounds(43f,1153f,274f,212f)
    val shop=HomeBounds(334f,1153f,274f,212f)
    val settings=HomeBounds(625f,1153f,273f,212f)
    // Second row: two wide controls in the illustrated owner reference.
    val achievements=HomeBounds(106f,1391f,349f,207f)
    val sound=HomeBounds(484f,1391f,349f,207f)

    val actions=listOf(play,skins,shop,settings,achievements,sound)
    fun supports(pxWidth:Int,pxHeight:Int,portrait:Boolean):Boolean =
        portrait && pxWidth>=860 && pxHeight>=1480

    fun valid():Boolean {
        val all=listOf(coins,stars,logo,hero)+actions
        if(!all.all{it.inside()} || coins.overlaps(stars))return false
        if(logo.bottom>=hero.y || hero.bottom>=play.y)return false
        for(i in actions.indices)for(j in i+1 until actions.size){
            if(actions[i].overlaps(actions[j]))return false
        }
        return true
    }
}
