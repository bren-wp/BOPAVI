#!/usr/bin/env python3
"""Owner-reference Home: authentic values, bounded live controls, no fake wallet."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
main=(root/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
icon=(root/"android/app/src/main/java/com/brendigo/bopavi/BopaviUiIconDrawable.kt").read_text()
logo=(root/"docs/assets/logo.svg").read_text()
qa=(root/"tools/qa_android_emulator.sh").read_text()
home=main.split("private fun showHome(){",1)[1].split("private fun showPremiumPause(",1)[0]
assert "val heroHeight=if(compactDisplay)" in home
assert "(resources.configuration.screenHeightDp-468).coerceIn(225,320)" in home
assert "val compactWingedBopi=when(PilotSelectionModel.spriteIdForChoice(" in home
for anchor in ("HomeWingActor(this,R.drawable.bopileft0",
               "HomeWingActor(this,R.drawable.bopileft9",
               "HomeWingActor(this,R.drawable.bopileft10",
               "R.drawable.bopiright0,R.drawable.bopi0,!progress.lessMotion())",
               "content.addView(compactWingedBopi,"):
    assert anchor in home, anchor
assert "setImageResource(R.drawable.logo)" in home
assert "progress.coins()" in home and "progress.worldStars(it)" in home
assert 'resourceChip("COINS",progress.coins(),BopaviUiIconDrawable.Kind.COIN' in home
assert 'resourceChip("STARS",stars,BopaviUiIconDrawable.Kind.STAR' in home
assert "NumberFormat.getIntegerInstance().format(count)" in home
assert 'contentDescription="$label: $count. $destination"' in home
assert 'isClickable=true;isFocusable=true' in home
assert "minimumHeight=d(48)" in home
assert "showLevels()" in home and "showShop()" in home
assert 'action(content,"▶  PLAY"){startGame()}' in home
assert "content.getChildAt(content.childCount-1) as Button" in home
assert "(resources.displayMetrics.widthPixels*.67f).toInt()" in home
assert "(resources.displayMetrics.widthPixels*.72f).toInt()" in home
assert "(resources.displayMetrics.widthPixels*.51f).toInt()" in home
assert "(resources.displayMetrics.widthPixels*.64f).toInt()" in home
assert "(resources.displayMetrics.widthPixels*.54f).toInt()" in home
assert 'shortcut("ACHIEVEMENTS",BopaviUiIconDrawable.Kind.RECORDS' in home
assert 'shortcut("LEADERBOARD",BopaviUiIconDrawable.Kind.LEADERBOARD' in home
assert 'shortcut("SOUND",BopaviUiIconDrawable.Kind.SOUND' in home
assert "content.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))" in home
assert 'HomeReferenceLayout.supports(resources.displayMetrics.widthPixels,' in home
assert 'HomeReferenceScreen(this,progress).create(' in home
for profile in ("480x800:160:small", "600x960:240:short",
                "720x1280:320:compact", "941x1672:320:reference",
                "1080x1920:420:standard", "1080x2400:420:tall",
                "1440x2960:560:large"):
    assert profile in qa,profile
# COIN and CROWN_COIN deliberately share the same vector case.
assert "Kind.COIN, Kind.CROWN_COIN ->" in icon
for kind in ("SHOP","LEADERBOARD","SOUND"):
    assert f"Kind.{kind} ->" in icon
assert "MALI LETOVI" not in logo
assert "1,250" not in home and "85 Gems" not in home
print("PASS: Home buttons match reference hierarchy and preserve live balances and reachable destinations")
