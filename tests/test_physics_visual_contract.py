"""New visual lip is decorative: 480x800 physics and rounded cap geometry remain."""
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
    "rect(c,x-7f,top-28f,x+w+5f,top,cap,7f)",
    "rect(c,x-7f,bottom,x+w+5f,bottom+27f,cap,7f)",
    "drawPolishedGateLip(c,x,w,top-28f)",
    "drawPolishedGateLip(c,x,w,bottom)",
    "hudGlass(c,h.lives.left,h.lives.top,h.lives.right,h.lives.bottom)",
):
    assert needle in view,needle
assert 'text(c,"BODOVI"' not in view
assert "fun courses(" not in view
assert "val safeTop=max(fromOpening.top,toOpening.top)+radius" in sim
assert "val radius = 18f" in sim
assert "sweptBodyHitsPillar" in sim
print("PASS: improved collision-aligned pillars, lives-only HUD and unchanged physics")
