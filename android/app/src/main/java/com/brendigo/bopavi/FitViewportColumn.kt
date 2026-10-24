package com.brendigo.bopavi

import android.content.Context
import android.widget.ScrollView

/**
 * A one-screen layout at normal heights and an accessible scroll container
 * only when the actual inset-safe window is too short for the real controls.
 *
 * Scaling the entire View hierarchy previously shrank 48dp touch targets,
 * text and accessibility focus rectangles on split-screen/compact phones.
 * Fill-viewport preserves the original spacious hero at normal sizes while
 * ScrollView handles genuinely undersized windows without clipping actions.
 * Art remains behind this transparent native interactive layer.
 */
internal class FitViewportColumn(context: Context) : ScrollView(context) {
    init {
        isFillViewport = true
        isVerticalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        isSmoothScrollingEnabled = true
        clipToPadding = false
    }
}
