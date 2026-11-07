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
assert "if(!slot.referenceAligned)return safe" in stage
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
# The OS navigation strip is absent on the owner's gameplay captures.
# Hide it only while an actual live GameView owns the screen; Home/Results
# must restore system navigation and cannot be covered by swipe handles.
activity=(src/"MainActivity.kt").read_text()
assert 'if(selectedScreen=="game") {' in activity
assert 'android.view.WindowInsets.Type.navigationBars())' in activity
assert 'controller?.show(android.view.WindowInsets.Type.navigationBars())' in activity
assert 'View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY' in activity
assert 'selectedScreen="game"\n        applyPremiumSystemBars()' in activity
assert 'sound.stop();selectedScreen="home"\n        applyPremiumSystemBars()' in activity

print("PASS: reference-aligned art and controls; immersive flight restores navigation outside gameplay")
