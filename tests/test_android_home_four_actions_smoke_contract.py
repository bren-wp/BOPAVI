#!/usr/bin/env python3
"""Fail closed when either Android emulator smoke regresses to three Home CTAs.

Source-contract gate ONLY: actual accessibility and flight must still pass
on both Android 15/16 emulators. This prevents the run 43650096430 failure,
where the native XML contained all four genuine Home controls while an
outdated QA assertion demanded exactly three.
"""
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
shell = (root / "tools/qa_android_emulator.sh").read_text(encoding="utf-8")
menus = (root / "tools/qa_android_reference_menus.py").read_text(encoding="utf-8")

required = {
    "Pokreni igru",
    "Otvori izbor likova",
    "Otvori trgovinu nadogradnji",
    "Otvori opcije igre",
}
assert shell.count("Pokreni igru") >= 3

# Parse precisely the Python blocks that execute in the Android shell run;
# evaluating their literal expectation avoids false passes from comments.
home_gate = shell.split("<<'PY_HOME'\n", 1)[1].split("\nPY_HOME", 1)[0]
after_settings_gate = shell.split("FAIL: Home must have exactly four actions", 1)[0]
assert "len(buttons)==4" in after_settings_gate

for gate in (home_gate, after_settings_gate):
    values = re.findall(r'"(Pokreni igru|Otvori izbor likova|Otvori trgovinu nadogradnji|Otvori opcije igre)"', gate)
    assert required.issubset(set(values)), (set(values), required)

assert "expected==owned" in home_gate
assert "assert len(buttons)==4 and actual==expected" in shell
assert "Home must have exactly three actions" not in shell
assert "exactly three Home buttons" not in menus
assert "len(actual)==4" in menus

# Play is a real production entry point. The debug-only capture route must
# come AFTER the live no-extra PLAY test; fake QA flags must not be needed.
tap = shell.index("<<'PY_REAL_PLAY'")
flight = shell.index("<<'PY_REAL_FLIGHT'")
debug = shell.index("--ez bopavi_qa_launch_flight true")
assert tap < flight < debug
assert 'matches[0].get("bounds","")' in shell
assert 'production_play_ready=0' in shell
assert 'if len(game)==1 and not play else 1' in shell
print("PASS: four genuine Home actions and actual production PLAY launch on API35/API36 smoke")
