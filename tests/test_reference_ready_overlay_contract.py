#!/usr/bin/env python3
"""Ready gameplay has an authentic life HUD and source-proportional tutorial."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text()
layout=(src/"FlightReadyOverlayLayout.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightReadyOverlayLayoutTest.kt").read_text()
assert "if(!game.finished) {" in view
assert "drawHud(canvas)" in view
assert "drawFlightReadyTutorial(canvas)" in view
# The approved Gameplay PNG uses an English cue on all device locales.
# Semantic TalkBack text is allowed to remain localized separately.
ready=view.split("private fun drawFlightReadyTutorial(c:Canvas)",1)[1].split("private fun drawTutorialText(",1)[0]
assert 'drawTutorialText(c,"Tap to fly",a.textCenterX,a.textBaselineY,21f)' in ready
assert "val croatian=" not in ready
assert "DODIRNI ZA LET" not in ready
assert "const val left=145f" in layout and "const val right=335f" in layout
assert "const val top=596f" in layout and "const val bottom=650f" in layout
assert "tutorialMatchesReferenceRelativeBounds" in unit
assert "liveHudStillFitsWhenGameIsReady" in unit
assert "vectorHandRemainsInTheReferenceCallout" in unit
assert "fun cueDecorationFits():Boolean" in layout
assert "drawTutorialHand(c,a.handCenterX,a.handCenterY)" in view
for color in ("0x6c69eaff", "0xad287fc0.toInt()",
              "0x9852b4e9.toInt()", "0x729eedff",
              "0x65031e50", "0xffb2faff.toInt()"):
    assert color in view,color
assert "3D cyan-glass CTA is only a visual cue" in view
assert "r-13f,t+10f,r-11f,b-12f,0x35ecfbff" in view
assert "rect(canvas,87f,671f,393f,746f" not in view
print("PASS: ready-state HUD and reference-sized instruction bubble")
