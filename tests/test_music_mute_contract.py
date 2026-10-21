#!/usr/bin/env python3
"""Offline music mute must not steal focus or decode a silent soundtrack.

The JVM policy tests verify boolean states and invalid volumes; this check
ensures the real Android AudioManager/MediaPlayer paths use those rules.
"""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
path=root/"android/app/src/main/java/com/brendigo/bopavi"
audio=(path/"Soundscape.kt").read_text(encoding="utf-8")
policy=(path/"MusicPlaybackPolicy.kt").read_text(encoding="utf-8")
activity=(path/"MainActivity.kt").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/MusicPlaybackPolicyTest.kt").read_text()

for required in (
    "MusicPlaybackPolicy.normalizedVolume(value)",
    "MusicPlaybackPolicy.shouldPlay(",
    "private fun ensureWorldPlayer():Boolean",
    "if(closed || !enabled || musicVolume<=0f || world !in worlds.indices)return false",
    "if(ensureWorldPlayer() && acquireFocus()) startPlayback()",
    "if (field<=0f) {",
    "pausePlayer()",
    "abandonFocus()",
    "if (!canPlayMusic()) return false",
    "if (!canPlayMusic() || !focusHeld) return",
    "if (focusRequested) {",
    "AUDIOFOCUS_REQUEST_DELAYED",
    "player?.release()",
    "pool.release()",
):
    assert required in audio,required

start=audio.split("    fun startWorld(index: Int) {",1)[1].split("    fun effect(name:",1)[0]
assert "MediaPlayer.create(" not in start,"Muted start must not eagerly decode"
assert "resume()" in start
assert "if(musicVolume<=0f)" in audio.split("    fun resume() {",1)[1].split("    fun stop()",1)[0]
assert "if(value.isFinite()) value.coerceIn(0f,1f) else 0f" in policy
assert "volume.isFinite() && volume>0f" in policy
assert "sound.musicVolume=value/100f" in activity
assert "progress.setMusicVolume(value)" in activity
for test in (
    "zeroVolumeIsRealMuteNotASilentFocusRequest",
    "focusIsOnlyNeededWhenAnAudibleActiveSessionWantsMusic",
    "invalidOrOutOfRangeVolumesNeverReachTheAudioEngine",
    "muteAndRestorePreserveIntentButRequireNewFocusGrant",
):
    assert test in unit,test
print("PASS: quiet mode avoids audio focus and silent MediaPlayer while retaining music restoration")
