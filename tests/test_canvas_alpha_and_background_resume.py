"""Guard Canvas ARGB opacity and the one-time native background pause recovery."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
src = root / "android/app/src/main/java/com/brendigo/bopavi"
view = (src / "GameView.kt").read_text()
main = (src / "MainActivity.kt").read_text()
qa = (root / "tools/qa_android_background.py").read_text()
runner = (root / "tools/qa_android_emulator.sh").read_text()
ci = (root / ".github/workflows/native-ci.yml").read_text()

fill = view.split("private fun fill(color: Int)", 1)[1].split("private fun rect(", 1)[0]
assert "p.shader=null" in fill
assert "p.color=color" in fill
assert "p.style=Paint.Style.FILL" in fill
assert "p.alpha=255" not in fill, "ARGB alpha must not be overwritten"
assert "drawPolishedGateLip(c,x,w,top-28f)" in view
assert "drawPolishedGateLip(c,x,w,bottom)" in view
assert "val color=(alpha shl 24)" in view, "Moving pillar alpha must be dynamic"
assert "0x44031835" in view and "0xe8092949.toInt()" in view
assert "0x552c4b73" in view, "Missing lives must remain translucent"

assert "private var showPauseAfterBackground = false" in main
paused = main.split("override fun onPause()",1)[1].split("override fun onDestroy()",1)[0]
assert "active.paused=true" in paused
assert "showPauseAfterBackground=true" in paused
assert "override fun onWindowFocusChanged(hasFocus:Boolean)" in paused
assert "if(hasFocus && showPauseAfterBackground)" in paused
assert paused.index("showPauseAfterBackground=false") < paused.index("showPremiumPause(active)")
assert "showPauseAfterBackground=false" in main.split("private fun showHome(){",1)[1]
assert "showPauseAfterBackground=false" in main.split("private fun startGame(){",1)[1]
assert "gamePauseButton" not in main, "No in-flight Pause icon may return"

assert 'keyevent", "3"' in qa and 'one_control("NASTAVI")' in qa
# A renamed gameplay accessibility prompt must be synchronized with the
# real emulator test, otherwise the test fails before testing backgrounding.
assert 'one_control("DODIRNI ZA ZAMAH KRILIMA")' in qa
assert 'one_control("DODIRNI ZA LET")' not in qa
assert 'DODIRNI ZA ZAMAH KRILIMA' in view
assert "before = active_flight().get(" in qa
assert 'resumed.get("content-desc") == before' in qa
assert "python3 tools/qa_android_background.py" in runner
assert "python tests/test_canvas_alpha_and_background_resume.py" in ci
print("PASS: opacity preserved and background/foreground cannot strand flight")
