#!/usr/bin/env python3
"""Gameplay reference contract: real collectibles, top-left hearts and honest art.

No new collision type, fictional gem balance, on-screen pause target, or
modification to the 480x800 simulation / 18px body collider is permitted.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
game=(src/"GameView.kt").read_text()
simulation=(src/"GameSimulation.kt").read_text()
layout=(src/"FlightHudLayout.kt").read_text()
activity=(src/"MainActivity.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/ReferenceGameplayPickupsTest.kt").read_text()
for expression in (
    "fun coinVisible(index:Int,incoming:Boolean=false):Boolean",
    "fun crystalVisible(index:Int,incoming:Boolean=false):Boolean",
    "fun powerVisible(index:Int,incoming:Boolean=false):Boolean",
    "level.gates[index].coin && !collectedCoins[index]",
    "level.gates[index].star && !collectedStars[index]",
    "level.gates[index].power!=0 && !collectedPowers[index]",
):
    assert expression in simulation,expression
for expression in (
    "private fun drawGoldenCoin(", "private fun drawBlueCrystal(",
    "private fun drawPowerPickup(", "private fun drawGateCapInlay(",
    "if(game.coinVisible(index,incoming))drawGoldenCoin(c,mid,cy)",
    "if(game.crystalVisible(index,incoming))drawBlueCrystal(c,mid+35f,cy-25f)",
    "if(game.powerVisible(index,incoming))drawPowerPickup(c,mid+39f,cy+39f,g.power)",
    "drawGateCapInlay(c,x,w,top-28f,g.kind)",
    "drawGateCapInlay(c,x,w,bottom,g.kind)",
    "path.addCircle(h.avatarX,h.avatarY,h.avatarRadius,Path.Direction.CW)",
    "c.drawBitmap(it,null,hudPortraitRect,p)",
    "drawHudHeart(c,h.heartCenters[i],h.heartBaseline,i<lives)",
    "lastHudAccessibility=",
):
    assert expression in game,expression
for forbidden in ('gamePauseButton','showPauseIcon('):
    assert forbidden not in activity,forbidden
hud=game.split('private fun drawHud(c:Canvas){',1)[1].split('/** Developer QA outlines',1)[0]
for forbidden in ('game.score()', 'game.coins', 'cachedLivesLabel',
                  'drawHudRightText(', 'game.foodCollected', 'game.magnetTime',
                  'game.shield'):
    assert forbidden not in hud,forbidden
assert 'val lives=FlightHudBox(12f,16f,244f,84f)' in layout
assert 'val heartCenters=floatArrayOf(116f,169f,222f)' in layout
assert 'val radius = 18f' in simulation
assert 'PillarCollision.sweptBodyHitsPillar(' in simulation
assert 'onlyRealFiniteRewardsAreVisible' in unit
assert 'endlessRunDoesNotPaintFictionalFiniteRewards' in unit
assert 'abs(cx+35f-birdX) < magnetRange' in simulation
assert 'abs(cx+39f-birdX) < 30f' in simulation
print("PASS: reference gameplay paints only true collectible and lives geometry")
