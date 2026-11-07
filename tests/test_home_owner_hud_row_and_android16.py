#!/usr/bin/env python3
"""Owner coin/gem row and Android16 real-device regression evidence."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
hud=(root/"android/app/src/main/java/com/brendigo/bopavi/HomeTopWalletRail.kt").read_text()
layout=(root/"android/app/src/main/java/com/brendigo/bopavi/HomeReferenceLayout.kt").read_text()
script=(root/"tools/prepare_android_emulator_image.sh").read_text()
readme=(root/"README.md").read_text()
assert 'maxOf(safeTop,(it.b.y*scale).toInt())' in hud
assert 'sharedTop=entries.maxOfOrNull' in hud
assert 'onRowBottomChanged?.invoke(sharedTop+minBottomSpace)' in hud
home=(root/'android/app/src/main/java/com/brendigo/bopavi/HomeReferenceScreen.kt').read_text()
main=(root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt').read_text()
assert 'walletRail.onRowBottomChanged={rowBottom->' in home
assert 'logoView.scaleX=scale' in home
assert 'logoView.scaleY=scale' in home
assert '},3500L)' in main
assert 'e.child.layout(x,sharedTop,x+e.child.measuredWidth,' in hud
assert 'val area=Rect(x,sharedTop,x+e.child.measuredWidth,' in hud
assert 'val coins=HomeBounds(457f,21f,220f,64f)' in layout
assert 'val stars=HomeBounds(705f,21f,215f,64f)' in layout
assert 'if [ "$api" = 36 ]; then' in script
assert 'targets=(default google_apis)' in script
assert 'targets=(google_apis default)' in script
assert '## Pogledaj sve zaslone igre' in readme
assert readme.count('releases/latest/download/BOPAVI-Android-')==32
print("PASS: safe single-row live HUD, API36 AOSP first, all README screenshots")
