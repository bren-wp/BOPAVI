package com.brendigo.bopavi

/** Owner-measured Shop header offset on the native 941×1672 design camera.
 *
 * The original v1.0.85 Android16 1080×1920 capture placed the gold title
 * roughly 97px above the user's native-size reference. This fixed correction
 * moves only the separate wood sign and its real TextView wordmark; the
 * functional wallets, 3D product buttons, kiosk and card grid do not move.
 * It is NOT proof of exact RGB identity, which needs another emulator capture.
 */
internal object ShopHeaderLayout {
    const val titleOffset=85f
    const val titleTop=123f+titleOffset
}
