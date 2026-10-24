#!/usr/bin/env python3
"""Source-contract coverage of Results, accessibility and display-cutout geometry."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
source=(root/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text(encoding="utf-8")
start=source.split("    private fun showResult(g:GameSimulation){",1)[1].split(
    "    override fun onBackPressed()",1)[0]
back=source.split("    private fun backAction(",1)[1].split(
    "    private fun showLegalNotice(",1)[0]
assert "iconKind:BopaviUiIconDrawable.Kind" in start
assert "iconTint:Int" in start
assert "setImageDrawable(uiIcon(iconKind,iconTint,29))" in start
assert 'resultCard("REZULTAT",BopaviUiIconDrawable.Kind.STAR,gold' in start
assert 'resultCard("HRANA",BopaviUiIconDrawable.Kind.BERRY,0xffff6179.toInt()' in start
assert 'if(icon=="🍓")' not in start
assert 'or android.view.WindowInsets.Type.displayCutout())' in start
assert 'view.setPadding(bars.left,bars.top,bars.right,bars.bottom)' in start
assert 'fitted.requestApplyInsets()' in start
assert 'contentDescription=caption' in back
print("PASS: results use stable semantic pictograms, berry red, safe cutout, dynamic back label")
