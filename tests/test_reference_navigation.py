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

    # One animation helper per platform: only artwork moves, both user
    # accessibility settings are honored, and Android detaches its animator.
    a_helper = section(android, "    private fun startSceneryDrift(", "    private fun ui(")
    i_helper = section(ios, "    private func startSceneryDrift(", "    private func menu(")
    a_menu = section(android, "    private fun base(", "    private fun title(")
    i_menu = section(ios, "    private func menu(", "    override func viewDidLayoutSubviews(")
    assert "progress.lessMotion()" in a_helper
    assert "ValueAnimator.areAnimatorsEnabled()" in a_helper
    assert "onViewDetachedFromWindow(v:View){drift.cancel()}" in a_helper
    assert "startSceneryDrift(scenicBackdrop)" in a_menu
    assert "startSceneryDrift(this)" in a_home
    assert "progress.lessMotion" in i_helper
    assert "UIAccessibility.isReduceMotionEnabled" in i_helper
    assert 'forKey:"scenery-breathing"' in i_helper
    assert "startSceneryDrift(backdrop)" in i_menu
    assert "startSceneryDrift(backdrop)" in i_home

    # A profile icon must announce the CURRENT pilot, never a hardcoded Bopi.
    assert 'contentDescription=ui("PROFIL")+" · "+progress.skinNames[progress.skin()]' in a_home
    assert 'avatar.accessibilityLabel=ui("PROFIL")+" · "+progress.skinNames[progress.skinIndex()]' in i_home
    assert 'ui("PROFIL · Bopi")' not in a_home + i_home

    # A brighter illustrated backdrop still has a separate contrast veil;
    # gameplay layers and actual clickable widgets are untouched.
    assert "0x16031837,0x2c061b3e,0x8604132e.toInt()" in android
    assert "alpha:0.18" in i_menu
    assert "root.addView(View(this).apply{background=premiumBackground()}" in a_menu
    assert "view.addSubview(shade)" in i_menu
    a_settings = section(android, "    private fun showSettings(", "    private fun showSettingsDetails(")
    i_settings = section(ios, "    private func showSettings(", "    private func showSettingsDetails(")
    a_details = section(android, "    private fun showSettingsDetails(", "    private fun showProfile(") if (
        android.index("    private fun showSettingsDetails(") <
        android.index("    private fun showProfile(")
    ) else android.split("    private fun showSettingsDetails(",1)[1]
    i_details = ios.split("    private func showSettingsDetails(",1)[1]
    assert "progress.setLessMotion(!progress.lessMotion())" in a_settings
    assert "showSettings()" in a_settings
    assert "self.progress.lessMotion = !self.progress.lessMotion" in i_settings
    assert "self.showSettings()" in i_settings
    assert "progress.setLessMotion(v)" in a_details
    assert "showSettingsDetails()" in a_details
    assert "self.progress.lessMotion=toggle.isOn" in i_details
    assert "self.showSettingsDetails()" in i_details
    for details in (a_details,i_details):
        assert "Oprema vrijedi za sve pilote." in details
        assert "Pilot: Bopi · sva oprema" not in details

    print("PASS: live 20-level galleries, selected pilot a11y and immediate motion preferences")


if __name__ == "__main__":
    main()
