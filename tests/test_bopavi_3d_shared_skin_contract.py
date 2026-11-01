#!/usr/bin/env python3
"""All native menus and gameplay HUD keep the owner's 3D vocabulary."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
source=root/"android/app/src/main/java/com/brendigo/bopavi"
theme=(source/"Bopavi3DTheme.kt").read_text()
for filename in ("ShopScreen.kt","SettingsScreen.kt","PilotSelectionScreen.kt",
                 "GameOverScreen.kt","LevelCompleteScreen.kt",
                 "AchievementsScreen.kt","LeaderboardScreen.kt","LevelMapScreen.kt"):
    assert "Bopavi3DTheme." in (source/filename).read_text(),filename
for name in ("GOLD","SKY","GREEN","VIOLET","CREAM","STONE"):
    assert "Finish."+name in theme,name
assert "RippleDrawable" in theme and "HomeEnamelDrawable(" in theme
main=(source/"MainActivity.kt").read_text()
for guard in ("background=Bopavi3DTheme.button(when {",
              "background=Bopavi3DTheme.button(finish)",
              "background=Bopavi3DTheme.button(Bopavi3DTheme.Finish.SKY)",
              "background=HomeEnamelDrawable(",
              "dialog.dismiss();action()",
              "onClick()"):
    assert guard in main,guard
flight=(source/"GameView.kt").read_text()
for guard in ("private fun hudGlass(c:Canvas",
              "private fun drawHudHeart",
              "l+12f,b-6f,r-12f,b-4f",
              "oval(c,x-5f,y-4f,x-1f,y-2f",
              "drawHud(c",
              "lastHudAccessibility"):
    assert guard in flight,guard
print("PASS: native 3D menus, pause/results and minimal enamel gameplay HUD")
