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
    assert "BODOVI" in hud
    assert "game.coins" not in hud and "game.stars" not in hud
    assert "walletCoins(" not in hud and "coinWallet(" not in hud and "displayLevel" not in hud
for required in (
    "val h=FlightHudLayout",
    "hudGlass(c,h.score.left,h.score.top,h.score.right,h.score.bottom)",
    "hudGlass(c,h.food.left,h.food.top,h.food.right,h.food.bottom)",
    "hudGlass(c,h.lives.left,h.lives.top,h.lives.right,h.lives.bottom)",
    "hudGlass(c,h.ability.left,h.ability.top,h.ability.right,h.ability.bottom)",
    "drawHudHeart(c,h.heartCenters[i],FlightHudLayout.heartBaseline,i<game.lives)",
    'text(c,cachedLivesLabel,436f,35f,11f,0xffffdce5.toInt())',
    'text(c,"MOĆ",30f,91f,10f,0xffd4f4ff.toInt())',
    "drawHudNumber(c,cachedScoreLabel",
    "drawHudNumber(c,cachedFoodLabel",
):
    assert required in a_hud,required
assert "p.measureText(value)" in a
assert "if(game.lives!=cachedLives)" in a
print("PASS: shared score, food, outlined hearts and power geometry, bounded large totals")
