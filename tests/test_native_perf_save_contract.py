#!/usr/bin/env python3
"""Fail-closed source contract for two focused P04 native hot-path changes.

This is NOT a profiler, crash reproduction or on-device persistence test.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
ios = (root / "ios/BOPAVI/GameCanvas.swift").read_text(encoding="utf-8")
android = (root / "android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt").read_text(encoding="utf-8")

# The immutable opaque CGColor palette belongs to one view and is reused;
# dynamic, translucent colors must retain their exact alpha on every frame.
assert "private var opaqueColors:[UInt32:CGColor]=[:]" in ios
color = ios.split("    private func color(_ rgb:UInt32,_ alpha:CGFloat=1)->CGColor {",1)[1].split("\n    }",1)[0]
assert "if alpha == 1, let cached=opaqueColors[rgb]" in color
assert "if alpha == 1 {opaqueColors[rgb]=result}" in color
assert "alpha:alpha" in color

# One SharedPreferences.Editor publishes both the run counters and leaderboard.
# Previously the second apply could leave an inconsistent snapshot if the app
# was interrupted between writes. Keep every existing key and serialization.
run = android.split("    fun recordRun(game: GameSimulation) {",1)[1].split("    fun languageCode():String",1)[0]
assert "val edit=prefs.edit()" in run
assert "saveLocalScore(game,edit)" in run
assert run.count("edit.apply()") == 1
assert run.index("saveLocalScore(game,edit)") < run.index("edit.apply()")
save = android.split("    private fun saveLocalScore(game:GameSimulation,editor:android.content.SharedPreferences.Editor) {",1)[1].split("    fun hapticEnabled()",1)[0]
assert 'editor.putString("local_leaderboard",entries.toString())' in save
assert ".apply()" not in save and "prefs.edit()" not in save
for saved_key in ("endless_food_best", "endless_runs", "flaps", "collectibles_", "local_leaderboard"):
    assert saved_key in android, saved_key
print("PASS: iOS opaque palette cache + single Android run-result editor; device QA still required")
