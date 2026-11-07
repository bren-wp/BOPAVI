#!/usr/bin/env python3
"""The 0% comparator must consume real exact-size Android captures."""
from pathlib import Path
repo=Path(__file__).resolve().parents[1]
qa=(repo/"tools/qa_android_emulator.sh").read_text()
strict=(repo/"tools/qa_owner_zero_pixel.py").read_text()
assert "adb shell wm size 864x1536" in qa
assert "capture android-home-owner-native-864" in qa
assert "capture android-loading-owner-native-864" in qa
assert '"▶  PLAY" in accessible and "Otvori opcije igre" in accessible' in qa
assert 'n.get("content-desc")=="Loading level"' in qa
assert "assert struct.unpack(\">II\",data[16:24])==(864,1536)" in qa
assert "adb shell wm size reset" in qa
assert "adb shell wm density reset" in qa
assert "BOPAVI_RESIZE_IN_PROGRESS=0" in qa
assert 'SIZE = (864, 1536)' in strict
assert 'different_pixels' in strict and 'zero_pixel_deviation' in strict
assert 'ImageChops.difference(reference, actual)' in strict
assert '.resize(' not in strict
print("PASS: original Android owner-resolution screenshots available for exact 0% gate")
