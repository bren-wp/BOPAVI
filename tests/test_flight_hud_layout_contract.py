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
    'text(c,"ŽIVOTI",300f,38f,12f',
    'drawHudRightText(c,cachedLivesLabel,h.lifeCountRight,38f)',
    "drawHudHeart(c,h.heartCenters[i],FlightHudLayout.heartBaseline,i<game.lives)",
    'text(c,"MOĆ",31f,110f,12f',
    "p.measureText(value)",
    "if(game.lives!=cachedLives)",
    "cachedLivesLabel=",
):
    assert name in (view if name=="p.measureText(value)" else hud),name
assert "FlightHudLayout.movingWarningVisible(g.movement,x,game.birdX)" in view
assert 'text(c,"!",cx,badgeY+6f,18f' in view
assert "if(FlightHudLayout.movingWarningVisible" in view
for name in (
    "val score = FlightHudBox(18f,16f,146f,82f)",
    "val food = FlightHudBox(154f,16f,276f,82f)",
    "val lives = FlightHudBox(284f,16f,462f,82f)",
    "val ability = FlightHudBox(18f,91f,340f,140f)",
    "val heartCenters = floatArrayOf(320f,373f,426f)",
    "const val lifeCountRight = 445f",
    "movement>0f && x>=birdX+48f && x<=480f",
):
    assert name in layout,name
for name in ("lifeHeartsNeverOverlapScoreFoodOrAbility",
             "onlyUpcomingMovingPillarsGetWarnings"):
    assert name in unit,name
assert "p.textAlign=Paint.Align.RIGHT" in view
assert "p.textAlign=Paint.Align.LEFT" in view
assert "game.score()" in hud and "game.foodCollected" in hud
assert "contentDescription=lastHudAccessibility" in hud
print("PASS: aligned collision-free HUD, truthful life indicators and moving pillar warnings")
