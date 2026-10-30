#!/usr/bin/env python3
"""Regression: forest rock draw path, controls and lifecycle polish (v1.0.9)."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text(encoding="utf-8")
activity=(src/"MainActivity.kt").read_text(encoding="utf-8")
simulation=(src/"GameSimulation.kt").read_text(encoding="utf-8")
render=view.split("private fun drawStoneCourses(",1)[1].split("private fun drawPolishedGateLip(",1)[0]
assert "while(row+16f<yEnd && n<12)" in render
assert "row+=57f;n++" in render
assert "rect(c,x+8f+(n%3)*4f,row,x+42f+(n%2)*6f,row+3f,seam,2f)" in render
assert "if(forest && n%3==0)" in render
assert "path.lineTo(x+18f,row+18f);path.lineTo(x+34f,row+29f)" in render
assert "fun courses(" not in view
assert view.count("drawStoneCourses(c,x,")==2
heart=view.split("private fun drawHudHeart",1)[1].split("private fun drawHud(",1)[0]
# Keep existing missing-heart transparency; the active enamel red and its
# navy contour were deliberately updated to match the owner's reference.
assert "fill(if(active)0xffff4f51.toInt() else 0x552c4b73)" in heart
assert "p.strokeWidth=2.3f" in heart
assert "p.style=Paint.Style.FILL" in heart
assert "p.strokeCap=Paint.Cap.BUTT" in heart
assert 'ColorStateList.valueOf(0xff80eeed.toInt())' in activity
assert 'ColorStateList.valueOf(0xff4cdddc.toInt())' in activity
assert 'ColorStateList.valueOf(0xffffde76.toInt())' in activity
assert 'window.decorView.hasWindowFocus()' in activity
assert "view.paused=true" in activity and "showPremiumPause(view)" in activity
assert "gamePauseButton" not in activity
for reward in ("coinVisible(","crystalVisible(","powerVisible("):
    assert reward in simulation and "game."+reward in view,reward
assert "starVisible(" not in simulation
# Safety invariants: no world/physics behaviour changed by render refactoring.
for constant in ("val radius = 18f", "1f / 120f", "maxStep=85f*dt",
                 "if(endless && nextLevel==null && passed>=level.gates.size/2)prepareNext()"):
    assert constant in simulation,constant
assert "android.permission.INTERNET" not in (root/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: renderer geometry, accessibility, pause focus, dead code and physics contracts")
