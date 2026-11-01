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

    // The supplied 941×1672 Home illustration places both live balances
    // in the upper right. Never hardcode the illustration's sample numbers.
    val coins=HomeBounds(457f,21f,220f,64f)
    val stars=HomeBounds(705f,21f,215f,64f)
    val logo=HomeBounds(122f,112f,697f,273f)
    val hero=HomeBounds(96f,391f,745f,483f)
    val play=HomeBounds(149f,894f,645f,191f)

    // Two broad native buttons, one centered Settings action, three
    // independently tappable circular shortcuts with captions underneath.
    val skins=HomeBounds(135f,1097f,325f,137f)
    val shop=HomeBounds(476f,1097f,325f,137f)
    val settings=HomeBounds(243f,1252f,453f,121f)
    val achievements=HomeBounds(208f,1452f,166f,142f)
    val leaderboard=HomeBounds(388f,1452f,166f,142f)
    val sound=HomeBounds(568f,1452f,166f,142f)

    val actions=listOf(play,skins,shop,settings,achievements,leaderboard,sound)
    fun supports(pxWidth:Int,pxHeight:Int,portrait:Boolean):Boolean =
        portrait && pxWidth>=860 && pxHeight>=1480

    fun valid():Boolean {
        val all=listOf(coins,stars,logo,hero)+actions
        if(!all.all{it.inside()} || coins.overlaps(stars))return false
        if(coins.bottom>=logo.y || stars.bottom>=logo.y)return false
        if(logo.bottom>=hero.y || hero.bottom>=play.y)return false
        if(skins.y!=shop.y || achievements.y!=leaderboard.y ||
            leaderboard.y!=sound.y || settings.y<=skins.y ||
            settings.bottom>=achievements.y)return false
        for(i in actions.indices)for(j in i+1 until actions.size){
            if(actions[i].overlaps(actions[j]))return false
        }
        return true
    }
}
