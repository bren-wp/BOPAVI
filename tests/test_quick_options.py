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
assert 'setTitle("⚙  OPCIJE IGRE")' in options
assert 'progress.difficultyNames' in options and 'progress.setDifficulty(index)' in options
assert 'progress.setSoundEnabled(enabled)' in options and 'sound.enabled=enabled' in options
assert 'progress.setHapticEnabled(enabled)' in options
assert 'progress.setLessMotion(enabled)' in options
assert 'progress.setMusicVolume(value)' in options and 'sound.musicVolume=value/100f' in options
assert 'progress.setEffectsVolume(value)' in options and 'sound.effectsVolume=value/100f' in options
assert 'onStopTrackingTouch(' in options and 'onProgressChanged(' in options
assert 'progress.difficulty(),1L)' in play
assert 'progress.totalRuns()' in records and 'progress.totalFlaps()' in records
assert 'progress.bestPoints()' in records and 'progress.bestFood()' in records
assert 'progress.endlessBest()' in records
assert 'fun totalRuns():Int' in store and 'fun totalFlaps():Long' in store
for name in ('difficulty','sound_enabled','haptic_enabled','less_motion','music_volume','effects_volume','endless_runs','flaps'):
    assert '"'+name+'"' in store,name
assert 'android.permission.INTERNET' not in manifest
print("PASS: reachable quick options and records, real difficulty, no new routes or permissions")
