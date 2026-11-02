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
for required in ("R.drawable.homeback","R.drawable.logo",
                 "R.drawable.bopi0","R.drawable.bopileft0","R.drawable.bopiright0",
                 '"Loading..."',"fun completePreparation()",
                 'if(startup)"BOPAVI se pokreće" else "Loading level"',
                 "max(w / bg.width, h / bg.height)",
                 "FlightLoadingLayout.track.left"):
    assert required in loading,required
assert "val track=LoadingArtBox(175f,1320f,766f,1422f)" in layout
assert "trackWings(canvas)" in loading
assert "ring(canvas,549f,1033f,37f,54f,-21f)" in loading
assert "paint.pathEffect=" not in loading
assert "FlightLoadingLayout.captionBaselineY" in loading
assert "Thread({" in activity and '"Bopavi-Level-Preparation"' in activity
assert 'val generated=runCatching{LevelEngine.createStream(world,number)}' in activity
assert 'val perks=progress.consumePerks()' in activity
assert activity.index('val generated=runCatching{LevelEngine.createStream(world,number)}') < activity.index('val perks=progress.consumePerks()')
assert 'if(token!=loadingGeneration || selectedScreen!="loading" ||' in activity
assert 'if(!hasWindowFocus())' in activity
assert 'loading.completePreparation()' in activity
assert 'openFlight(simulation,world)' in activity
assert 'private var loadingGeneration = 0' in activity
assert 'loadingGeneration++' in activity
assert 'bopavi_qa_loading_preview' in activity and 'bopavi_qa_loading_preview' in qa
assert "showBrandedStartup()" in activity
assert "},1450L)" in activity
assert "startup=true" in activity
assert "PilotSelectionModel.spriteIdForChoice(pilotChoice)" in loading
assert 'android-loading.png' in qa
assert 'python3 tools/qa_android_loading.py' in runner
for forbidden in ("postDelayed(", "CountDownTimer(", "Thread.sleep(", "INTERNET", "100%", "50%"):
    assert forbidden not in loading,forbidden
assert '"showSplash()"' not in activity
print("PASS: reference Loading animation uses real preparation and cancels stale launches")
