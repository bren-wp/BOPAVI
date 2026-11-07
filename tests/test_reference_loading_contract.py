#!/usr/bin/env python3
"""Native Loading visual and real level preparation must both remain truthful."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
loading=(src/"FlightLoadingView.kt").read_text(encoding="utf-8")
layout=(src/"FlightLoadingLayout.kt").read_text(encoding="utf-8")
activity=(src/"MainActivity.kt").read_text(encoding="utf-8")
qa=(root/"tools/qa_android_loading.py").read_text(encoding="utf-8")
runner=(root/"tools/qa_android_emulator.sh").read_text(encoding="utf-8")
assert "internal class FlightLoadingView(context: Context, pilotChoice: Int, private val startup:Boolean=false) : View(context)" in loading
for required in ("R.drawable.loadingback","R.drawable.logo",
                 "R.drawable.bopi0","R.drawable.bopileft0","R.drawable.bopiright0",
                 '"Loading..."',"fun completePreparation()",
                 'if(startup)"BOPAVI se pokreće" else "Loading level"',
                 "max(w / bg.width, h / bg.height)",
                 "FlightLoadingLayout.track.left"):
    assert required in loading,required
assert "val track=LoadingArtBox(161f,1350f,780f,1454f)" in layout
assert "trackWings(canvas)" in loading
assert "outer.left-3f,outer.top-4f" in loading
assert "0xff482614.toInt()" in loading
assert "paint.color=0xfffff6d2.toInt()" in loading
assert "FlightLoadingTrack.segment(inner.width()-6f," in loading
assert "android.os.SystemClock.uptimeMillis()-scanStartedAtMs," in loading
assert "preparationFinished)" in loading
assert ".61f+.07f" not in loading
assert "ring(canvas,546f,1057f,37f,54f,-21f)" in loading
assert "paint.pathEffect=" not in loading
assert "FlightLoadingLayout.captionBaselineY" in loading
assert "Thread({" in activity and '"Bopavi-Level-Preparation"' in activity
assert 'val generated=runCatching{LevelEngine.createStream(world,number)}' in activity
assert 'val perks=progress.consumePerks()' in activity
assert activity.index('val generated=runCatching{LevelEngine.createStream(world,number)}') < activity.index('val perks=progress.consumePerks()')
assert 'if(token!=loadingGeneration || selectedScreen!="loading" ||' in activity
# PR #214 intentionally replaced transient window-focus gating with Activity
# resume state, so UIAutomator dumps cannot discard a real Play tap.
prepare=activity.split("private fun prepareFlight(",1)[1].split(
    "private fun openFlight(",1)[0]
assert "if(!activityResumed)" in prepare
assert "if(!hasWindowFocus())" not in prepare
assert 'loading.completePreparation()' in activity
assert 'openFlight(simulation,world)' in activity
assert 'private var loadingGeneration = 0' in activity
assert 'loadingGeneration++' in activity
assert 'bopavi_qa_loading_preview' in activity and 'bopavi_qa_loading_preview' in qa
assert "showBrandedStartup()" in activity
assert "},2450L)" in activity
assert "startup=true" in activity
assert "PilotSelectionModel.spriteIdForChoice(pilotChoice)" in loading
assert 'android-loading.png' in qa
assert 'python3 tools/qa_android_loading.py' in runner
for forbidden in ("postDelayed(", "CountDownTimer(", "Thread.sleep(", "INTERNET", "100%", "50%"):
    assert forbidden not in loading,forbidden
assert '"showSplash()"' not in activity
print("PASS: reference Loading animation uses real preparation and cancels stale launches")
