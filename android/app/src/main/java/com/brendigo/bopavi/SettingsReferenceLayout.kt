package com.brendigo.bopavi

/** Approved 941 × 1672 Settings reference measurement grid. */
internal object SettingsReferenceLayout {
    const val width=941f
    const val height=1672f
    // The owner art separates the live settings from two legal routes by a
    // Nine 80px owner rows with visible enamel/cream gutters and a carved
    // legal divider after Cloud Save, measured on the 941x1672 reference.
    // Physical hit targets remain native and accessibility-safe.
    val rows=floatArrayOf(620f,706f,796f,885f,975f,1066f,1156f,1257f,1340f)
    const val rowHeight=81f
    const val left=137f
    const val rowWidth=667f
    const val homeTop=1430f
    fun fits():Boolean = rows.size==9 &&
        (0 until rows.lastIndex).all{rows[it+1]-rows[it]>=rowHeight} &&
        rows.last()+rowHeight<homeTop &&
        homeTop+112f<=height && left+rowWidth<=width
}
