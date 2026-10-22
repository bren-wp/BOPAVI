"""Source-level lifecycle regressions for BOPAVI's minimal Android game."""
from pathlib import Path
R=Path(__file__).resolve().parents[1]
game=(R/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
activity=(R/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
audio=(R/"android/app/src/main/java/com/brendigo/bopavi/Soundscape.kt").read_text()
flow=(R/".github/workflows/native-ci.yml").read_text()
install=(R/"tools/prepare_android_emulator_image.sh").read_text()

# No render scheduling on Home preview, paused flights, or background windows.
assert "isAttachedToWindow && game.active && !paused && !game.finished && !qaWaitingForCapture" in game
assert "GameFramePacing.redrawDelayMillis(reducedMotion,powerSaveMode)" in game
assert "FlightFrameClock()" in game and "frameClock.tick(now,running=true)" in game
assert "startSceneryDrift(" not in activity
assert "override fun onDetachedFromWindow()" in game and "override fun onAttachedToWindow()" in game
assert all(x+"=null" in game for x in ("birdBitmap","wingLeftBitmap","wingRightBitmap","worldBitmap"))
assert ".recycle()" not in game.split("override fun onDetachedFromWindow()")[1]
assert "updateWorldCover(width,height)" in game

# Audio must not touch a released SoundPool; legacy purchase SFX not preloaded.
assert "private var closed = false" in audio and "if(closed)return" in audio
assert "if(closed || !enabled || effectsVolume<=0f)return" in audio
assert '"purchase" to pool.load(' not in audio

# A removed menu must not retain a dangling Back callback or duplicate CTA click.
assert "menuBackAction" not in activity
home=activity.split("private fun showHome(){",1)[1].split("private fun showPremiumPause(",1)[0]
assert 'action(content,"▶  IGRAJ"){startGame()}' in home
# Pilot-choice feedback uses click SFX; the shared CTA action handles its own sound.
assert 'progress.chooseArcadePilot(index)' in home
assert 'gamePauseButton?.text = "▶"' in activity
assert 'gamePauseButton?.text="Ⅱ"' in activity
assert 'if(game.paused) {' in activity and 'return@setOnClickListener' in activity
assert 'if(view!=null && view.paused) showPremiumPause(view)' in activity
assert 'selectedScreen=="game" && window.decorView.hasWindowFocus()' in activity
assert 'progressTintList=ColorStateList.valueOf(0xff4cdddc.toInt())' in activity
assert 'buttonTintList=ColorStateList.valueOf(0xff80eeed.toInt())' in activity
assert "drawStoneCourses(" in game and "fun courses(yStart" not in game

# API36 only falls back to an image that is verified before emulator boot.
assert "targets=(google_apis default)" in install and "ready \"$image\"" in install
assert "id: image35" in flow and flow.count("${{ steps.image35.outputs.image_target }}")==2
assert "id: image36" in flow and "${{ steps.image36.outputs.image_target }}" in flow
print("PASS: lifecycle, bitmap ownership, audio teardown, pause and API36 fallback")

assert "currentWorld" not in activity and "currentLevel" not in activity

assert '"purchase":' not in (R/"tools/generate_audio.py").read_text()
assert '"purchase"' not in (R/"tools/verify_audio.py").read_text()
assert '"purchase"' not in (R/"tools/audit_native_dead_code.py").read_text()


# Results must not leave an audible or silent decoder looping under the static
# results screen; SoundPool feedback must remain available after music stops.
result=activity.split("    private fun showResult(g:GameSimulation){",1)[1].split("    override fun onBackPressed()",1)[0]
assert result.index("sound.stop()") < result.index('sound.effect(if(g.won)"level" else "hit")')
assert 'selectedScreen=="game" &&' in activity.split("    override fun onResume()",1)[1].split("    override fun onDestroy()",1)[0]
assert "gameView?.game?.finished == false" in activity
assert 'lastHudAccessibility="${flightHint()} Bodovi:' in game
assert 'contentDescription=lastHudAccessibility.ifEmpty { flightHint() }' in game
print("PASS: results audio focus teardown and accessible gameplay controls")
