"""Reference-inspired navigation must remain genuinely interactive and offline.

The nine 941x1672 user references define an art target, not real score
or currency states. No compiled screen may display a baked fake wallet.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
menus=(src/"ReferenceMenus.kt").read_text()
app=(src/"MainActivity.kt").read_text()
state=(src/"ProgressStore.kt").read_text()
sim=(src/"GameSimulation.kt").read_text()
for route in ("fun skins(","fun shop(","fun levels("):
    assert route in menus,route
for route in ("private fun showSkins()","private fun showShop()",
              "private fun showLevels()","private fun startLevel("):
    assert route in app,route
for action in ('feature("SKINS"','feature("SHOP"',
               'private fun showLevels()'):
    assert action in app,action
assert "progress.chooseArcadePilot(index)" in app
assert "progress.buyPerk(index)" in app
assert "progress.claimBonusCoins()" in app
levelMap=(src/"LevelMapScreen.kt").read_text()
geometry=(src/"LevelMapGeometry.kt").read_text()
assert "LevelMapScreen(host,progress).create(world,onWorld,onPlay,onBack)" in menus
assert "progress.frontier(selected)" in levelMap
assert "progress.bestLevelStars(selected,level.toLong())" in levelMap
assert "if(unlocked)onPlay(selected,level.toLong())" in levelMap
assert "isEnabled=enabled" in levelMap
assert "LevelMapGeometry.isUnlocked(level,frontier)" in levelMap
assert "val nodes=listOf(" in geometry
assert "progress.recordLevelStars(completed.first,completed.second,g.completedLevelStars)" in app
assert "earnedReward=progress.completeLevelFromRecordedRun(" in app
assert "completed.first,completed.second,g.score())" in app
assert "val winData=LevelCompleteState.fromRun(" in app
assert "earnedReward)" in app
assert "if(!runAlreadyRecorded)e.putInt" in state
assert "fun completeLevel(world:Int, number:Long, score:Int):Int" in state
assert "fun recordRun(game: GameSimulation)" in state
assert "fun buyPerk(index:Int):Boolean" in state
assert "val radius = 18f" in sim
for fake in ("1,250", "85 Gems", "Cloud Save", "Restore Purchases",
             "Notifications", "R.drawable.reference_screenshot"):
    assert fake not in menus, fake
assert "gamePauseButton" not in app
assert "python3 tools/qa_android_reference_menus.py" in (root/"tools/qa_android_emulator.sh").read_text()
assert "versionCode = 170" in (root/"android/app/build.gradle.kts").read_text()
print("PASS: three real menus, persisted pilots, earned-coin shop and genuine level unlocks")
