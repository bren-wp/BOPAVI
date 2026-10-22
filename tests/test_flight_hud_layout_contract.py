#!/usr/bin/env python3
"""Guard on-screen lives, fruit, score, ability and approaching pillar warnings.

This is a source contract; the Kotlin test checks actual pure geometry and the
Android emulator captures the real painted view.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text(encoding="utf-8")
layout=(src/"FlightHudLayout.kt").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightHudLayoutTest.kt").read_text(encoding="utf-8")
hud=view.split("    private fun drawHud(c:Canvas){",1)[1].split("    private fun drawCollisionDebugOverlay(",1)[0]
for name in (
    "hudGlass(c,h.score.left,h.score.top,h.score.right,h.score.bottom)",
    "hudGlass(c,h.food.left,h.food.top,h.food.right,h.food.bottom)",
    "hudGlass(c,h.lives.left,h.lives.top,h.lives.right,h.lives.bottom)",
    "hudGlass(c,h.ability.left,h.ability.top,h.ability.right,h.ability.bottom)",
    'text(c,"ŽIVOTI",316f,35f,11f',
    'text(c,cachedLivesLabel,436f,35f,11f',
    "drawHudHeart(c,h.heartCenters[i],FlightHudLayout.heartBaseline,i<game.lives)",
    'text(c,"MOĆ",30f,91f,10f',
    "p.measureText(value)",
    "if(game.lives!=cachedLives)",
    "cachedLivesLabel=",
):
    assert name in (view if name=="p.measureText(value)" else hud),name
assert "FlightHudLayout.movingWarningVisible(g.movement,x,game.birdX)" in view
assert 'text(c,"!",cx,badgeY+6f,18f' in view
assert "if(FlightHudLayout.movingWarningVisible" in view
for name in (
    "val score = FlightHudBox(18f,16f,165f,68f)",
    "val food = FlightHudBox(172f,16f,297f,68f)",
    "val lives = FlightHudBox(304f,16f,462f,68f)",
    "val ability = FlightHudBox(18f,75f,310f,119f)",
    "val heartCenters = floatArrayOf(326f,370f,414f)",
    "movement>0f && x>=birdX+48f && x<=480f",
):
    assert name in layout,name
for name in ("lifeHeartsNeverOverlapScoreFoodOrAbility",
             "onlyUpcomingMovingPillarsGetWarnings"):
    assert name in unit,name
assert "game.score()" in hud and "game.foodCollected" in hud
assert "contentDescription=lastHudAccessibility" in hud
print("PASS: aligned collision-free HUD, truthful life indicators and moving pillar warnings")
