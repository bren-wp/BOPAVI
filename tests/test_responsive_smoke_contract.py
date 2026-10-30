#!/usr/bin/env python3
"""CI-only responsive emulator assertions; physical devices remain separate."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
flow=(r/".github/workflows/native-ci.yml").read_text()
qa=(r/"tools/qa_android_emulator.sh").read_text()
assert flow.count("BOPAVI_VIEWPORT_QA=1 sh tools/qa_android_emulator.sh")==2
assert "720x1280:320:compact" in qa and "1080x2400:420:tall" in qa
assert 'adb shell wm size reset' in qa and 'adb shell wm density reset' in qa
assert 'BOPAVI_RESIZE_IN_PROGRESS' in qa
assert 'min48=48*int(density)/160' in qa
assert 'n.get("scrollable")=="true"' in qa
assert 'actual==(width,height)' in qa
assert 'Settings legal control missing' in qa
assert 'for title,needle in (' in qa
assert 'touch target below 48dp' in qa
print("PASS: two real emulator viewport/density variants and fail-closed UI assertions")
