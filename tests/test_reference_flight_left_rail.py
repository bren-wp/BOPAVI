"""Removed legacy flight left rail must not reappear in the actual draw path."""
from pathlib import Path
p=Path(__file__).resolve().parents[1]
a=(p/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
a_hud=a.split("    private fun drawHud(c:Canvas){",1)[1].split("    override fun onTouchEvent(",1)[0]
for hud in (a_hud,):
    assert "hudOrb(" not in hud
    assert "hudGoldCoin(" not in hud
    assert "hudPilotPortrait(" not in hud
    assert "game.magnetTime" not in hud
    assert "game.shield" not in hud
    assert "game.score()" in hud
assert "game.flap()" in a
assert "override fun onTouchEvent(event:MotionEvent)" in a
print("PASS: premium Bopi endless flight without legacy currency/boost status rail")
