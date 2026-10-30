#!/usr/bin/env python3
"""Leaderboard is a native, on-device score view, not a fabricated global board."""
from pathlib import Path
repo=Path(__file__).resolve().parents[1]
src=repo/"android/app/src/main/java/com/brendigo/bopavi"
main=(src/"MainActivity.kt").read_text()
screen=(src/"LeaderboardScreen.kt").read_text()
model=(src/"LocalLeaderboardPresentation.kt").read_text()
layout=(src/"LeaderboardLayout.kt").read_text()
unit=(repo/"android/app/src/test/java/com/brendigo/bopavi/LocalLeaderboardPresentationTest.kt").read_text()
qa=(repo/"tools/qa_android_reference_menus.py").read_text()
assert 'selectedScreen="records"' in main
assert 'LeaderboardScreen(this).create(summary,scores,onBack={showHome()})' in main
for term in ("progress.bestPoints()","progress.bestFood()","progress.endlessBest()",
             "progress.totalRuns()","progress.totalFlaps()","progress.leaderboard()"):
    assert term in main,term
for term in ("TOP 10 LOCAL SCORES","OFFLINE • THIS DEVICE",
             "NO SCORES YET — PLAY TO SET A RECORD","R.drawable.homeback",
             "contentDescription=item.description","minimumHeight=dp(48)",
             "ScrollView(host)","stage.setSafe("):
    assert term in screen,term
assert ".sortedWith(compareByDescending<LeaderboardEntry>{it.score}" in model
assert ".thenByDescending{it.gates}).take(10)" in model
assert 'name=item.name.trim().take(24).ifBlank{"Igrač"}' in model
assert "fun valid()" in layout
assert "scoresSortDescendingWithGateTieBreakAndOnlyTenRows" in unit
assert 'capture_png("android-leaderboard-reference-size")' in qa
assert "android.permission.INTERNET" not in (repo/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: top-ten offline leaderboard, real names/scores and responsive native UI")
