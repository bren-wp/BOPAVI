"""V5 game navigation: Home, playable endless flight, Pause and Results only."""
from pathlib import Path
p=Path(__file__).resolve().parents[1]
a=(p/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
i=(p/"ios/BOPAVI/GameController.swift").read_text()
for code in (a,i):
    assert "showHome()" in code and "showPremiumPause(" in code
    assert "startGame(" in code and "showResult(" in code
    for removed in ("showWorlds(", "showLevels(", "showShop(", "showProfile(", "showCollection(", "showLeaderboard(", "showAchievements(", "showSettings("):
        assert removed not in code,removed
    assert "progress.recordRun(g)" in code
    assert "NOVI REKORD!" in code
    assert "previousRecord" in code
assert "gamePauseButton?.performClick()" in a
assert "pause.addAction(" in i
print("PASS: one-button BOPAVI and four-view navigation, no legacy routes")
