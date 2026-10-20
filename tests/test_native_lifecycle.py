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
assert "if(reducedMotion) postInvalidateDelayed(33L) else postInvalidateOnAnimation()" in game
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
assert 'startGame()' in home and 'sound.effect("click")' not in home
assert 'gamePauseButton?.text = "▶"' in activity
assert 'gamePauseButton?.text="Ⅱ"' in activity

# API36 only falls back to an image that is verified before emulator boot.
assert "targets+=(default)" in install and "ready \"$image\"" in install
assert "id: image36" in flow and "${{ steps.image36.outputs.image_target }}" in flow
print("PASS: lifecycle, bitmap ownership, audio teardown, pause and API36 fallback")

assert "currentWorld" not in activity and "currentLevel" not in activity

assert '"purchase":' not in (R/"tools/generate_audio.py").read_text()
assert '"purchase"' not in (R/"tools/verify_audio.py").read_text()
assert '"purchase"' not in (R/"tools/audit_native_dead_code.py").read_text()

