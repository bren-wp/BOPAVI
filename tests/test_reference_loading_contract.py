#!/usr/bin/env python3
"""Reference Loading must be a truthful, native, cancellable runtime state."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
loading=(src/"FlightLoadingView.kt").read_text(encoding="utf-8")
activity=(src/"MainActivity.kt").read_text(encoding="utf-8")
qa=(root/"tools/qa_android_loading.py").read_text(encoding="utf-8")
runner=(root/"tools/qa_android_emulator.sh").read_text(encoding="utf-8")
gradle=(root/"android/app/build.gradle.kts").read_text(encoding="utf-8")

assert 'internal class FlightLoadingView(context: Context, pilotChoice: Int) : View(context)' in loading
assert 'R.drawable.homeback' in loading
assert 'R.drawable.logo' in loading
assert 'R.drawable.bopiportrait0' in loading
assert 'RectF(139f, 1333f, 802f, 1441f)' in loading
assert '"Loading..."' in loading
assert 'if (preparationFinished)' in loading
assert 'fun completePreparation()' in loading
assert 'contentDescription = "Loading level"' in loading
assert "val scale = min(w / 941f, h / 1672f)" in loading
assert 'max(w / bg.width, h / bg.height)' in loading
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
assert 'android-loading.png' in qa
assert 'python3 tools/qa_android_loading.py' in runner
assert 'versionCode = 169' in gradle and 'versionName = "1.0.39"' in gradle

# Must not fake a timed progress countdown, require network, or replace the
# one-system-splash cold start with an artificial second splash.
for forbidden in ('postDelayed(', 'CountDownTimer(', 'Thread.sleep(', 'INTERNET', '100%', '50%'):
    assert forbidden not in loading
assert '"showSplash()"' not in activity
assert 'if(intent?.getBooleanExtra("bopavi_qa_loading_preview",false)==true)' in activity
print("PASS: native reference Loading uses real level preparation, cancellation and saved perks safely")
