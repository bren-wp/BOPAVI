"""Final-heart state and genuine on-screen touch targets, without a Pause HUD."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text()
activity=(src/"MainActivity.kt").read_text()
policy=(src/"FlightHudReadability.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightHudReadabilityTest.kt").read_text()
emulator=(root/"tools/qa_android_emulator.sh").read_text()
assert 'FlightHudReadability.lifeCountColor(lives,game.maxLives)' in view
assert 'FlightHudReadability.isFinalLife(lives,game.maxLives)' in view
assert 'p.textAlign=Paint.Align.RIGHT' in view
assert 'lastHudAccessibility=' in view
assert 'lifeColorsMatchTheRealState' in unit
assert 'finalLifeWarningOnlyWhenOneHeartRemains' in unit
assert 'fun isFinalLife(' in policy and 'fun lifeCountColor(' in policy
assert 'gamePauseButton' not in activity
assert 'PAUZIRAJ IGRU" in label(n)' in emulator
assert 'len(pause)==0' in emulator
print("PASS: last-life cue and on-screen HUD without Pause overlap")
