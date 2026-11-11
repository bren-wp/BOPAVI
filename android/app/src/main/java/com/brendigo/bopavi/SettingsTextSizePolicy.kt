package com.brendigo.bopavi

/**
 * Guarantee a valid Android TextView uniform auto-size range.
 *
 * Both the compact Settings footer and the difficulty/Mode label are 12sp.
 * Passing 13sp as the minimum for either 12sp maximum throws during page
 * creation and can return the user to the Android launcher.
 */
internal object SettingsTextSizePolicy {
    fun minSp(maxSp:Int):Int {
        require(maxSp>=2) { "Text maximum must be at least 2sp" }
        return if(maxSp>13) 13 else (maxSp-2).coerceAtLeast(1)
    }
}
