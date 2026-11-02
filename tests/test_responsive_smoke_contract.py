#!/usr/bin/env python3
"""CI-only responsive emulator assertions; physical devices remain separate."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
flow=(r/".github/workflows/native-ci.yml").read_text()
qa=(r/"tools/qa_android_emulator.sh").read_text()
assert flow.count("BOPAVI_VIEWPORT_QA=1 sh tools/qa_android_emulator.sh")==2
for device in ("480x800:160:small","600x960:240:short",
               "720x1280:320:compact","941x1672:320:reference",
               "1080x1920:420:standard","1080x2400:420:tall",
               "1440x2960:560:large"):
    assert device in qa,device
layout=(r/"android/app/src/main/java/com/brendigo/bopavi/HomeReferenceLayout.kt").read_text()
activity=(r/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
assert "fontScale>=.85f && fontScale<1.3f" in layout
assert "resources.configuration.fontScale" in activity
assert "screenDensityAndLargeFontBreakpointMatrix" in (r/"android/app/src/test/java/com/brendigo/bopavi/HomeReferenceLayoutTest.kt").read_text()
assert 'adb shell wm size reset' in qa and 'adb shell wm density reset' in qa
assert 'BOPAVI_RESIZE_IN_PROGRESS' in qa
assert 'min48=48*int(density)/160' in qa
assert 'n.get("scrollable")=="true"' in qa
assert 'actual==(width,height)' in qa
assert 'Settings legal control missing' in qa
assert 'for title,needle in (' in qa
assert 'touch target below 48dp' in qa
print("PASS: two real emulator viewport/density variants and fail-closed UI assertions")
