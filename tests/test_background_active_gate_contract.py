#!/usr/bin/env python3
"""Real active-flight semantics must be observable before background/resume QA."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
game=(src/"GameView.kt").read_text()
qa=(root/"tools/qa_android_background.py").read_text()
assert 'private var lastAccessibilityActive = false' in game
assert 'if(lives!=lastAccessibilityLives || game.active!=lastAccessibilityActive)' in game
assert 'lastAccessibilityActive=game.active' in game
assert 'if(game.active)" Aktivan let." else " Prije prvog zamaha."' in game
assert 'lastAccessibilityActive=true' in game
assert 'lastHudAccessibility=flightHint()+" Aktivan let. Životi:' in game
assert 'private fun announceActiveFlight()' in game
assert 'if(!game.active || game.finished)return' in game
assert 'if(game.active)announceActiveFlight()' in game
assert 'if(scoreChanged || crystalsChanged || livesChanged || !lastAccessibilityActive){' in game
assert game.count("announceActiveFlight()")>=3
assert 'android-background-active-failed-hierarchy.xml' in qa
assert 'android-background-active-failed-screen' in qa
assert 'duplicate native active flight controls' in qa
assert '"AKTIVAN LET" in label(n)' in qa
assert 'first flap never transitioned the actual simulation to active state' in qa
assert 'showPauseAfterBackground=true' in (src/"MainActivity.kt").read_text()
print("PASS: background flow requires genuine active first flap, never ready-game accessibility")
