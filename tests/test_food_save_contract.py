#!/usr/bin/env python3
"""Regression guard: fruit only from physical contact, independent of currency."""
from pathlib import Path
p=Path(__file__).resolve().parents[1]
a=(p/"android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt").read_text()
for code in (a,):
    assert "BerryCollision.touched(" in code and "collectedFood[i]=true" in code
    assert "foodCollected" in code and "score()" in code
    assert "if(endless && gate.coin && !collectedFood[i] &&" in code
    assert "!collectedHearts[i] && (!heartAt(i) || lives>=maxLives)" in code
for renderer in ("android/app/src/main/java/com/brendigo/bopavi/GameView.kt",):
    code=(p/renderer).read_text()
    assert "cachedFood" not in code and "lastAccessibilityFood" not in code
    assert 'Životi: $lives/' in code
    activity=(p/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
    assert "g.foodCollected" in activity and "progress.bestFood()" in activity
for store in ("android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt",):
    code=(p/store).read_text()
    assert "endless_food_best" in code and "bestFood()" in code and "recordRun" in code
    assert "maxOf(bestFood()" in code or "max(bestFood()" in code
print("PASS: fruit contact, Results-only score and food presentation and bestFood monotonicity source guard")
