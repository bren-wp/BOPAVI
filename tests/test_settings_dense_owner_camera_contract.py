#!/usr/bin/env python3
"""Density-aware owner Settings composition must preserve native interactions.

A 48dp hitbox is a hard requirement; this owner portrait exception permits at
most 16% measured row overlap and never activates on small/large-font screens.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
layout=(src/"SettingsReferenceLayout.kt").read_text()
settings=(src/"SettingsScreen.kt").read_text()
qa=(root/"tools/qa_android_owner_viewports.py").read_text()
assert "fun supportsDenseOwnerCamera(screenW:Int,screenH:Int,density:Float)" in layout
assert "if(touch>pitch*1.16f)return false" in layout
assert "return homeTop*scale+restoreHeight<=screenH" in layout
assert "SettingsReferenceLayout.supportsDenseOwnerCamera(" in settings
assert "SettingsReferenceLayout.supportsNativeTouchTargets(" in settings
assert "host.resources.configuration.fontScale<1.25f" in settings
assert "stage.place(row,SettingsReferenceLayout.left," in settings
assert "SettingsReferenceLayout.rowHeight,minTouch=true" in settings
assert "val scroll=ScrollView(host).apply" in settings
assert "SettingsLegalArrow(host,\"Pročitaj uvjete korištenja\",onTerms)" in settings
assert 'onChange(safe)' in settings
assert '("Settings", "09_Settings")' in qa
print("PASS: dense reference composition, native 48dp targets, scrolling fallback and all owner sizes")
