#!/usr/bin/env python3
"""No obsolete destinations; supported Home actions use native UI routes."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
src=r/"android/app/src/main/java/com/brendigo/bopavi"
a=(src/"MainActivity.kt").read_text()
m=(src/"ReferenceMenus.kt").read_text()
assert all(s in a for s in ("private fun showHome(){",
    "private fun showSkins(){","private fun showShop(){",
    "private fun showSettingsScreen(){","showPremiumPause("))
for removed in ("showLevels(","showLocalRecords(","showLocalAchievements(",
                "showProgressOverview(","showQuickOptions("):
    assert removed not in a,removed
for s in ('onSkins={showSkins()}','onShop={showShop()}',
          'onSettings={showSettingsScreen()}'):
    assert s in a,s
assert "fun levels(" not in m
assert "fun skins(" in m and "fun shop(" in m
assert "progress.recordRun(g)" in a
assert "onBack={showHome()}" in a
assert "gamePauseButton" not in a
print("PASS: exactly supported native Home routes and retained game lifecycle")
