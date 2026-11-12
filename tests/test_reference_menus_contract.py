#!/usr/bin/env python3
"""Native offline menu destinations: Skins, Shop and Settings only."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
src=r/"android/app/src/main/java/com/brendigo/bopavi"
menu=(src/"ReferenceMenus.kt").read_text()
a=(src/"MainActivity.kt").read_text()
store=(src/"ProgressStore.kt").read_text()
for s in ("fun skins(","fun shop("):assert s in menu,s
assert "fun levels(" not in menu
assert "LevelMapScreen" not in menu
for s in ('feature("SKINS"', 'feature("SHOP"',
          'private fun showSettingsScreen(){','progress.chooseArcadePilot(index)',
          'progress.buyPerk(index)','progress.claimBonusCoins()'):
    assert s in a,s
for s in ('fun completeLevel(world:Int, number:Long, score:Int):Int',
          'fun recordRun(game: GameSimulation)','fun buyPerk(index:Int):Boolean'):
    assert s in store,s
assert "android.permission.INTERNET" not in (r/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: only supported offline menu routes remain")
