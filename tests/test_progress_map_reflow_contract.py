#!/usr/bin/env python3
"""Native Progress and Level Map reflow/interaction source guard."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
native=root/"android/app/src/main/java/com/brendigo/bopavi"
progress=(native/"ProgressOverviewScreen.kt").read_text(encoding="utf-8")
main=(native/"MainActivity.kt").read_text(encoding="utf-8")
map_source=(native/"LevelMapScreen.kt").read_text(encoding="utf-8")
geo=(native/"LevelMapGeometry.kt").read_text(encoding="utf-8")
qa=(root/"tools/qa_android_reference_menus.py").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/LevelMapGeometryTest.kt").read_text(encoding="utf-8")
for item in (
    "private fun worldCard(world:Int,onOpenMap:()->Unit):View",
    "isClickable=true", "isFocusable=true", "setOnClickListener{onOpenMap()}",
    "column.addView(worldCard(world){onWorld(world)}",
    "ProgressOverviewMetrics.completedLevels(frontier)",
    "ProgressOverviewMetrics.firstTwentyStarPercent(stars)",
    "store.frontier(world)", "store.worldStars(world)",
    "store.bestPoints()", "store.wins()", "store.totalRuns()",
    "store.coins()", "store.totalFlaps()",
):
    assert item in progress,item
for item in (
    "private var progressMapReflowPending=false",
    'selectedScreen=="levels" || selectedScreen=="progress"',
    "val pendingScreen=selectedScreen",
    'if(pendingScreen=="levels")showLevels()',
    "else showProgressOverview()",
    "onWorld={world->",
    "progress.chooseWorld(world)",
):
    assert item in main,item
assert "LevelMapStage(host).apply {useShopFullBleedReferenceCamera()}" in map_source
assert "stage.place(LevelMapBackControl(host,onBack)," in map_source
assert "stage.place(ReferenceArrowOverlayView(host)" not in map_source
assert "RippleDrawable(" in progress
assert "LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT)" in progress
assert "private val lowerColors=LevelMapGeometry.islandPalette(selected,0)" in map_source
assert "private val middleColors=LevelMapGeometry.islandPalette(selected,1)" in map_source
assert "private val upperColors=LevelMapGeometry.islandPalette(selected,2)" in map_source
assert "fun islandPalette(world:Int,slot:Int):IntArray" in geo
assert "fun supportsNodeTouch(" in geo and "48f*density" in geo
assert "firstOwnerPageRetainsItsExactIslandColors" in unit
assert "laterPagesHaveDistinctWorldBiomesWithoutChangingStageGeometry" in unit
assert 'adb("shell","wm","size","720x1280")' in qa
assert 'tap(node_with("SKY MEADOWS",clickable=True))' in qa
assert 'capture_png("android-progress-reference-size")' in qa
assert 'verify_eight_progress_cards()' in qa
assert 'android-progress-middle-reference-size' in qa
assert 'android-progress-bottom-reference-size' in qa
assert 'android-level-map-page-two-reference-size' in qa
assert 'android-level-map-page-three-reference-size' in qa
assert 'tap(unique_map_home())' in qa
assert 'capture_png("android-level-map-reference-size")' in qa
assert "onPlay(selected,frontier.toLong())" in map_source
assert "if(enabled)onPlay(selected,level.toLong())" in map_source
print("PASS: Progress world-card routing, resize reflow, owner-sized Map art and real levels")
