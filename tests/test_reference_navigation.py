"""Reference-driven navigation: Home, Skins, Shop, Levels, Flight, Pause and Results."""
from pathlib import Path
p=Path(__file__).resolve().parents[1]
a=(p/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
for code in (a,):
    assert "showHome()" in code and "showPremiumPause(" in code
    assert "startGame(" in code and "showResult(" in code
    for removed in ("showWorlds(", "showProfile(", "showCollection(", "showLeaderboard(", "showAchievements(", "showSettings("):
        assert removed not in code,removed
    assert "progress.recordRun(g)" in code
    assert "NOVI REKORD!" in code
    assert "previousRecord" in code
assert "gamePauseButton" not in a and "view.paused=true" in a
assert all(x in a for x in ("showLevels()","showShop()","showSkins()"))
print("PASS: real BOPAVI menu destinations, flight and system-Back pause")
