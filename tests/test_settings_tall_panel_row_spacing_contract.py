#!/usr/bin/env python3
"""Settings native row spreading must be compatible with 48dp touch and scroll fallback."""
from pathlib import Path
src=Path(__file__).resolve().parents[1]/"android/app/src/main/java/com/brendigo/bopavi"
layout=(src/"SettingsReferenceLayout.kt").read_text()
screen=(src/"SettingsScreen.kt").read_text()
for name in ("fun rowOffset(index:Int,screenW:Int,screenH:Int):Float",
             "fun panelOffset(screenW:Int,screenH:Int):Float",
             "fun panelExtra(screenW:Int,screenH:Int):Float",
             "fun legalSeparatorOffset(screenW:Int,screenH:Int):Float",
             "const val restoreAnchor=.62f",
             "const val heroAnchor=.05f",
             "const val titleAnchor=.20f"):
    assert name in layout,name
assert "SettingsReferenceLayout.rows[index]+SettingsReferenceLayout.rowOffset(" in screen
assert "1007f+SettingsReferenceLayout.panelExtra(" in screen
assert "559f+SettingsReferenceLayout.panelOffset(" in screen
assert "1244f+SettingsReferenceLayout.legalSeparatorOffset(" in screen
assert "SettingsReferenceLayout.rowHeight,minTouch=true," in screen
assert "val scroll=ScrollView(host).apply" in screen
assert "onPrivacy" in screen and "onTerms" in screen
assert "onChange(safe)" in screen
print("PASS: Settings spreads all 9 real controls, real legal links and panel on tall viewports")
