#!/usr/bin/env python3
"""Reference art uses the full 941x1672 grid; taps must remain cutout-safe."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
stage=(src/"LevelMapScreen.kt").read_text()
home=(src/"HomeReferenceScreen.kt").read_text()
result=(src/"GameOverScreen.kt").read_text()

assert "val referenceAligned:Boolean" in stage
assert "referenceAligned:Boolean=false" in stage
assert "private fun referenceTransform(width:Int,height:Int)" in stage
assert "private fun placement(slot:Slot,width:Int,height:Int," in stage
# Level Map keeps safe-area geometry; Shop, Skins and Settings opt into
# the reference full-bleed camera while keeping native hit targets cutout-safe.
assert "if(!slot.referenceAligned && !shopFullBleed)return safe" in stage
assert "private var shopFullBleed=false" in stage
assert "fun useShopFullBleedReferenceCamera()" in stage
shop=(src/"ShopScreen.kt").read_text()
assert "stage.useShopFullBleedReferenceCamera()" in shop
assert "if(!slot.minTouch)return aligned" in stage
assert "extraHeight*slot.verticalAnchor" in stage
assert "verticalAnchor.coerceIn(0f,1f)" in stage
for safe_dimension in (
    "x+b.left*scale>=safeLeft",
    "x+b.right*scale<=width-safeRight",
    "y+b.top*scale>=safeTop",
    "y+b.bottom*scale<=height-safeBottom",
):
    assert safe_dimension in stage,safe_dimension
assert "return if(inside)aligned else safe" in stage
assert "val (scale,_,_)=placement(slot,w,h,safe,reference)" in stage
assert "val (scale,ox,oy)=placement(slot,w,h,safe,reference)" in stage
assert "stage.place(view,b.x,b.y,b.w,b.h,minTouch=interactive," in home
assert "referenceAligned=true,verticalAnchor=verticalAnchor)" in home
assert "stage.place(HomeOrbitDecoration(host),0f,0f,941f,1672f," in home
assert "minTouch=true,referenceAligned=true)" in result
assert "stage.setSafe(" in home and "stage.setSafe(" in result
# The owner captures for Gameplay, Level Map and Shop all cover the
# complete 941x1672 viewport. Splash and reference-size Home must also be
# immersive; compact fallback Home and non-reference menus restore navigation.
activity=(src/"MainActivity.kt").read_text()
assert 'if(selectedScreen=="game") {' in activity
assert 'else if(selectedScreen=="shop" || selectedScreen=="levels" ||' in activity
assert 'ownerImmersiveReferenceScreen()' in activity
assert 'if(selectedScreen=="loading")return true' in activity
assert '"home" -> HomeReferenceLayout.supports(' in activity
assert '"skins","settings","result" -> portrait &&' in activity
assert 'stage.useShopFullBleedReferenceCamera()' in (src/"PilotSelectionScreen.kt").read_text()
assert 'stage.useShopFullBleedReferenceCamera()' in (src/"SettingsScreen.kt").read_text()
assert 'selectedScreen="skins"\n        applyPremiumSystemBars()' in activity
assert 'selectedScreen="settings"\n        applyPremiumSystemBars()' in activity
assert 'selectedScreen="splash"' not in activity
assert 'selectedScreen="loading"' in activity
assert 'selectedScreen="shop"\n        applyPremiumSystemBars()' in activity
assert 'selectedScreen="levels"\n        applyPremiumSystemBars()' in activity
assert 'android.view.WindowInsets.Type.navigationBars())' in activity
assert 'controller?.show(android.view.WindowInsets.Type.navigationBars())' in activity
assert 'View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY' in activity
assert 'selectedScreen="game"\n        applyPremiumSystemBars()' in activity
assert 'sound.stop();selectedScreen="home"\n        applyPremiumSystemBars()' in activity

print("PASS: reference-aligned art and controls; immersive flight restores navigation outside gameplay")
