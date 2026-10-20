#!/usr/bin/env python3
"""Static regression for Android audio-focus and playback ownership.

This test does not certify actual interruptions on physical devices.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
audio=(root/"android/app/src/main/java/com/brendigo/bopavi/Soundscape.kt").read_text()
activity=(root/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
for symbol in ("AudioFocusRequest.Builder(", "requestAudioFocus(focusRequest)",
               "abandonAudioFocusRequest(focusRequest)", "AUDIOFOCUS_LOSS_TRANSIENT",
               "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK", "AUDIOFOCUS_LOSS",
               "AUDIOFOCUS_GAIN", "playbackRequested=false", "appContext",
               "player?.release()", "pool.release()"):
    assert symbol in audio, symbol
assert "catch(_:Exception)" not in audio and "catch (_:Exception)" not in audio
assert "if(closed || !enabled || effectsVolume<=0f)return" in audio
assert "if(closed)return" in audio
assert "onPause()" in activity and "sound.pause()" in activity
assert "onDestroy()" in activity and "sound.close()" in activity
assert "sound.startWorld(0)" in activity
print("PASS: Android audio focus, interruption and lifecycle ownership contract")
