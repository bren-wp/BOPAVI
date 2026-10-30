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
assert "portraitDrawableIds[selectedPilotChoice]" in home
assert "setImageResource(R.drawable.logo)" in home
assert "progress.coins()" in home and "progress.worldStars(it)" in home
assert 'resourceChip("COINS",progress.coins(),BopaviUiIconDrawable.Kind.COIN' in home
assert 'resourceChip("STARS",stars,BopaviUiIconDrawable.Kind.STAR' in home
assert "NumberFormat.getIntegerInstance().format(count)" in home
assert 'contentDescription="$label: $count. $destination"' in home
assert 'isClickable=true;isFocusable=true' in home
assert "minHeight=d(48)" in home
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
assert "for spec in 720x1280:320:compact 1080x2400:420:tall 941x1672:320:reference; do" in qa
for kind in ("COIN","SHOP","LEADERBOARD","SOUND"):
    assert f"Kind.{kind} ->" in icon
assert "MALI LETOVI" not in logo
assert "1,250" not in home and "85 Gems" not in home
print("PASS: Home buttons match reference hierarchy and preserve live balances and reachable destinations")
