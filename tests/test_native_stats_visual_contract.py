#!/usr/bin/env python3
"""All visible results/records/options use consistent native icons and accessible stats.

This source contract complements real Android 15/16 UI hierarchy and screenshot QA.
It does not assert pixel identity without the original target-screen references.
"""
from pathlib import Path

src = Path(__file__).resolve().parents[1] / "android/app/src/main/java/com/brendigo/bopavi"
main = (src / "MainActivity.kt").read_text(encoding="utf-8")
icons = (src / "BopaviUiIconDrawable.kt").read_text(encoding="utf-8")
records = main.split("private fun showLocalRecords(){",1)[1].split("private fun showHome(){",1)[0]
result = main.split("private fun showResult(g:GameSimulation){",1)[1].split("override fun onBackPressed()",1)[0]
options = main.split("private fun showQuickOptions(){",1)[1].split("private fun showLocalRecords(){",1)[0]
pause = main.split("private fun showPremiumPause(game:GameView){",1)[1].split("private fun startGame(){",1)[0]
home = main.split("private fun showHome(){",1)[1].split("private fun showPremiumPause(",1)[0]
title = main.split("private fun title(",1)[1].split("private fun action(",1)[0]

for kind in ("STAR","BERRY","GATE","FLIGHT","WING","SETTINGS","RECORDS","PAUSE"):
    assert f"Kind.{kind}" in main, kind
for kind in ("GATE","FLIGHT","WING"):
    assert f"Kind.{kind} ->" in icons
assert "Color.alpha(paint.color), 76, 211, 137" in icons
assert "Color.argb(requestedAlpha, 76, 211, 137)" not in icons

for caption in ("Najbolji rezultat","Najviše hrane u jednom letu",
                "Najviše prođenih prepreka","Odigrani letovi",
                "Ukupni zamasi krilima"):
    assert caption in records, caption
assert "for((caption,value,kind) in rows)" in records
assert 'contentDescription="$caption: $value"' in records
assert 'setCompoundDrawablesRelativeWithIntrinsicBounds(' in records
assert 'premiumDialogHeader("MOJI REKORDI",BopaviUiIconDrawable.Kind.RECORDS)' in records
assert 'premiumDialogHeader("OPCIJE IGRE",BopaviUiIconDrawable.Kind.SETTINGS)' in options
assert "Kind.PAUSE" in title
assert 'command("▶  NASTAVI",0)' in pause
assert 'setCompoundDrawablesRelativeWithIntrinsicBounds(' in pause

assert 'recordSummaryRow("NAJBOLJI REZULTAT"' in result
assert 'recordSummaryRow("NAJVIŠE HRANE"' in result
assert 'text="NOVI REKORD!"' in result
assert "Kind.RECORDS" in result
assert 'contentDescription="$label: $value"' in result
assert 'text="★  NAJBOLJI REZULTAT' not in result
assert 'text="♛  NOVI REKORD!"' not in result
assert 'text="🍓  NAJVIŠE HRANE:' not in result
assert "🍓  Najviše hrane" not in records
assert "★  Najbolji rezultat" not in records

assert "isClickable=true" in home
assert "isFocusable=true" in home
assert "GradientDrawable().apply{setColor(Color.TRANSPARENT);cornerRadius=d(12).toFloat()}" in home
print("PASS: native score/food/record/pause icons, semantic stats and bounded tappable legal controls")
