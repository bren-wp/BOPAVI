#!/usr/bin/env python3
"""Android15 rotation QA regression: actual user PLAY, no mid-flight restart.

The previous real emulator test captured landscape Home at 2400x1080, then
force-stopped/launched the app; its next GameView capture was PORTRAIT
1080x2400. The stale landscape size caused a false clipped-touch assertion.
This source contract guards the corrected UI test, not a fake emulator pass.
"""
import ast
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=(root/"tools/qa_android_rotation.py").read_text(encoding="utf-8")
ast.parse(src)
assert 'w, h = rotate(1)' in src
assert 'portrait_w, portrait_h = rotate(0)' in src
session=src.split("w, h = rotate(1)",1)[1].split(
    "portrait_w, portrait_h = rotate(0)",1)[0]
assert 'tap(play[0])' in session, "Must launch actual flight with visible PLAY"
assert 'n.get("content-desc")=="Pokreni igru"' in session
assert 'force-stop' not in session, "Do not reset Android orientation mid-test"
assert 'bopavi_qa_launch_flight' not in src, "Do not use debug-only direct launch"
assert 'bopavi_qa_capture_first_flap' in src, (
    "Genuine first-flap game state needs deterministic capture, not a fake run")
assert 'native_w,native_h=screenshot("android-flight-landscape")' in session
assert '(native_w,native_h)==(w,h) and native_w>native_h' in session
assert 'assert adb("shell","pidof",APP)==initial_pid' in session
assert 'assert_flight_visible(active, measured_w, measured_h)' in session
assert 'android-rotation-current-ui.xml' in src
assert '"bounds":(left,top,right,bottom)' in src
print("PASS: landscape native PLAY keeps Activity/process, checks true pixels and touch bounds")
