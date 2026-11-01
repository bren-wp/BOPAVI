#!/usr/bin/env python3
"""Owner's 941x1672 Home: two-wide, Settings and three native footer CTAs."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
activity=(src/"MainActivity.kt").read_text()
view=(src/"HomeReferenceScreen.kt").read_text()
geometry=(src/"HomeReferenceLayout.kt").read_text()
bevel=(src/"HomeEnamelDrawable.kt").read_text()
icons=(src/"HomeHeroIconDrawable.kt").read_text()
lettering=(src/"HomeEmbossedLabel.kt").read_text()
wing=(src/"HomeWingActor.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/HomeReferenceLayoutTest.kt").read_text()
qa=(root/"tools/qa_android_emulator.sh").read_text()
assert "HomeReferenceScreen(this,progress).create(" in activity
assert "pilotIndex=selectedPilotChoice" in activity
assert "HomeReferenceLayout.supports(resources.displayMetrics.widthPixels," in activity
for box in (
    "val coins=HomeBounds(457f,21f,220f,64f)",
    "val stars=HomeBounds(705f,21f,215f,64f)",
    "val logo=HomeBounds(122f,112f,697f,273f)",
    "val hero=HomeBounds(96f,391f,745f,483f)",
    "val play=HomeBounds(149f,894f,645f,191f)",
    "val skins=HomeBounds(135f,1097f,325f,137f)",
    "val shop=HomeBounds(476f,1097f,325f,137f)",
    "val settings=HomeBounds(243f,1252f,453f,121f)",
    "val achievements=HomeBounds(208f,1452f,166f,142f)",
    "val leaderboard=HomeBounds(388f,1452f,166f,142f)",
    "val sound=HomeBounds(568f,1452f,166f,142f)",
):
    assert box in geometry,box
for item in (
    "R.drawable.homeback","R.drawable.logo",
    "R.drawable.bopileft0","R.drawable.bopiright0","R.drawable.bopi0",
    "left[chosen],right[chosen],bodies[chosen]",
    "progress.coins()","progress.worldStars(it)",
    "HomeOrbitDecoration(host)","HomeReferenceLayout.play",
    'pill("PLAY","▶  PLAY"','pill("SKINS","Otvori izbor likova"',
    'pill("SHOP","Otvori trgovinu nadogradnji"',
    'pill("SETTINGS","Otvori opcije igre"',
    'footerShortcut("ACHIEVEMENTS",',
    'footerShortcut("LEADERBOARD",',
    'footerShortcut("SOUND",',
    '"Pogledaj svoje rekorde",BopaviUiIconDrawable.Kind.LEADERBOARD',
    "stage.setOnApplyWindowInsetsListener","stage.setSafe(",
    "isClickable=true;isFocusable=true","minimumHeight=dp(48)",
    "HomeEnamelDrawable(","android.view.MotionEvent.ACTION_DOWN",
):
    assert item in view,item
for feature in ("LinearGradient(","canvas.drawRoundRect(",
                "ColorFilter","setAlpha(alpha:Int)","shadow:Int"):
    assert feature in bevel,feature
# All major controls use native independent 3D pictograms and outlined
# text. Rounded edge and contrast must never be replaced with emoji glyphs.
assert "HomeHeroIconDrawable(kind,dp(imageSize))" in view
assert "if(isWide)LinearLayout.HORIZONTAL else LinearLayout.VERTICAL" in view
assert "GradientDrawable.OVAL" in view
assert "LinearLayout.LayoutParams(dp(48),0,1f)" in view
assert "HomeEmbossedLabel(host,text,15f,false)" in view
assert "HomeEmbossedLabel(host,text,font.toFloat(),isPlay)" in view
for kind in ("PLAY","SHIRT","SHOP","SETTINGS","RECORDS","SOUND"):
    assert f"BopaviUiIconDrawable.Kind.{kind}->" in icons,kind
for required in ("LinearGradient(","Paint.Style.STROKE",
                 "canvas.drawPath(", "drawRoundRect(", "Path()"):
    assert required in icons,required
for required in ("internal class HomeEmbossedLabel(", ":TextView(context)",
                 "text=caption", "importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES",
                 "isClickable=false", "isFocusable=false",
                 "textAlign=Paint.Align.CENTER", "canvas.drawText(caption",
                 "ink.textSize", "Paint.Style.STROKE", "Color.WHITE"):
    assert required in lettering,required
# The emulator requires exactly one text-bearing SKINS/SHOP child inside
# the actual clickable 3D button, not a silent custom Canvas View.
assert "HomeEmbossedLabel(host,text,font.toFloat(),isPlay)" in view
assert "HomeWingActor(host," in view
for anchor in ("left[chosen],right[chosen],bodies[chosen]",
               "addView(leftWing,LayoutParams(-1,-1))",
               "addView(rightWing,LayoutParams(-1,-1))",
               "addView(body,LayoutParams(-1,-1))",
               "ValueAnimator.areAnimatorsEnabled()",
               "onWindowVisibilityChanged(visibility:Int)",
               "onDetachedFromWindow()", "stopFlapping()",
               "leftFlap.cancel()", "rightFlap.cancel()"):
    assert anchor in (view+wing),anchor
assert '("SKINS","Otvori izbor likova")' in qa
assert '("SHOP","Otvori trgovinu nadogradnji")' in qa
assert 'n.get("text","").strip().upper()==menu' in qa
assert "android.graphics.DashPathEffect" in view
assert "ring(canvas,809f,744f,143f,170f,-7f)" in view
assert "assertTrue(HomeReferenceLayout.valid())" not in unit or "assertTrue(home.valid())" in unit
assert "assertTrue(HomeReferenceLayout.supports(941,1672,true))" in unit
assert 'capture "android-home-$label"' in qa
assert "941x1672:320:reference" in qa
assert "1,250" not in view and "85 Gems" not in view
print("PASS: 2+1 Home and three circular native shortcuts, saved balances, all pilots")
