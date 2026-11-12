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
assert "referenceModeKeepsBreakpointAndLargerFontsAccessible" in (r/"android/app/src/test/java/com/brendigo/bopavi/HomeReferenceLayoutTest.kt").read_text()
assert 'adb shell wm size reset' in qa and 'adb shell wm density reset' in qa
assert 'BOPAVI_RESIZE_IN_PROGRESS' in qa
# The scroll-aware Android helper checks full native hit bounds on each page.
helper=(r/"tools/qa_home_viewports.py").read_text()
assert 'min48 = 48 * int(density) / 160' in helper
assert 'adb("shell", "input", "swipe"' in helper
assert 'set(TARGETS) <= all_found' in helper
assert 'tools/qa_home_viewports.py "$dims" "$density" "$label"' in qa
# The old assertion block targeted an inline Python heredoc that was
# replaced by the separately testable, swipe-aware real-emulator helper.
assert 'def scrollable_home(root: ET.Element) -> bool:' in helper
assert 'node.get("scrollable") == "true"' in helper
assert 'struct.unpack(">II", image[16:24]) == (width, height)' in helper
assert 'assert len(matches) <= 1' in helper
assert 'native touch target smaller than 48dp' in helper
assert 'Home targets not reachable after swipes' in helper
assert 'Reference Home unexpectedly needs scrolling' in helper
assert 'android-home-{label}-scrolled.png' in helper
assert 'python tests/test_home_viewport_a11y.py' in flow
print("PASS: seven emulator viewports and scroll-aware, fail-closed UI assertions")
