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
    // Measured from the owner-supplied 1080×1920 screenshot, normalized
    // onto the established 941×1672 native camera. The controls are an
    // actual VERTICAL stack: blue SKINS, gold SHOP, violet SETTINGS.
    // The obsolete side-by-side arrangement was visually incorrect.
    val skins=HomeBounds(266f,1025f,410f,122f)
    val shop=HomeBounds(265f,1182f,411f,119f)
    val settings=HomeBounds(264f,1332f,412f,117f)
    val actions=listOf(skins,shop,settings)

    /**
     * The same owner package depicts a ~19% larger button stack for the
     * 1080×2280, 1080×2340, 1080×2400 and 1440×3040 aspect ratios.
     * Enlarge about the REFERENCE CAMERA center, not the top of the page.
     * Interpolate for intermediate aspect ratios instead of jumping when
     * a handset crosses a device-model breakpoint.
     */
    fun actionScale(pxWidth:Int,pxHeight:Int):Float {
        if(pxWidth<=0 || pxHeight<=0)return 1f
        val aspect=pxHeight.toFloat()/pxWidth
        val mix=((aspect-16f/9f)/(19f/9f-16f/9f)).coerceIn(0f,1f)
        return 1f+.19f*mix
    }
    fun actionBounds(pxWidth:Int,pxHeight:Int):List<HomeBounds> {
        val multiplier=actionScale(pxWidth,pxHeight)
        val centerX=width/2f
        val centerY=height/2f
        return actions.map { b ->
            HomeBounds(centerX+(b.x-centerX)*multiplier,
                centerY+(b.y-centerY)*multiplier,
                b.w*multiplier,b.h*multiplier)
        }
    }

    /** Accessible controls must not silently stretch below 48dp.
     * For too-small/high-density windows we use native compact controls,
     * not a clipped imitation of the reference camera.
     */
    fun supports(pxWidth:Int,pxHeight:Int,portrait:Boolean,
                 fontScale:Float=1f,density:Float=2f):Boolean {
        if(!portrait || pxWidth<720 || pxHeight<1280 ||
            fontScale<.85f || fontScale>=1.3f || density<=0f)return false
        val scale=minOf(pxWidth/width,pxHeight/height)
        val targets=actionBounds(pxWidth,pxHeight)
        val minTouch=48f*density
        if(targets.any { it.w*scale<minTouch || it.h*scale<minTouch })return false
        return targets.all { it.inside() } &&
            targets.indices.all { i ->
                ((i+1) until targets.size).all { j ->
                    !targets[i].overlaps(targets[j])
                }
            }
    }

    fun valid():Boolean {
        val all=listOf(coins,logo,logoArtwork,hero,heroArtwork)+actions
        if(!all.all{it.inside()})return false
        if(coins.bottom>=logoArtwork.y)return false
        if(logoArtwork.bottom>=heroArtwork.y || heroArtwork.bottom>=skins.y)return false
        if(skins.bottom>=shop.y || shop.bottom>=settings.y ||
            kotlin.math.abs(skins.x+skins.w/2f-width/2f)>3f ||
            kotlin.math.abs(shop.x+shop.w/2f-width/2f)>3f ||
            kotlin.math.abs(settings.x+settings.w/2f-width/2f)>3f)
            return false
        for(i in actions.indices)for(j in i+1 until actions.size){
            if(actions[i].overlaps(actions[j]))return false
        }
        return true
    }
}
