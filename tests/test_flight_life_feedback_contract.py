"""Keep physical pickup and life-loss feedback, but no extra status panels."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
src=r/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text()
layout=(src/"FlightHudLayout.kt").read_text()
assert not (src/"FlightHudFeedback.kt").exists()
assert "flightHudFeedback" not in view and "drawHudCaption" not in view
assert "notice" not in layout and "ability = FlightHudBox" not in layout
assert "drawHudHeart(" in view
assert "if(game.impactPulse>0f)" in view
assert "if(game.collectPulse>0f)" in view
assert "FlightHudReadability.isFinalLife" in view
assert "android.permission.INTERNET" not in (r/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: lives-only HUD and true physical feedback without redundant notice chips")
