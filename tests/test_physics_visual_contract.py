#!/usr/bin/env python3
"""Camera overflow is a drawing-only extension; physics boundaries stay fixed."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]/"android/app/src/main/java/com/brendigo/bopavi"
view=(r/"GameView.kt").read_text()
sim=(r/"GameSimulation.kt").read_text()
assert "((h/cameraScale-800f)*.5f).coerceAtLeast(0f)" in view
for needle in (
    "pillarRenderTop=-25f-overflow",
    "pillarRenderBottom=825f+overflow",
    "drawCliffShaft(c,x,w,pillarRenderTop,top",
    "drawCliffShaft(c,x,w,bottom,pillarRenderBottom",
    "drawRockStrata(c,x,w,pillarRenderTop+11f",
    "drawStoneCourses(c,x,pillarRenderTop+36f",
    "drawStoneCourses(c,x,bottom+34f",
    "hudGlass(c,h.score.left,h.score.top,h.score.right,h.score.bottom)",
    "hudGlass(c,h.food.left,h.food.top,h.food.right,h.food.bottom)",
    "hudGlass(c,h.lives.left,h.lives.top,h.lives.right,h.lives.bottom)",
    'text(c,"BODOVI"', 'text(c,"HRANA"'):
    assert needle in view,needle
assert "fun courses(" not in view
assert "private fun drawStoneCourses(" in view
assert "val safeTop=max(fromOpening.top,toOpening.top)+radius" in sim
assert "val safeBottom=min(fromOpening.bottom,toOpening.bottom)-radius" in sim
assert "val radius = 18f" in sim
assert "touchesRoundedBand" in sim and "sweptBodyHitsPillar" in sim
print("PASS: extended on-screen pillars, Croatian HUD, unchanged world physics and safe swept broadphase")
