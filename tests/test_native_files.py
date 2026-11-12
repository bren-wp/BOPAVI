#!/usr/bin/env python3
"""Production Android navigation, persistent data, flight and security invariants."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
src=r/"android/app/src/main/java/com/brendigo/bopavi"
a=(src/"MainActivity.kt").read_text()
sim=(src/"GameSimulation.kt").read_text()
view=(src/"GameView.kt").read_text()
store=(src/"ProgressStore.kt").read_text()
manifest=(r/"android/app/src/main/AndroidManifest.xml").read_text()
flow=(r/".github/workflows/native-ci.yml").read_text()
assert 'android:allowBackup="false"' in manifest
assert 'android.permission.INTERNET' not in manifest
assert 'else showHome()' in a
home=a.split("private fun showHome(){",1)[1].split("private fun confirmGameplayShop(",1)[0]
for name in ('feature("SKINS"','feature("SHOP"','text="SETTINGS"'):
    assert name in home,name
for forbidden in ('▶  PLAY','shortcut("','showLevels(', 'showLocalRecords(',
                  'showProgressOverview(', 'showLocalAchievements('):
    assert forbidden not in home,forbidden
assert 'private fun showSkins(){' in a
assert 'private fun showShop(){' in a
assert 'private fun showSettingsScreen(){' in a
assert 'private fun startGame(){' in a  # retained game engine, not Home route
assert 'private fun showResult(g:GameSimulation){' in a
assert 'progress.recordRun(g)' in a
assert 'GameSimulation(' in a
assert 'BerryCollision.touched(' in sim
assert 'foodCollected' in sim and 'sweptBodyHitsPillar' in sim
assert 'hudPortraitRect' in view and 'drawHudHeart(' in view
assert 'fun recordRun(game: GameSimulation)' in store
assert 'fun selectOrBuy(index: Int): Boolean' in store
assert "python tools/audit_native_dead_code.py" in flow
print("PASS: three-route Home, offline persistence, retained gameplay core")
