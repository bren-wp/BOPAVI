#!/usr/bin/env python3
"""v1.0.6: native quick options and read-only records, preserving minimal flight."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
main=(src/"MainActivity.kt").read_text(encoding="utf-8")
store=(src/"ProgressStore.kt").read_text(encoding="utf-8")
manifest=(root/"android/app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")

home=main.split("private fun showHome(){",1)[1].split("private fun showPremiumPause(",1)[0]
options=main.split("private fun showQuickOptions(){",1)[1].split("private fun showLocalRecords(){",1)[0]
records=main.split("private fun showLocalRecords(){",1)[1].split("private fun showHome(){",1)[0]
play=main.split("private fun startGame(){",1)[1].split("private fun showResult(",1)[0]

assert 'secondaryChoice("⚙  OPCIJE","Otvori opcije igre"){showQuickOptions()}' in home
assert 'secondaryChoice("★  REKORDI","Pogledaj svoje rekorde"){showLocalRecords()}' in home
assert 'LinearLayout.LayoutParams(0,d(48),1f)' in home
assert 'action(content,"▶  IGRAJ"){startGame()}' in home
assert 'showSettings(' not in main
assert 'setTitle("OPCIJE IGRE")' in options
assert 'premiumDialogHeader("OPCIJE IGRE",BopaviUiIconDrawable.Kind.SETTINGS)' in options
assert 'progress.difficultyNames' in options and 'progress.setDifficulty(index)' in options
assert 'progress.setSoundEnabled(enabled)' in options and 'sound.enabled=enabled' in options
assert 'progress.setHapticEnabled(enabled)' in options
assert 'progress.setLessMotion(enabled)' in options
assert 'progress.setMusicVolume(safe)' in options and 'sound.musicVolume=value/100f' in options
assert 'progress.setEffectsVolume(safe)' in options and 'sound.effectsVolume=value/100f' in options
assert 'VolumeGesturePolicy.commitAsProgressChanges(fromUser,touchTracking)' in options
assert 'bar?.progress?.let(::persistVolume)' in options
assert 'onStopTrackingTouch(' in options and 'onProgressChanged(' in options
assert 'perks.first,perks.second,progress.difficulty(),1L,' in play
assert 'PilotAbility.fromChoice(selectedPilotChoice)),0)' in play
assert 'LevelEngine.createStream(world,number)' in play
assert 'progress.totalRuns()' in records and 'progress.totalFlaps()' in records
assert 'progress.bestPoints()' in records and 'progress.bestFood()' in records
assert 'progress.endlessBest()' in records
assert 'fun totalRuns():Int' in store and 'fun totalFlaps():Long' in store
for name in ('difficulty','sound_enabled','haptic_enabled','less_motion','music_volume','effects_volume','endless_runs','flaps'):
    assert '"'+name+'"' in store,name
assert 'android.permission.INTERNET' not in manifest
print("PASS: reachable quick options and records, real difficulty and new offline menu routes")

qa=(root/"tools/qa_android_emulator.sh").read_text(encoding="utf-8")
for contract in (
    "FAIL: Home quick options inaccessible",
    "FAIL: relaxed flight difficulty not selected",
    "FAIL: restored standard difficulty not selected",
    "FAIL: Home records action missing",
    "PASS: local records visible and dismissible",
):
    assert contract in qa,contract

assert 'val compactDisplay=resources.configuration.screenHeightDp<770' in main
assert 'd(if(tinyDisplay)70 else if(compactDisplay)90 else 203)' in main

# The approved home logo already paints the slogan: do not waste another
# full-height native row and scale the 48dp legal hitboxes on compact phones.
assert 'title(content,"MALI LETOVI · VELIKE PRIČE"' not in home
