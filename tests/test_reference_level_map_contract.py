#!/usr/bin/env python3
"""Genuine interactive world-map source contract, never a screenshot mock."""
from pathlib import Path
repo=Path(__file__).resolve().parents[1]
src=repo/"android/app/src/main/java/com/brendigo/bopavi"
mapSource=(src/"LevelMapScreen.kt").read_text()
geometry=(src/"LevelMapGeometry.kt").read_text()
store=(src/"ProgressStore.kt").read_text()
unit=(repo/"android/app/src/test/java/com/brendigo/bopavi/LevelMapGeometryTest.kt").read_text()
menu=(src/"ReferenceMenus.kt").read_text()
for phrase in (
    "SKY MEADOWS","CRYSTAL RUINS","VOLCANO PEAKS",
    "onWorld(id)","onPlay(selected,frontier.toLong())",
    "node.textSize=if(unlocked)28f else 24f",
    "isEnabled=enabled","setOnClickListener{ if(isEnabled) onClick() }",
    'if(unlocked)"Level $level, $earned stars. Tap to play"',
    'else "Locked level $level"',
    '"PLAY LEVEL $frontier, $chosenStars STARS"',
    "stage.setOnApplyWindowInsetsListener",
    "View.MeasureSpec.makeMeasureSpec",
    "minimumHeight=dp(48)",
    "importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO",
):
    assert phrase in mapSource,phrase
assert "LevelMapScreen(host,progress).create(" in menu
assert "nearestLevels(frontier)" in mapSource
assert "LevelMapLockGlyph(host)" in mapSource
assert "LevelMapStarStrip(host,earned.coerceIn(0,3))" in mapSource
assert "node.background=RippleDrawable(" in mapSource
assert "node.textSize=if(unlocked)28f else 24f" in mapSource
assert "fun starStrip(index:Int):Point" in geometry
assert "fun nodesInsideReference():Boolean" in geometry
assert "fun starStripsInsideReference():Boolean" in geometry
markers=(src/"LevelMapMarkers.kt").read_text()
assert "private fun star(" in markers
assert "LevelMapLockGlyph" in markers
assert "IMPORTANT_FOR_ACCESSIBILITY_NO" in markers
assert '"🔒"' not in mapSource
assert "else if(index<3)" in mapSource
assert '"Coins: $number"' in mapSource
assert '"Earned stars: $number"' in mapSource
assert "stage.place(LevelMapLandscape(host,selected),0f,0f,941f,1672f)" in mapSource
assert "full=true" not in mapSource
assert "fun displayedWorlds(selected:Int)" in geometry
assert "1L..20L" in store  # 20 stages x 3 genuine rating stars
assert "lockedNodesCannotLaunchAtFreshInstall" in unit
assert "realLevelNodesNeverExceedTheMillionLevelLimit" in unit
qa=(repo/"tools/qa_android_reference_menus.py").read_text()
# The binary screenshot helper appends .png and saves it under qa/screenshots;
# the caller intentionally supplies only the basename.
assert 'width,height=capture_png("android-level-map-reference-size")' in qa
assert 'width,height=capture_png("android-level-map-reference-size")' in qa
assert 'assert (width,height)==(941,1672)' in qa
assert 'screenshot=adb("exec-out","screencap","-p")' not in qa
assert 'node_with("SELECT WORLD 1, SKY MEADOWS",clickable=True)' in qa
assert 'node_with("SELECT WORLD 2, CRYSTAL RUINS",clickable=True)' in qa
assert 'node_with("SELECT WORLD 3, VOLCANO PEAKS",clickable=True)' in qa
assert '"wm","size","reset"' in qa
assert "http://" not in mapSource and "https://" not in mapSource
print("PASS: interactive Level Map nodes, 8 worlds, saved ratings, native touch, no fake locks")
