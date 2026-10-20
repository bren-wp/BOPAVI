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
    "courses(pillarRenderTop+36f",
    "hudGlass(c,18f,20f,212f,88f)",
    "hudGlass(c,228f,20f,376f,88f)",
    'text(c,"BODOVI"', 'text(c,"HRANA"'):
    assert needle in view,needle
assert "val safeTop=max(fromOpening.top,toOpening.top)+radius" in sim
assert "val safeBottom=min(fromOpening.bottom,toOpening.bottom)-radius" in sim
assert "val radius = 18f" in sim
assert "touchesRoundedBand" in sim and "sweptBodyHitsPillar" in sim
print("PASS: extended on-screen pillars, Croatian HUD, unchanged world physics and safe swept broadphase")
