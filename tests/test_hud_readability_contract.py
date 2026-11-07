"""Idle last-heart HUD and active reference SCORE/Pause both use real state."""
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
assert 'FlightHudReadability.lifeCountColor(lives,game.maxLives)' in view
assert 'path.addCircle(h.avatarX,h.avatarY,h.avatarRadius,Path.Direction.CW)' in view
assert 'lastHudAccessibility=' in view
assert 'lifeColorsMatchTheRealState' in unit
assert 'finalLifeWarningOnlyWhenOneHeartRemains' in unit
assert 'fun isFinalLife(' in policy and 'fun lifeCountColor(' in policy
assert 'gamePauseButton' not in activity
# The active flight now has a real orange Pause touch region; the ready
# state still keeps the old accessible lives badge and no fake shop currency.
assert '"GORE DESNO PAUZA"' in emulator
assert 'GameplayReferenceHudLayout.hitArea(' in view
assert 'showPremiumPause(current)' in activity
assert 'adb shell input keyevent 4' in emulator
print("PASS: idle last-life cue and real active SCORE/Pause HUD without overlap")
