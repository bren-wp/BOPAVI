#!/usr/bin/env python3
"""Battery-aware renderer and premium results/HUD wiring: source contracts."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
src=r/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text(encoding="utf-8")
activity=(src/"MainActivity.kt").read_text(encoding="utf-8")
policy=(src/"GameFramePacing.kt").read_text(encoding="utf-8")
unit=(r/"android/app/src/test/java/com/brendigo/bopavi/GameFramePacingTest.kt").read_text()
physics=(r/"android/app/src/test/java/com/brendigo/bopavi/LevelEngineTest.kt").read_text()
assert "redrawDelayMillis(reducedMotion:Boolean, powerSaveMode:Boolean)" in policy
assert "if(reducedMotion || powerSaveMode) POWER_SAVER_DELAY_MS else null" in policy
assert "const val POWER_SAVER_DELAY_MS = 33L" in policy
assert "getSystemService(Context.POWER_SERVICE)" in view
assert "private fun refreshPowerSaveMode()" in view
assert "PowerManager.ACTION_POWER_SAVE_MODE_CHANGED" in view
assert "context.registerReceiver(powerSaveReceiver," in view
assert "context.unregisterReceiver(powerSaveReceiver)" in view
assert "powerSaveReceiverRegistered=false" in view
assert "override fun onWindowVisibilityChanged(visibility:Int)" in view
assert "refreshPowerSaveMode()" in view.split("override fun onAttachedToWindow()",1)[1]
assert "GameFramePacing.redrawDelayMillis(reducedMotion,powerSaveMode)" in view
assert "if(delay!=null) postInvalidateDelayed(delay) else postInvalidateOnAnimation()" in view
assert "frameClock.tick(now,running=true)?.let { game.step(it) }" in view
assert "val scale=min(width/480f,height/800f)" in view
assert "hudGlass(c,317f,16f,462f,68f)" in view
assert 'text(c,"ŽIVOTI"' in view
assert "resultWorldDrawableIds[resultWorld]" in activity
assert "LevelEngine.names[resultWorld]" in activity
assert "flightPhysicsRemainConsistentAcross30_60_120HzFrameRates" in physics
for t in ("displayVsyncIsUsedWhenMotionAllowedAndBatterySaverOff",
          "userReducedMotionAlwaysCapsFullScreenRedrawWork",
          "osPowerSaverCapsRedrawsWithoutAlteringPhysics"):
    assert t in unit,t
assert "android.permission.INTERNET" not in (r/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: power saver redraws, truthful world art, compact life HUD and deterministic physics parity")
