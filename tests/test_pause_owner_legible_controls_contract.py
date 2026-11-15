#!/usr/bin/env python3
"""Native Pause: larger actual panel lettering and three working unique CTAs."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
m=(r/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
p=m.split("private fun showPremiumPause(",1)[1].split("private fun startGame(){",1)[0]
assert 'title(content,"PAUZA",if(compactPause)30 else 38,Color.WHITE)' in p
assert 'isAllCaps=false;textSize=21f' in p
assert 'setAutoSizeTextTypeUniformWithConfiguration(12,21,1,' in p
for name in ('NASTAVI','PONOVO','IZLAZ'):
 assert p.count('command("'+{'NASTAVI':'▶','PONOVO':'↻','IZLAZ':'←'}[name]+'  '+name+'"')==1,name
assert 'if(pauseActionCommitted)return@setOnClickListener' in p
assert 'PauseReplayTarget.fromStage(activeStage)' in p
assert "dialog.setCanceledOnTouchOutside(false)" in p
print("PASS: larger native Pause text; no duplicate actions or replay state mutation")
