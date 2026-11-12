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
    // The approved logo including crown, enamel and feathers is higher and
    // slightly wider than the previous reference box. Preserve the SVG
    // aspect ratio inside this box; Home does not stretch logo pixels.
    val logo=HomeBounds(72f,72f,798f,351f)
    // Source logo PNG is 780x325; its real nontransparent pixels occupy
    // x=57..723 and y=34..294. The 941px reference must compare PAINTED
    // logo with Bopi, not the unused transparent bleed in its ImageView.
    private val logoScale=minOf(logo.w/780f,logo.h/325f)
    val logoArtwork=HomeBounds(
        logo.x+(logo.w-780f*logoScale)/2f+57f*logoScale,
        logo.y+(logo.h-325f*logoScale)/2f+34f*logoScale,
        666f*logoScale,260f*logoScale)
    // The original Bopi layers use a SQUARE 512x512 transparency grid.
    // Source canvas is square, and the painted alpha is inset. Align its
    // visible upper feathers near reference y≈393, and feet near y≈830.
    // Position the first visible alpha (y≈86 in the original 512px rig)
    // immediately below the winged wordmark, not ~60px below the owner.
    // All three original body/wing sprite layers retain equal square bounds.
    // Source 864px Home: painted Bopi spans roughly x=118..651 and
    // y=365..744. Keep source body and wing layers on a SQUARE canvas.
    val hero=HomeBounds(80f,274f,680f,680f)
    // Extents of alpha>12 in the approved classic 512px body/wing union:
    // x=18..493 and y=86..424. Only painted pixels, not the transparent
    // square canvas, must clear the logo and the native Play touch target.
    val heroArtwork=HomeBounds(
        hero.x+18f*hero.w/512f,hero.y+86f*hero.h/512f,
        475f*hero.w/512f,339f*hero.h/512f)
    // Three interactive entry points, without Play or legacy score screens.
    val skins=HomeBounds(135f,965f,325f,137f)
    val shop=HomeBounds(476f,965f,325f,137f)
    val settings=HomeBounds(243f,1122f,453f,121f)
    val actions=listOf(skins,shop,settings)
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
        val all=listOf(coins,logo,logoArtwork,hero,heroArtwork)+actions
        if(!all.all{it.inside()})return false
        if(coins.bottom>=logoArtwork.y)return false
        if(logoArtwork.bottom>=heroArtwork.y || heroArtwork.bottom>=skins.y)return false
        if(skins.y!=shop.y || settings.y<=skins.y || settings.y<=shop.bottom)
            return false
        for(i in actions.indices)for(j in i+1 until actions.size){
            if(actions[i].overlaps(actions[j]))return false
        }
        return true
    }
}
