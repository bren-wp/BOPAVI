#!/usr/bin/env python3
"""Regression for owner-referenced ACTIVE gameplay, while preserving real physics."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
source=root/'android/app/src/main/java/com/brendigo/bopavi'
game=(source/'GameView.kt').read_text()
activity=(source/'MainActivity.kt').read_text()
sim=(source/'GameSimulation.kt').read_text()
geometry=(source/'GameplayReferenceHudLayout.kt').read_text()
unit=(root/'android/app/src/test/java/com/brendigo/bopavi/GameplayReferenceHudLayoutTest.kt').read_text()

assert 'if(game.active)drawGameplayReferenceHud(canvas) else drawHud(canvas)' in game
assert 'val score=game.score()' in game
assert 'private val savedCoinsAtFlightStart=ProgressStore(context).coins()' in game
assert 'game.stars' in game
assert 'referenceCoinLabel' in game
assert '"SCORE"' in game
assert 'drawHudEmbossed(c,referenceScoreLabel' in game
assert 'GameplayReferenceHudLayout.hitArea(' in game
assert 'if(target==1){onPauseRequested();return true}' in game
assert 'if(target==2){onWalletRequested();return true}' in game
assert 'onPauseRequested={gameView?.let' in activity
assert 'showPremiumPause(current)' in activity
assert 'onWalletRequested={gameView?.let' in activity
assert 'confirmGameplayShop(current)' in activity
assert 'setNegativeButton("NASTAVI")' in activity
assert 'game.paused=true' in activity
assert 'game.paused=false' in activity
assert 'val coinWallet=FlightHudBox(8f,9f,119f,39f)' in geometry
assert 'val crystalWallet=FlightHudBox(126f,9f,237f,39f)' in geometry
assert 'val pause=FlightHudBox(426f,10f,475f,63f)' in geometry
assert 'pauseAndWalletHitTargetsUseActualNativeViewport' in unit
assert 'fun score(): Int' in sim
assert 'val radius = 18f' in sim

# Genuine first-world gates retain physical top/bottom and no fake pickups.
assert 'drawRoyalPillar(c,x,w,top,true,cliffSeed,spiked)' in game
assert 'drawRoyalPillar(c,x,w,bottom,false,cliffSeed+3,spiked)' in game
assert 'val base=if(upper)edge-25f else edge+25f' in game
assert 'val tip=if(upper)edge-.6f else edge+.6f' in game
assert 'drawRoyalRing(' not in game
assert 'RoyalRingTrail' not in game
assert 'RoyalRingTrail' not in sim
assert 'collectedRoyalRingBits' not in sim
assert 'if(game.coinVisible(index,incoming))drawGoldenCoin(c,mid,cy)' in game
assert 'c.skew(-.045f,0f)' in game
assert 'c.rotate(PilotFlightPose.pitchDegrees(game.velocity,reducedMotion))' in game
assert 'PilotFlightPose.wingDegrees(game.time,game.flapPulse,reducedMotion)' in game
assert 'val showGameplayObstacles=FlightHazardPresentation.obstaclesVisible(' in game
print('PASS: active HUD, native hit targets, moving pillars, no floating golden ring trails')
