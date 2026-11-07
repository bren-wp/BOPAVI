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
    // The approved logo including crown, enamel and feathers is higher and
    // slightly wider than the previous reference box. Preserve the SVG
    // aspect ratio inside this box; Home does not stretch logo pixels.
    val logo=HomeBounds(110f,100f,720f,285f)
    // The original Bopi layers use a SQUARE 512x512 transparency grid.
    // Source canvas is square, and the painted alpha is inset. Align its
    // visible upper feathers near reference y≈393, and feet near y≈830.
    // Position the first visible alpha (y≈86 in the original 512px rig)
    // immediately below the winged wordmark, not ~60px below the owner.
    // All three original body/wing sprite layers retain equal square bounds.
    // Source 864px Home: painted Bopi spans roughly x=118..651 and
    // y=365..744. Keep source body and wing layers on a SQUARE canvas.
    val hero=HomeBounds(116f,294f,615f,615f)
    // Extents of alpha>12 in the approved classic 512px body/wing union:
    // x=18..493 and y=86..424. Only painted pixels, not the transparent
    // square canvas, must clear the logo and the native Play touch target.
    val heroArtwork=HomeBounds(
        hero.x+18f*hero.w/512f,hero.y+86f*hero.h/512f,
        475f*hero.w/512f,339f*hero.h/512f)
    // Gold enamel body is x~175..688 on the owner image. The native
    // button remains clickable; the white wings extend outside its body.
    val play=HomeBounds(190f,902f,560f,186f)
    // Visual wings extend beyond the native gold button on both sides.
    val playWings=HomeBounds(112f,904f,719f,171f)

    // Two broad native buttons, one centered Settings action, three
    // independently tappable circular shortcuts with captions underneath.
    val skins=HomeBounds(135f,1097f,325f,137f)
    val shop=HomeBounds(476f,1097f,325f,137f)
    val settings=HomeBounds(243f,1252f,453f,121f)
    val achievements=HomeBounds(208f,1452f,166f,142f)
    val leaderboard=HomeBounds(388f,1452f,166f,142f)
    val sound=HomeBounds(568f,1452f,166f,142f)

    val actions=listOf(play,skins,shop,settings,achievements,leaderboard,sound)
    // Large system text requires the native adaptive Home instead of
    // squeezing labelled controls into the fixed reference artwork grid.
    fun supports(pxWidth:Int,pxHeight:Int,portrait:Boolean,
                 fontScale:Float=1f):Boolean =
        // The reference stage uniformly fits a 720x1280 viewport.
        // This enables the same 3D artwork/controls on compact phones,
        // instead of switching to unrelated portrait/button art.
        // Truly small devices retain the native scroll-safe composition.
        portrait && pxWidth>=720 && pxHeight>=1280 &&
        fontScale>=.85f && fontScale<1.3f

    fun valid():Boolean {
        val all=listOf(coins,stars,logo,hero)+actions
        if(!all.all{it.inside()} || !playWings.inside() ||
            coins.overlaps(stars))return false
        if(playWings.x>=play.x || playWings.right<=play.right ||
            playWings.y<play.y || playWings.bottom>play.bottom)return false
        if(coins.bottom>=logo.y || stars.bottom>=logo.y)return false
        if(!heroArtwork.inside() || logo.bottom>=heroArtwork.y ||
            heroArtwork.bottom>=play.y)return false
        if(skins.y!=shop.y || achievements.y!=leaderboard.y ||
            leaderboard.y!=sound.y || settings.y<=skins.y ||
            settings.bottom>=achievements.y)return false
        for(i in actions.indices)for(j in i+1 until actions.size){
            if(actions[i].overlaps(actions[j]))return false
        }
        return true
    }
}
