package com.brendigo.bopavi

/** Measured on the owner's actual BOPAVI Home image (941×1672). */
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

    // Artwork remains separately layered. Coins and stars are actual saved
    // counters, not the 1,250 / 85 numbers in the concept screenshot.
    val coins=HomeBounds(452f,20f,215f,82f)
    val stars=HomeBounds(684f,20f,219f,82f)
    val logo=HomeBounds(127f,123f,686f,208f) // 780:236 original PNG
    val hero=HomeBounds(139f,367f,663f,500f)
    val play=HomeBounds(153f,891f,637f,182f)
    val skins=HomeBounds(136f,1090f,323f,138f)
    val shop=HomeBounds(478f,1090f,329f,138f)
    val settings=HomeBounds(248f,1245f,445f,122f)
    val achievements=HomeBounds(176f,1440f,195f,149f)
    val leaderboard=HomeBounds(379f,1440f,198f,149f)
    val sound=HomeBounds(588f,1440f,182f,149f)

    val actions=listOf(play,skins,shop,settings,achievements,leaderboard,sound)
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
