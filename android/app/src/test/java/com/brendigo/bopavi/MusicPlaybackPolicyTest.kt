package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class MusicPlaybackPolicyTest {
    @Test fun zeroVolumeIsRealMuteNotASilentFocusRequest() {
        assertFalse(MusicPlaybackPolicy.shouldPlay(
            enabled=true,closed=false,requested=true,playerAvailable=true,volume=0f))
        assertFalse(MusicPlaybackPolicy.shouldPlay(
            enabled=true,closed=false,requested=true,playerAvailable=true,volume=-1f))
    }

    @Test fun focusIsOnlyNeededWhenAnAudibleActiveSessionWantsMusic() {
        assertTrue(MusicPlaybackPolicy.shouldPlay(true,false,true,true,.6f))
        assertFalse(MusicPlaybackPolicy.shouldPlay(false,false,true,true,.6f))
        assertFalse(MusicPlaybackPolicy.shouldPlay(true,true,true,true,.6f))
        assertFalse(MusicPlaybackPolicy.shouldPlay(true,false,false,true,.6f))
        assertFalse(MusicPlaybackPolicy.shouldPlay(true,false,true,false,.6f))
    }

    @Test fun invalidOrOutOfRangeVolumesNeverReachTheAudioEngine() {
        for(value in listOf(Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY))
            assertEquals(0f,MusicPlaybackPolicy.normalizedVolume(value),0f)
        assertEquals(0f,MusicPlaybackPolicy.normalizedVolume(-.5f),0f)
        assertEquals(1f,MusicPlaybackPolicy.normalizedVolume(1.5f),0f)
        assertEquals(.35f,MusicPlaybackPolicy.normalizedVolume(.35f),0f)
        assertFalse(MusicPlaybackPolicy.shouldPlay(true,false,true,true,Float.NaN))
    }

    @Test fun muteAndRestorePreserveIntentButRequireNewFocusGrant() {
        val playbackRequested=true
        val muted=MusicPlaybackPolicy.shouldPlay(true,false,playbackRequested,true,0f)
        val restored=MusicPlaybackPolicy.shouldPlay(true,false,playbackRequested,true,.5f)
        assertFalse(muted)
        assertTrue(restored)
        assertFalse(MusicPlaybackPolicy.shouldPlay(true,false,false,true,.5f))
    }
}
