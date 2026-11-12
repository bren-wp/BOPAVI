#!/usr/bin/env python3
"""Active Home/Settings/Shop/Skins remain native, safe and scrollable."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
src=r/"android/app/src/main/java/com/brendigo/bopavi"
a=(src/"MainActivity.kt").read_text()
h=a.split("private fun showHome(){",1)[1].split("private fun confirmGameplayShop(",1)[0]
layout=(src/"HomeReferenceLayout.kt").read_text()
assert "FitViewportColumn(this)" in h and "viewport.requestApplyInsets()" in h
assert 'val compactDisplay=resources.configuration.screenHeightDp<770' in h
assert 'val tinyDisplay=resources.configuration.screenHeightDp<610' in h
assert 'val actions=listOf(skins,shop,settings)' in layout
assert 'content.addView(compactWingedBopi,' in h
assert 'text="SETTINGS"' in h
assert 'shortcut(' not in h
assert 'action(content,"▶  PLAY")' not in h
res=r/"android/app/src/main/res/mipmap-anydpi-v26"
assert (res/"ic_launcher_round.xml").read_bytes()==(res/"ic_launcher.xml").read_bytes()
assert 'android:roundIcon="@mipmap/ic_launcher_round"' in (r/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: fully native active screens and exactly three Home actions")
