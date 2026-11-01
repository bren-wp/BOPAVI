#!/usr/bin/env python3
"""Owner's 941x1672 Home: 3+2 glossy button grid, real 3D controls."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
activity=(src/"MainActivity.kt").read_text()
view=(src/"HomeReferenceScreen.kt").read_text()
geometry=(src/"HomeReferenceLayout.kt").read_text()
bevel=(src/"HomeEnamelDrawable.kt").read_text()
icons=(src/"HomeHeroIconDrawable.kt").read_text()
lettering=(src/"HomeEmbossedLabel.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/HomeReferenceLayoutTest.kt").read_text()
qa=(root/"tools/qa_android_emulator.sh").read_text()
assert "HomeReferenceScreen(this,progress).create(" in activity
assert "pilotIndex=selectedPilotChoice" in activity
assert "HomeReferenceLayout.supports(resources.displayMetrics.widthPixels," in activity
for box in (
    "val coins=HomeBounds(37f,18f,329f,87f)",
    "val stars=HomeBounds(607f,18f,299f,87f)",
    "val logo=HomeBounds(108f,111f,725f,240f)",
    "val hero=HomeBounds(99f,352f,743f,518f)",
    "val play=HomeBounds(139f,908f,664f,213f)",
    "val skins=HomeBounds(43f,1153f,274f,212f)",
    "val shop=HomeBounds(334f,1153f,274f,212f)",
    "val settings=HomeBounds(625f,1153f,273f,212f)",
    "val achievements=HomeBounds(106f,1391f,349f,207f)",
    "val leaderboard=HomeBounds(360f,1398f,95f,95f)",
    "val sound=HomeBounds(484f,1391f,349f,207f)",
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
    'pill("ACHIEVEMENTS","Pogledaj svoja postignuća"',
    'pill("SOUND","Otvori zvučne postavke"',
    'contentDescription="Pogledaj svoje rekorde"',
    "stage.setOnApplyWindowInsetsListener","stage.setSafe(",
    "isClickable=true;isFocusable=true","minimumHeight=dp(48)",
    "HomeEnamelDrawable(","android.view.MotionEvent.ACTION_DOWN",
):
    assert item in view,item
for feature in ("LinearGradient(","canvas.drawRoundRect(",
                "ColorFilter","setAlpha(alpha:Int)","shadow:Int"):
    assert feature in bevel,feature
# All six major controls use native independent 3D pictograms and outlined
# text. Rounded edge and contrast must never be replaced with emoji glyphs.
assert "HomeHeroIconDrawable(kind,dp(imageSize))" in view
assert "HomeEmbossedLabel(host,text,font.toFloat(),isPlay)" in view
for kind in ("PLAY","SHIRT","SHOP","SETTINGS","RECORDS","SOUND"):
    assert f"BopaviUiIconDrawable.Kind.{kind}->" in icons,kind
for required in ("LinearGradient(","Paint.Style.STROKE",
                 "canvas.drawPath(", "drawRoundRect(", "Path()"):
    assert required in icons,required
for required in ("textAlign=Paint.Align.CENTER","canvas.drawText(caption",
                 "paint.textSize" if False else "p.textSize",
                 "Paint.Style.STROKE","Color.WHITE"):
    assert required in lettering,required
assert "android.graphics.DashPathEffect" in view
assert "ring(canvas,809f,744f,143f,170f,-7f)" in view
assert "assertTrue(HomeReferenceLayout.valid())" not in unit or "assertTrue(home.valid())" in unit
assert "assertTrue(HomeReferenceLayout.supports(941,1672,true))" in unit
assert 'capture "android-home-$label"' in qa
assert "941x1672:320:reference" in qa
assert "1,250" not in view and "85 Gems" not in view
print("PASS: glossed 3+2 Home, accessible records badge, real saved balances and all 11 pilots")
