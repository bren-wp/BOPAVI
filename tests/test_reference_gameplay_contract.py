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
    "c.drawBitmap(portrait,hudFaceSource,hudPortraitRect,p)",
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
assert 'val lives=FlightHudBox(18f,10f,181f,61f)' in layout
assert 'val heartCenters=floatArrayOf(83f,119f,155f)' in layout
# The owner's 00_32_52-2 Gameplay reference has gold crown coins,
# faceted sapphires, a linked bronze mace and bright wind behind the bird.
# These layers MUST follow real pickups and the physical 18px hazard radius.
assert 'oval(c,x-14f,y-14f,x+14f,y+14f,0xffdf8f0b.toInt())' in game
assert 'c.drawArc(x-12f,y-12f,x+12f,y+12f,201f,148f,false,p)' in game
assert 'path.moveTo(x-7f,y-3f);path.lineTo(x-4f,y+2f)' in game
assert 'c.drawLine(x,y-18f,x,y+16f,p)' in game
assert 'fill(0xffe83bfa.toInt());c.drawPath(path,p)' in game
assert 'fill(0xfff7b6ff.toInt());c.drawPath(path,p)' in game
assert 'c.drawLine(x-12f,y,x+12f,y,p)' in game
assert 'for(link in 1..3)' in game
assert 'val r=SkyMaceHazard.radius' in game
assert 'path.lineTo(cx+dx*r,cy+dy*r)' in game
assert 'c.drawCircle(lx,ly,2.8f,p)' in game
assert 'path.cubicTo(game.birdX-88f,game.y+15f,' in game
# The lives-only HUD now uses two layered enamel highlights. Ensure
# the heart remains state-dependent, outlined, and visibly dimensional.
assert 'if(active){' in game
assert 'oval(c,x-7f,y-4f,x+4f,y+3f,0x88ffad98.toInt())' in game
assert 'oval(c,x-5f,y-4f,x-1f,y-2f,0xccffffff.toInt())' in game
# Revised owner 3D heart extrusion is deeper without moving the actual HUD.
assert 'c.translate(0f,3.5f)' in game
assert 'if(active)0xff7e1836.toInt() else 0x55304460' in game
assert 'oval(c,x-3f,y+2f,x+5f,y+4.5f,0x66ffbba1)' in game
assert 'rect(c,l+12f,b-6f,r-12f,b-4f,0x77021944,2f)' in game
assert 'SkyMaceHazard.sweptHit(' in simulation
assert 'if(game.coinVisible(index,incoming))drawGoldenCoin(c,mid,cy)' in game
assert 'if(game.crystalVisible(index,incoming))drawBlueCrystal(c,mid+35f,cy-25f)' in game
assert 'if(!reducedMotion && game.active && !game.finished)' in game
assert 'drawHud(canvas)' in game
assert 'val radius = 18f' in simulation
assert 'if(!game.active && !game.finished) {' in game
assert 'drawFlightReadyTutorial(canvas)' in game
assert 'if(!game.finished) {' in game
assert 'drawHud(canvas)' in game
assert 'drawTutorialText(c,"Tap to fly",a.textCenterX,a.textBaselineY,21f)' in game
assert 'drawTutorialHand(c,a.handCenterX,a.handCenterY)' in game
assert 'drawTutorialText(c,"DODIRNI ZA LET"' not in game
assert 'Dodirni za zamah krilima.' in game
assert 'FlightReadyOverlayLayout' in game
assert 'PillarCollision.sweptBodyHitsPillar(' in simulation
assert 'onlyRealFiniteRewardsAreVisible' in unit
assert 'endlessRunDoesNotPaintFictionalFiniteRewards' in unit
assert 'abs(cx+35f-birdX) < magnetRange' in simulation
assert 'abs(cx+39f-birdX) < 30f' in simulation
# Raised crown-coin depth and mace spike highlights must remain inside
# existing physical pickup/hazard extents: purely visual, no new hit target.
assert 'c.drawArc(x-14f,y-14f,x+14f,y+14f,28f,150f,false,p)' in game
assert 'c.drawArc(x-13f,y-13f,x+13f,y+13f,197f,104f,false,p)' in game
assert 'c.drawLine(cx+dx*13f+px*2f,cy+dy*13f+py*2f,' in game
assert 'cx+dx*(r-2f),cy+dy*(r-2f),p)' in game
print("PASS: reference gameplay paints only true collectible and lives geometry")
