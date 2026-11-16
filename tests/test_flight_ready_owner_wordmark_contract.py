#!/usr/bin/env python3
"""Flight ready has local BOPAVI title; active frame remains clear/no fake HUD."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
s=(r/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
for marker in ("private var readyWordmark=BitmapFactory.decodeResource(resources,",
 "R.drawable.logo,BitmapFactory.Options().apply{inScaled=false})",
 "private val readyWordmarkRect=RectF(104f,120f,376f,233f)",
 "private fun drawReadyWordmark(c:Canvas) {",
 "c.drawBitmap(logo,null,readyWordmarkRect,p)",
 "if(readyWordmark==null)readyWordmark=BitmapFactory.decodeResource(resources,R.drawable.logo,opts)",
 "readyWordmark=null"):
 assert marker in s,marker
frame=s.split("if(!game.active && !game.finished) {",1)[1].split("}",1)[0]
assert "drawReadyWordmark(canvas)" in frame
assert "drawFlightReadyTutorial(canvas)" in frame
assert "if(game.active)drawGameplayReferenceHud(canvas) else drawHud(canvas)" in s
assert "PillarCollision.sweptBodyHitsPillar" not in frame
print("PASS: only real idle flight gets locally decoded wordmark, physics untouched")
