#!/usr/bin/env python3
"""Source gate: two real native captures on every Android density/size profile."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
shell=(root/"tools/qa_android_emulator.sh").read_text()
activity=(root/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
splash=(root/"tools/qa_splash_viewports.py").read_text()
responsive=(root/"tools/qa_responsive_home_splash.py").read_text()

specs=(
    "480x800:160:small","600x960:240:short","720x1280:320:compact",
    "941x1672:320:reference","1080x1920:420:standard",
    "1080x2400:420:tall","1440x2960:560:large"
)
for profile in specs:
    assert profile in shell,profile
    label=profile.split(":")[-1]
    assert f'"{label}": ' in responsive
assert 'capture "android-home-$label"' in shell
assert 'capture "android-loading-$label"' in shell
assert shell.index('capture "android-home-$label"') < shell.index(
    'capture "android-loading-$label"')
assert 'python3 tools/qa_home_viewports.py "$dims" "$density" "$label"' in shell
assert 'python3 tools/qa_splash_viewports.py "$dims" "$density" "$label"' in shell
assert '--ez bopavi_qa_loading_preview true' in shell
assert 'if(intent?.getBooleanExtra("bopavi_qa_loading_preview",false)==true)' in activity
assert 'showNativeView(FlightLoadingView(this,selectedPilotChoice))' in activity
assert 'android-loading-{label}.png' in splash
assert 'android-loading-{label}.xml' in splash
assert 'content-desc' in splash
assert "image.format == \"PNG\"" in splash
assert 'ImageOps.fit(owner, size' in responsive
assert 'reference.fit' not in responsive
assert '"all_devices_meet_target"' in responsive
assert '--enforce-target' in responsive
assert 'TARGET_MAX_RGB_MAE' in responsive
assert "screenshots_are_untouched" in responsive
assert 'adb shell wm size reset' in shell
assert 'adb shell wm density reset' in shell
print("PASS: real Home+Splash screenshots at seven Android viewport sizes, "
      "minimum native touch targets, no fake screen or percentages")
