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
play_wings=(src/"HomePlayWings.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/HomeReferenceLayoutTest.kt").read_text()
qa=(root/"tools/qa_android_emulator.sh").read_text()
assert "HomeReferenceScreen(this,progress).create(" in activity
assert "pilotIndex=selectedPilotChoice" in activity
assert "HomeReferenceLayout.supports(resources.displayMetrics.widthPixels," in activity
for box in (
    "val coins=HomeBounds(457f,21f,220f,64f)",
    "val stars=HomeBounds(705f,21f,215f,64f)",
    "val logo=HomeBounds(72f,72f,798f,351f)",
    "val hero=HomeBounds(80f,274f,680f,680f)",
    "val play=HomeBounds(190f,902f,560f,186f)",
    "val playWings=HomeBounds(112f,904f,719f,171f)",
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
# 3D owner-reference pills must be rounded and physically extruded rather
# than flat Android backgrounds; major icons must have independent art.
assert "val radius=min(h*.48f,w*.24f)" in bevel
assert "val drop=min(if(sourceHome)30f else 22f," in bevel
assert "h*(if(sourceHome).155f else .13f)" in bevel
assert "val inset=min(if(sourceHome)8f else 12f," in bevel
assert "h*(if(sourceHome).054f else .075f)" in bevel
assert "canvas.clipPath(faceClip)" in bevel
assert "sourceHome=true" in view
assert "if(sourceHome)0xff102347.toInt() else 0x77001b4e" in bevel
assert "BopaviUiIconDrawable.Kind.LEADERBOARD->podium(canvas)" in icons
assert "enamel(canvas,speaker,0xfffff9af.toInt(),0xffffae22.toInt()," in icons
assert 'wallet("STARS",earnedStars,' in view
assert 'BopaviUiIconDrawable.Kind.STAR,"Otvori kartu razina",onStars)' in view
assert 'private val logoScale=minOf(logo.w/780f,logo.h/325f)' in geometry
assert 'val logoArtwork=HomeBounds(' in geometry
assert 'val paintedTop=logoView.top+logoView.height*.12f' in view
assert "BopaviUiIconDrawable.Kind.GEM->gem(canvas)" in icons
for feature in ("LinearGradient(","canvas.drawRoundRect(",
                "ColorFilter","setAlpha(alpha:Int)","shadow:Int"):
    assert feature in bevel,feature
# All major controls use native independent 3D pictograms and outlined
# text. Rounded edge and contrast must never be replaced with emoji glyphs.
assert "HomeHeroIconDrawable(kind,dp(imageSize))" in view
assert "Measure actual painted text." in view
assert "ReferenceButtonCluster.measure(" in view
assert "textMeasure.measureText(text)" in view
assert "width=cluster.labelWidth" in view
assert "val heightFraction=if(isPlay).76f else .71f" in view
assert "if(isPlay).76f else .71f,.94f)" in view
assert "marginStart=cluster.gap" in view
assert "val titleW=(w*when(kind)" not in view
assert "HomeTopWalletRail(host)" in view
assert "walletRail.attach(wallet(" in view
rail=(src/"HomeTopWalletRail.kt").read_text()
assert "maxOf(safeTop,(it.b.y*scale).toInt())" in rail
assert "sharedTop=entries.maxOfOrNull" in rail
assert "e.child.layout(x,sharedTop,x+e.child.measuredWidth," in rail
assert "safeTop+e.b.y*scale" not in rail
assert "setPadding(0,0,0,0)" in view
assert "dp(if(isPlay)93 else 59)" not in view
assert "HomeMedalDrawable()" in view
assert "if(isWide)LinearLayout.HORIZONTAL else LinearLayout.VERTICAL" in view
assert "HomeMedalDrawable()" in view
assert "val caption=(available*.23f).toInt().coerceAtLeast(1)" in view
assert "val emblem=minOf(" in view
assert "width=emblem" in view and "height=emblem" in view
assert "LinearLayout.LayoutParams(-1,0,1f)" in view
assert "glyph(kind,40)" in view
assert "HomeEmbossedLabel(host,text,21f,false," in view
assert "footer=true)" in view
assert "HomeEmbossedLabel(host,text,font.toFloat(),isPlay," in view
assert 'Typeface.create("sans-serif-rounded",Typeface.BOLD)' in lettering
assert "ink.strokeWidth=ink.textSize*(if(footer).075f else .09f)" in lettering
for kind in ("PLAY","SHIRT","SHOP","SETTINGS","RECORDS","LEADERBOARD","SOUND"):
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
assert "HomeEmbossedLabel(host,text,font.toFloat(),isPlay," in view
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
assert "android.graphics.DashPathEffect" not in view
assert "ring(canvas,761f,672f,18f,41f,-23f)" in view
assert "ring(canvas,810f,705f,14f,31f,-17f)" in view
assert "HomePlayWings(host)" in view
assert "private fun drawWing(canvas:Canvas,mirror:Boolean)" in play_wings
assert "R.drawable.bopiportrait9" in view
assert "val chosen=PilotSelectionModel.spriteIdForChoice(pilotIndex)" in view
assert "allowMotion=!progress.lessMotion()" in view
assert "setImageResource(skinPortraitRes)" in view
assert "0xff9110e2.toInt()" in view
assert "0xff2abe1c.toInt()" in view
assert "assertTrue(HomeReferenceLayout.valid())" not in unit or "assertTrue(home.valid())" in unit
assert "assertTrue(HomeReferenceLayout.supports(941,1672,true))" in unit
assert 'capture "android-home-$label"' in qa
assert "941x1672:320:reference" in qa
assert "1,250" not in view and "85 Gems" not in view
print("PASS: 2+1 Home and three circular native shortcuts, saved balances, all pilots")
