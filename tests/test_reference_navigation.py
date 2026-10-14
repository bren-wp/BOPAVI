"""Source-contract regressions for the two live no-scroll menu implementations.

This does not assert pixel-identical artwork or replace emulator/UI smoke tests.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def section(source: str, start: str, end: str) -> str:
    left = source.index(start)
    right = source.index(end, left + len(start))
    return source[left:right]


def main() -> None:
    android = (ROOT / "android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
    ios = (ROOT / "ios/BOPAVI/GameController.swift").read_text()

    a_levels = section(android, "    private fun showLevels(", "    private fun levelTile(")
    i_levels = section(ios, "    private func showLevels(", "    private func levelTile(")

    # Five rows of four genuinely interactive level tiles, with stored stars
    # and frontier gating rather than 20 screenshot hotspots.
    assert "for(r in 0 until 5)" in a_levels
    assert "for(col in 0 until 4)" in a_levels
    assert "for rowNumber in 0..<5" in i_levels
    assert "for column in 0..<4" in i_levels
    for levels in (a_levels, i_levels):
        assert "levelTile(" in levels
        assert "sectionHeading(" not in levels
        assert "POKRENI ODABRANI LEVEL" not in levels
        assert "NASTAVI LET" not in levels
        assert "PRETHODNIH 20" in levels
        assert "SLJEDEĆIH 20" in levels

    a_home = section(android, "    private fun showHome(", "    private fun showWorlds(")
    i_home = section(ios, "    private func showHome(", "    private func showWorlds(")

    assert "portraitDrawableIds[progress.skin()]" in a_home
    assert 'UIImage(named:"BopiPortrait\\(progress.skinIndex())")' in i_home
    assert "showProfile()" in a_home and "showProfile()" in i_home
    assert "showSettings()" in a_home and "showSettings()" in i_home
    assert "showWorlds()" in a_home and "showWorlds()" in i_home
    assert "showPerks(" in a_home and "showPerks(" in i_home

    # Motion is behind all controls; Android stops on detach, UIKit respects
    # Reduce Motion, and the two native game simulations are not touched.
    assert "onViewDetachedFromWindow(v:View){drift.cancel()}" in a_home
    assert "UIAccessibility.isReduceMotionEnabled" in i_home
    assert "home-scenery-drift" in i_home
    print("PASS: functional, paged 20-level galleries and native live Home contracts")


if __name__ == "__main__":
    main()
