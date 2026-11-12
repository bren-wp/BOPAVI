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
settings=(src/"SettingsScreen.kt").read_text(encoding="utf-8")
policy=(src/"VolumeGesturePolicy.kt").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/VolumeGesturePolicyTest.kt").read_text(encoding="utf-8")
# The deleted quick-options overlay no longer owns volume controls.
# Real Settings sliders persist keyboard/TalkBack and drag changes.
for required in (
    "VolumeGesturePolicy.commitAsProgressChanges(fromUser,tracking)",
    "bar?.progress?.let(::save)",
    "setOnSeekBarChangeListener(",
    "onMusic",
    "onEffects",
):
    assert required in settings,required
assert "progress.setMusicVolume(value)" in activity
assert "progress.setEffectsVolume(value)" in activity
assert "showQuickOptions(" not in activity
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
