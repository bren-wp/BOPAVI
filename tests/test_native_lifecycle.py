"""Native lifecycle, audio safety, HUD removal and Android emulator safeguards."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
src=r/"android/app/src/main/java/com/brendigo/bopavi"
game=(src/"GameView.kt").read_text()
activity=(src/"MainActivity.kt").read_text()
audio=(src/"Soundscape.kt").read_text()
flow=(r/".github/workflows/native-ci.yml").read_text()
install=(r/"tools/prepare_android_emulator_image.sh").read_text()
assert "isAttachedToWindow && game.active && !paused && !game.finished && !qaWaitingForCapture" in game
assert "GameFramePacing.redrawDelayMillis(reducedMotion,powerSaveMode)" in game
assert "FlightFrameClock()" in game and "frameClock.tick(now,running=true)" in game
assert "override fun onDetachedFromWindow()" in game and "override fun onAttachedToWindow()" in game
assert all(x+"=null" in game for x in ("birdBitmap","wingLeftBitmap","wingRightBitmap","worldBitmap"))
assert ".recycle()" not in game.split("override fun onDetachedFromWindow()")[1]
assert "updateWorldCover(width,height)" in game
assert "private var closed = false" in audio and "if(closed)return" in audio
assert "if(closed || !enabled || effectsVolume<=0f)return" in audio
home=activity.split("private fun showHome(){",1)[1].split("private fun showPremiumPause(",1)[0]
assert 'action(content,"▶  PLAY"){startGame()}' in home
assert "progress.chooseArcadePilot(index)" in activity
assert "selectedPilotChoice" in home and "showSkins()" in home
assert "gamePauseButton" not in activity and "showPauseIcon(" not in activity
assert "override fun onWindowFocusChanged(hasFocus:Boolean)" in activity
assert "showPauseAfterBackground=true" in activity
assert "showPremiumPause(active)" in activity
assert "FlightOverlayLayout" not in activity and "onFlightStarted" not in game
assert 'private fun showPremiumPause(game:GameView)' in activity
assert 'view.paused=true' in activity and 'showPremiumPause(view)' in activity
assert 'selectedScreen=="game"' in activity
assert 'progressTintList=ColorStateList.valueOf(0xff4cdddc.toInt())' in activity
assert 'buttonTintList=ColorStateList.valueOf(0xff80eeed.toInt())' in activity
assert "drawStoneCourses(" in game and "fun courses(yStart" not in game
assert "targets=(google_apis default)" in install
assert "id: image35" in flow and "id: image36" in flow
result=activity.split("    private fun showResult(g:GameSimulation){",1)[1].split("    override fun onBackPressed()",1)[0]
assert result.index("sound.stop()") < result.index('sound.effect(if(g.won)"level" else "hit")')
assert 'gameView?.game?.finished == false' in activity
assert 'lastHudAccessibility="${flightHint()} Životi:' in game
assert 'contentDescription=lastHudAccessibility.ifEmpty { flightHint() }' in game
print("PASS: native lifecycle, offline audio, Back pause and minimal accessible HUD")
