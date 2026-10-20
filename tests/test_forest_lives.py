#!/usr/bin/env python3
"""Forest HUD/lives/pickup contract, no data or permission expansion."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
sim=(src/"GameSimulation.kt").read_text()
view=(src/"GameView.kt").read_text()
assert "var lives = if(endless)3 else 1" in sim
assert "invulnerable=1.6f" in sim and "lives--" in sim
assert "heartsCollected++" in sim and "lives=min(maxLives,lives+1)" in sim
assert "private var collectedHearts" in sim and "heartVisible(" in sim
assert "private fun drawMossLedge(" in view and "private fun drawHeartPickup(" in view
assert "private fun drawHudHeart(" in view
assert "hudGlass(c,18f,16f,178f,68f)" in view
assert "Životi:" in view
assert "postInvalidateDelayed(if(reducedMotion) 33L else 14L)" in view
manifest=(root/"android/app/src/main/AndroidManifest.xml").read_text()
assert "android.permission.INTERNET" not in manifest
print("PASS: three lives, rare collectible hearts, mossy pillars and compact native HUD")
