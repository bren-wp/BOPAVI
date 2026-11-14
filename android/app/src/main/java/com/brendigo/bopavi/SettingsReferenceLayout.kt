package com.brendigo.bopavi

/** Approved 941 × 1672 Settings reference measurement grid. */
internal object SettingsReferenceLayout {
    const val width=941f
    const val height=1672f
    // The owner art separates the live settings from two legal routes by a
    // Nine 80px owner rows with visible enamel/cream gutters and a carved
    // legal divider after Cloud Save, measured on the 941x1672 reference.
    // Physical hit targets remain native and accessibility-safe.
    // Each native touch target must remain at least 48dp tall; the approved
    // decorative image's tighter spacing is not a safe hit target at 320dpi.
    val rows=floatArrayOf(610f,706f,802f,898f,994f,1090f,1186f,1282f,1378f)
    const val rowHeight=96f
    const val left=137f
    const val rowWidth=667f
    const val homeTop=1486f

    // The owner artwork is not vertically centered as a SINGLE block on
    // tall 19:9 and 20:9 phones. Measure each group independently against
    // real 1080×2400 captures; keep native controls and 48dp hit targets.
    const val logoAnchor=.08f
    const val heroAnchor=.05f
    const val backAnchor=.32f
    const val titleAnchor=.20f
    const val contentAnchor=.14f
    const val restoreAnchor=.62f

    /**
     * Native Settings composition uses the full 941×1672 reference camera.
     * The owner's 20:9 artwork spreads the nine enamel rows and grows the
     * carved frame downward rather than letterboxing an immutable panel.
     * Only the extra physical viewport height drives the adjustment; in a
     * 16:9 camera the original measured locations are unchanged.
     */
    private fun extraPx(screenW:Int,screenH:Int):Float {
        if(screenW<=0 || screenH<=0)return 0f
        val scale=minOf(screenW/width,screenH/height)
        return (screenH-height*scale).coerceAtLeast(0f)
    }
    private fun cameraScale(screenW:Int,screenH:Int):Float=
        minOf(screenW/width,screenH/height).coerceAtLeast(.001f)
    fun rowOffset(index:Int,screenW:Int,screenH:Int):Float {
        require(index in rows.indices)
        // Native row 1 was ~30px too high; row 9 ~150px too high
        // in the real 1080×2400 capture. Keep each item interactive.
        return (.06f+.0325f*index)*extraPx(screenW,screenH)/
            cameraScale(screenW,screenH)
    }
    fun panelOffset(screenW:Int,screenH:Int):Float=
        .06f*extraPx(screenW,screenH)/cameraScale(screenW,screenH)
    fun panelExtra(screenW:Int,screenH:Int):Float=
        .57f*extraPx(screenW,screenH)/cameraScale(screenW,screenH)
    fun legalSeparatorOffset(screenW:Int,screenH:Int):Float=
        rowOffset(6,screenW,screenH)

    fun supportsNativeTouchTargets(screenW:Int,screenH:Int,density:Float):Boolean {
        if(screenW<=0 || screenH<=0 || density<=0f)return false
        val scale=minOf(screenW/width,screenH/height)
        val minimum=48f*density
        return rowHeight*scale+0.001f>=minimum &&
            (rows[1]-rows[0])*scale+0.001f>=minimum
    }
    /**
     * The owner-supplied 1080p/QHD Settings mockups use 96px rows on the
     * 941×1672 camera. At 420/560dpi this is slightly shorter than 48dp,
     * even though the entire nine-row composition comfortably fits.
     *
     * The native reference stage *expands* touch bounds to 48dp. Allow
     * its bounded <=16% vertical overlap on standard owner-sized portrait
     * canvases rather than substituting the unrelated compact ScrollView.
     * Larger fonts, truly small screens or excessive overlap retain the
     * scrollable layout. This is a visual compromise, not certified 1:1.
     */
    fun supportsDenseOwnerCamera(screenW:Int,screenH:Int,density:Float):Boolean {
        if(screenW<860 || screenH<1480 || density<=0f)return false
        val scale=minOf(screenW/width,screenH/height)
        val pitch=(rows[1]-rows[0])*scale
        val touch=48f*density
        if(touch>pitch*1.16f)return false
        // The bottom restore action must remain on-screen once expanded.
        val homeBottom=homeTop+112f
        val restoreHeight=maxOf(112f*scale,touch)
        return homeTop*scale+restoreHeight<=screenH &&
            homeBottom*scale<=screenH
    }
    fun fits():Boolean = rows.size==9 &&
        (0 until rows.lastIndex).all{rows[it+1]-rows[it]>=rowHeight} &&
        rows.last()+rowHeight<homeTop &&
        homeTop+112f<=height && left+rowWidth<=width
}
