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
assert 'action(content,"▶  PLAY"){startGame()}' not in home
assert 'feature("SKINS"' in home and 'feature("SHOP"' in home
assert 'text="SETTINGS"' in home
assert 'bopavi_qa_launch_flight' in activity
assert "progress.chooseArcadePilot(index)" in activity
assert "selectedPilotChoice" in home and "showSkins()" in home
assert "gamePauseButton" not in activity and "showPauseIcon(" not in activity
assert "override fun onWindowFocusChanged(hasFocus:Boolean)" in activity
# Level preparation must be gated on Activity lifecycle, not an unstable
# window-focus bit that UIAutomator can briefly clear while Home/Flight
# remains resumed. Genuine onPause still cancels background game starts.
assert "private var activityResumed = false" in activity
assert "override fun onPause() {\n        activityResumed=false" in activity
assert "override fun onResume() {\n        super.onResume()\n        activityResumed=true" in activity
prepare=activity.split("private fun prepareFlight(",1)[1].split("private fun openFlight(",1)[0]
assert "if(!activityResumed)" in prepare
assert "if(!hasWindowFocus())" not in prepare
assert "if(token!=loadingGeneration || selectedScreen!=\"loading\"" in prepare
manifest=(r/"android/app/src/main/AndroidManifest.xml").read_text()
assert 'android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|density"' in manifest
assert 'override fun onConfigurationChanged(newConfig:android.content.res.Configuration)' in activity
assert 'gameView?.requestLayout()' in activity
assert 'window.decorView.requestApplyInsets()' in activity
assert "showPauseAfterBackground=true" in activity
assert "showPremiumPause(active)" in activity
assert "FlightOverlayLayout" not in activity and "onFlightStarted" not in game
assert 'private fun showPremiumPause(game:GameView)' in activity
assert 'game.paused=true' in activity and 'showPremiumPause(view)' in activity
assert 'if(pauseDialog?.isShowing!=true)showPremiumPause(view)' in activity
assert 'selectedScreen=="game"' in activity
assert "private fun showSettingsScreen(){" in activity
assert "showQuickOptions(" not in activity
# The exported launcher may not accept debug QA intent extras in release.
assert 'private fun qaIntentEnabled(key:String):Boolean' in activity
assert 'ApplicationInfo.FLAG_DEBUGGABLE != 0' in activity
assert activity.count('intent?.getBooleanExtra(')==1
assert 'if(qaIntentEnabled("bopavi_qa_loading_preview"))' in activity
assert 'showBrandedStartup()' not in activity
assert 'else showHome()' in activity
assert 'selectedScreen="splash"' not in activity
assert '<activity android:name=".MainActivity"' in (r/"android/app/src/main/AndroidManifest.xml").read_text()
assert 'qaCaptureFirstFlap=qaIntentEnabled(' in activity
assert 'qaCollisionOverlay=qaIntentEnabled(' in activity
assert "drawStoneCourses(" in game and "fun courses(yStart" not in game
assert "targets=(google_apis default)" in install
assert "id: image35" in flow and "id: image36" in flow
result=activity.split("    private fun showResult(g:GameSimulation){",1)[1].split("    override fun onBackPressed()",1)[0]
assert result.index("sound.stop()") < result.index('sound.effect(if(g.won)"level" else "hit")')
assert 'gameView?.game?.finished == false' in activity
assert 'lastHudAccessibility="${flightHint()} Životi:' in game
assert 'contentDescription=lastHudAccessibility.ifEmpty { flightHint() }' in game
print("PASS: native lifecycle, offline audio, Back pause and minimal accessible HUD")
