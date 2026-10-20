#!/usr/bin/env python3
"""Fail-closed static contract for debug-only physics collision outlines."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
android=(r/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
activity=(r/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
ci=(r/".github/workflows/native-ci.yml").read_text()
assert 'FLAG_DEBUGGABLE' in android and 'collisionOverlayEnabled=qaCollisionOverlay &&' in android
assert 'bopavi_qa_collision_overlay' in activity
assert 'drawCollisionDebugOverlay(canvas)' in android
assert 'Lint, JVM unit tests, compile APK and unsigned release AAB' in ci
assert "drawCircle(game.birdX,game.y,game.radius,p)" in android
assert "FLAG_DEBUGGABLE" in android
print("PASS: only debuggable/test-opt-in Android collision debug artwork")
