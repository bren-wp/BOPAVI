#!/usr/bin/env python3
"""Verify real progression navigation and nine-screen high-DPI safeguards."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
main=(src/"MainActivity.kt").read_text(encoding="utf-8")
screen=(src/"ProgressOverviewScreen.kt").read_text(encoding="utf-8")
model=(src/"ProgressOverviewMetrics.kt").read_text(encoding="utf-8")
ach=(src/"AchievementsScreen.kt").read_text(encoding="utf-8")
records=(src/"LeaderboardScreen.kt").read_text(encoding="utf-8")
map_screen=(src/"LevelMapScreen.kt").read_text(encoding="utf-8")
ach_layout=(src/"AchievementsLayout.kt").read_text(encoding="utf-8")
records_layout=(src/"LeaderboardLayout.kt").read_text(encoding="utf-8")
map_geometry=(src/"LevelMapGeometry.kt").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/ProgressOverviewMetricsTest.kt").read_text(encoding="utf-8")
qa=(root/"tools/qa_android_reference_menus.py").read_text(encoding="utf-8")

assert 'action(column,"NAPREDAK",false)' in main
assert 'selectedScreen="progress"' in main
assert 'ProgressOverviewScreen(this,progress).create(' in main
assert 'onBack={showHome()}' in main
assert 'onMap={showLevels()}' in main

for marker in ('ProgressOverviewMetrics.completedLevels(frontier)',
               'ProgressOverviewMetrics.firstTwentyStarPercent(stars)',
               'ProgressOverviewMetrics.detail(world,frontier,stars)',
               'store.frontier(world)', 'store.worldStars(world)',
               'store.bestPoints()', 'store.wins()', 'store.totalRuns()',
               'store.coins()', 'store.totalFlaps()',
               'for(world in 0 until LevelEngine.WORLD_COUNT)',
               'ScrollView(host)', 'contentDescription=label',
               'Bopavi3DTheme.panel('):
    assert marker in screen,marker

assert "RippleDrawable(" in screen
assert "setPadding(dp(14),dp(12),dp(14),dp(32))" in screen
assert "LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT)" in screen
assert 'coerceIn(0L,LevelEngine.LEVELS_PER_WORLD.toLong())' in model
assert 'earned.coerceIn(0,60)' in model
assert 'first 20 levels' in model
assert 'assertEquals(100,ProgressOverviewMetrics.firstTwentyStarPercent(60))' in unit
assert 'tap(node_with("NAPREDAK"))' in qa
assert 'capture_png("android-progress-reference-size")' in qa
assert 'assert (width,height)==(941,1672),"FAIL: Progress capture is not 941x1672"' in qa

# Keep the reference camera only where true font/card/touch sizes fit;
# otherwise use the existing accessible native scrolling layouts.
for view,model_source,method in (
    (ach,ach_layout,'supportsCardContent'),
    (records,records_layout,'supportsScoreRows'),
    (map_screen,map_geometry,'supportsNodeTouch'),
):
    assert f'{method}(' in view,method
    assert f'fun {method}(' in model_source,method
    assert 'density.isFinite()' in model_source,method

for screen_source in (screen,ach,records,map_screen):
    assert 'android.permission.INTERNET' not in screen_source
assert "android.permission.INTERNET" not in (root/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: real eight-world Progress page and density-safe Achievements/Records/Level Map")
