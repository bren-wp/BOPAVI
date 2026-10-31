package com.brendigo.bopavi

/** Approved 941 × 1672 Settings reference measurement grid. */
internal object SettingsReferenceLayout {
    const val width=941f
    const val height=1672f
    val rows=floatArrayOf(620f,720f,820f,920f,1020f,1120f,1220f,1320f)
    const val rowHeight=98f
    const val left=137f
    const val rowWidth=667f
    const val homeTop=1471f
    fun fits():Boolean = rows.size==8 &&
        (0 until rows.lastIndex).all{rows[it+1]-rows[it]>=rowHeight} &&
        rows.last()+rowHeight<homeTop &&
        homeTop+112f<=height && left+rowWidth<=width
}
