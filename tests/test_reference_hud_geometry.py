"""Minimal V5 flight HUD. No level bar, coin wallet or fake interactive art."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
a=(root/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
def get(code,start,end):
    return code.split(start,1)[1].split(end,1)[0]
a_draw=get(a,"    override fun onDraw(canvas:Canvas) {","    private fun drawWorldLighting(")
assert "drawHud(canvas)" in a_draw
assert "drawLevelProgress(canvas)" not in a_draw
a_hud=get(a,"    private fun drawHud(c:Canvas){","    override fun onTouchEvent(")
for hud in (a_hud,):
    assert "game.score()" in hud and "game.foodCollected" in hud
    assert "SCORE" in hud
    assert "game.coins" not in hud and "game.stars" not in hud
    assert "walletCoins(" not in hud and "coinWallet(" not in hud and "displayLevel" not in hud
assert "hudGlass(c,18f,20f,212f,88f)" in a_hud
assert "hudGlass(c,228f,20f,376f,88f)" in a_hud
print("PASS: Android safe-area gameplay scoreboard only")
