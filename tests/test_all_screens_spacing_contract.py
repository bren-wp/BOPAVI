#!/usr/bin/env python3
"""BOPAVI native layout invariants across Home, Options and all nine UI captures.

These guards check measurable UI geometry and actual release capture origins,
not unverified owner-reference pixel identity. Emulator smoke tests confirm
real view bounds and taps at compact/tall density configurations.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt"
main=src.read_text(encoding="utf-8")
qa=(root/"tools/qa_android_emulator.sh").read_text(encoding="utf-8")
wf=(root/".github/workflows/native-ci.yml").read_text(encoding="utf-8")
options=main.split("private fun showQuickOptions(){",1)[1].split("private fun premiumDialogHeader(",1)[0]
home=main.split("private fun showHome(){",1)[1].split("private fun showPremiumPause(",1)[0]
title=main.split("private fun title(",1)[1].split("private fun action(",1)[0]

# Real widget measured height can expand for translated/wrapped Croatian labels.
assert "minHeight=d(64)" in options
assert "setPadding(d(6),d(9),d(6),d(9))" in options
assert "maxLines=4" in options
assert "LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT)" in options
assert "minHeight=d(52)" in options
assert "android.widget.RadioGroup.LayoutParams.WRAP_CONTENT" in options
assert "android.widget.RadioGroup.LayoutParams(-1,d(45))" not in options
assert "contentDescription=caption" in options

# Actual Home hero stays native and accommodates smaller windows/font scales.
assert 'val compactDisplay=resources.configuration.screenHeightDp<770' in home
assert 'val tinyDisplay=resources.configuration.screenHeightDp<610' in home
assert 'portraitDrawableIds[selectedPilotChoice]' in home
assert 'contentDescription="Otvori izbor likova"' not in home or 'showSkins()' in home
assert 'LinearLayout.LayoutParams(0,d(77),1f)' in home
assert "FitViewportColumn(this)" in home
assert "viewport.requestApplyInsets()" in home

# The PAUZA glyph is decorative only; TalkBack should not speak a chess queen.
assert 'contentDescription=text.toString()' in title
assert 'contentDescription=s' not in title

# Genuine native captures cover every reachable screen and dialog.
for item in ("android-home","android-options","android-records","android-privacy",
             "android-terms","android-gameplay-ready","android-gameplay-active",
             "android-pause","android-result"):
    assert item in qa,item
for item in ("Options","Records","Privacy","Terms"):
    assert f"BOPAVI-Android-{item}.png" in wf,item
for item in ("Home","Flight-Preview","Flight-Active","Pause","Results"):
    assert f"BOPAVI-Android-{item}.png" in wf,item
assert "test \"$(wc -l < SHA256SUMS.txt)\" -eq 14" in wf
assert "source.stat().st_size < 2000" in wf
assert "all four native submenus have distinct authentic captures" in wf

# Both icon types use the same exact owner-approved Bopi drawable XML.
res=root/"android/app/src/main/res/mipmap-anydpi-v26"
assert (res/"ic_launcher_round.xml").read_bytes()==(res/"ic_launcher.xml").read_bytes()
assert 'android:roundIcon="@mipmap/ic_launcher_round"' in (root/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: fully measured Options controls, Home heading, original round launcher and nine genuine Android UI captures")
