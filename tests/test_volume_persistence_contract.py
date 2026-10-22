#!/usr/bin/env python3
"""Guard both real native volume sliders against dropped accessibility saves.

The deterministic Kotlin/JVM tests cover the gesture decision. This source
contract ensures the actual music and effect listeners use that decision.
It is not a substitute for a physical TalkBack session.
"""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
activity=(src/"MainActivity.kt").read_text(encoding="utf-8")
policy=(src/"VolumeGesturePolicy.kt").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/VolumeGesturePolicyTest.kt").read_text(encoding="utf-8")
menu=activity.split("    private fun showQuickOptions(){",1)[1].split("    private fun premiumDialogHeader(",1)[0]
slider=menu.split("fun volumeSlider(caption:String,initial:Int,music:Boolean){",1)[1].split('volumeSlider("Glazba",',1)[0]

for required in (
    "var lastSavedVolume=initial",
    "if(safe==lastSavedVolume)return",
    "this@MainActivity.progress.setMusicVolume(safe)",
    "this@MainActivity.progress.setEffectsVolume(safe)",
    "VolumeGesturePolicy.commitAsProgressChanges(fromUser,touchTracking)",
    "private var touchTracking=false",
    "touchTracking=true",
    "touchTracking=false",
    "bar?.progress?.let(::persistVolume)",
    "if(music) sound.musicVolume=value/100f",
    "else sound.effectsVolume=value/100f",
):
    assert required in slider,required
assert slider.count("setMusicVolume(safe)")==1
assert slider.count("setEffectsVolume(safe)")==1
assert 'volumeSlider("Glazba",progress.musicVolume(),true)' in menu
assert 'volumeSlider("Zvučni efekti",progress.effectsVolume(),false)' in menu
assert 'fromUser && !touchTracking' in policy
for name in (
    "keyboardAndAccessibilityAdjustmentsPersistWithoutTouchRelease",
    "dragEventsAreCoalescedUntilTrackingStops",
    "initialSeekBarBindingNeverWritesUserPreferences",
):
    assert name in unit,name

store=(src/"ProgressStore.kt").read_text(encoding="utf-8")
for key in ('"music_volume"','"effects_volume"','"music_last_audible_volume"'):
    assert key in store,key
print("PASS: keyboard/TalkBack slider changes persist; drags coalesce; no duplicate writes")
