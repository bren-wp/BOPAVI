package com.brendigo.bopavi

/**
 * Small, platform-independent contract for offline background music.
 *
 * A 0% slider is a true mute: it must not play a silent MediaPlayer or
 * request/retain music audio focus. Effects remain independent.
 */
internal object MusicPlaybackPolicy {
    fun normalizedVolume(value:Float):Float =
        if(value.isFinite()) value.coerceIn(0f,1f) else 0f

    fun shouldPlay(enabled:Boolean,closed:Boolean,requested:Boolean,
                   playerAvailable:Boolean,volume:Float):Boolean =
        enabled && !closed && requested && playerAvailable &&
            volume.isFinite() && volume>0f
}
