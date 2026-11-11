package com.brendigo.bopavi

/** Approved 941 × 1672 Settings reference measurement grid. */
internal object SettingsReferenceLayout {
    const val width=941f
    const val height=1672f
    // The owner art separates the live settings from two legal routes by a
    // thin carved divider. Keep nine true 98px rows non-overlapping; a full
    // 48dp hit target remains reachable even on the 941px portrait camera.
    val rows=floatArrayOf(620f,718f,816f,914f,1012f,1110f,1208f,1306f,1404f)
    const val rowHeight=98f
    const val left=137f
    const val rowWidth=667f
    const val homeTop=1510f
    fun fits():Boolean = rows.size==9 &&
        (0 until rows.lastIndex).all{rows[it+1]-rows[it]>=rowHeight} &&
        rows.last()+rowHeight<homeTop &&
        homeTop+112f<=height && left+rowWidth<=width
}
