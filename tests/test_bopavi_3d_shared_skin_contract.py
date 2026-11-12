#!/usr/bin/env python3
"""All native menus and gameplay HUD keep the owner's 3D vocabulary."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
source=root/"android/app/src/main/java/com/brendigo/bopavi"
theme=(source/"Bopavi3DTheme.kt").read_text()
# Shared enamel theme is used in these six menu screens.
for filename in ("ShopScreen.kt","SettingsScreen.kt","PilotSelectionScreen.kt"):
    assert "Bopavi3DTheme." in (source/filename).read_text(),filename
# Results screens deliberately use their own native reference artwork and
# still require real accessible controls; do not force an unused theme call.
for filename in ("GameOverScreen.kt","LevelCompleteScreen.kt"):
    screen=(source/filename).read_text()
    for marker in ("BopaviUiIconDrawable.Kind.","setOnClickListener{onClick()}",
                   "contentDescription=","GradientDrawable("):
        assert marker in screen,(filename,marker)
for name in ("GOLD","SKY","GREEN","VIOLET","CREAM","STONE"):
    assert "Finish."+name in theme,name
assert "RippleDrawable" in theme and "HomeEnamelDrawable(" in theme
enamel=(source/"HomeEnamelDrawable.kt").read_text()
for state in ("override fun isStateful():Boolean=true",
              "override fun onStateChange(state:IntArray):Boolean",
              "android.R.attr.state_pressed in state",
              "android.R.attr.state_selected in state",
              "android.R.attr.state_enabled in state",
              "if(!enabled)canvas.saveLayerAlpha(",
              "if(pressed || selected)", "invalidateSelf()"):
    assert state in enamel,state
main=(source/"MainActivity.kt").read_text()
for guard in ("background=Bopavi3DTheme.button(when {",
              "background=Bopavi3DTheme.button(finish)",
              "background=Bopavi3DTheme.button(Bopavi3DTheme.Finish.SKY)",
              "background=HomeEnamelDrawable(",
              "if(pauseActionCommitted)return@setOnClickListener",
              "pauseActionCommitted=true",
              "isEnabled=false",
              "dialog.dismiss()",
              "action()",
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
