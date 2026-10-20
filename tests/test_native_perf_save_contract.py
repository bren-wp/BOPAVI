#!/usr/bin/env python3
"""Fail-closed source contract for Android save hot-path consistency.

This is NOT a profiler, crash reproduction or on-device persistence test.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
android = (root / "android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt").read_text(encoding="utf-8")

# One SharedPreferences.Editor publishes both the run counters and leaderboard.
# Previously the second apply could leave an inconsistent snapshot if the app
# was interrupted between writes. Keep every existing key and serialization.
run = android.split("    fun recordRun(game: GameSimulation) {",1)[1].split("    fun lessMotion(): Boolean",1)[0]
assert "val edit=prefs.edit()" in run
assert "saveLocalScore(game,edit)" in run
assert run.count("edit.apply()") == 1
assert run.index("saveLocalScore(game,edit)") < run.index("edit.apply()")
save = android.split("    private fun saveLocalScore(game:GameSimulation,editor:android.content.SharedPreferences.Editor) {",1)[1].split("    fun hapticEnabled()",1)[0]
assert 'editor.putString("local_leaderboard",entries.toString())' in save
assert ".apply()" not in save and "prefs.edit()" not in save
for saved_key in ("endless_food_best", "endless_runs", "flaps", "collectibles_", "local_leaderboard"):
    assert saved_key in android, saved_key
print("PASS: single Android run-result editor; device QA still required")
