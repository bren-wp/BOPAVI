"""Minimal V5 flight HUD. No level bar, coin wallet or fake interactive art."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
a=(root/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
i=(root/"ios/BOPAVI/GameCanvas.swift").read_text()
def get(code,start,end):
    return code.split(start,1)[1].split(end,1)[0]
a_draw=get(a,"    override fun onDraw(canvas:Canvas) {","    private fun drawWorldLighting(")
i_draw=get(i,"    override func draw(_ bound:CGRect){","    private func hudText(")
assert "drawHud(canvas)" in a_draw and "drawBoostHUD(c)" in i_draw
assert "drawLevelProgress(canvas)" not in a_draw
assert "drawLevelProgress(c)" not in i_draw
a_hud=get(a,"    private fun drawHud(c:Canvas){","    override fun onTouchEvent(")
i_hud=get(i,"    private func drawBoostHUD(_ c:CGContext) {","    private func drawWorldLighting(")
for hud in (a_hud,i_hud):
    assert "game.score()" in hud
    assert "SCORE" in hud
    assert "game.coins" not in hud and "game.stars" not in hud
    assert "walletCoins(" not in hud and "coinWallet(" not in hud and "displayLevel" not in hud
assert "hudGlass(c,18f,20f,212f,88f)" in a_hud
assert "hudGlass(c,18,20,194,68)" in i_hud
print("PASS: Android/iOS safe-area gameplay scoreboard only")
