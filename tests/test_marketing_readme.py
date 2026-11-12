#!/usr/bin/env python3
"""Player-facing README contract: authentic art, honest links and no development log."""
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
README = ROOT / "README.md"


class MarketingReadmeTests(unittest.TestCase):
    def test_player_facing_copy_and_real_assets(self):
        content = README.read_text(encoding="utf-8")
        for path in ("docs/assets/logo.svg", "docs/assets/hero.svg", "docs/play/PRIVACY_POLICY.md"):
            self.assertIn(path, content)
            self.assertTrue((ROOT / path).is_file(), path)
        for name in ("Home", "Skins", "Shop", "Settings", "Game-Over", "Privacy", "Terms", "Flight-Preview", "Flight-Active", "Pause", "Results"):
            self.assertIn(f"releases/latest/download/BOPAVI-Android-{name}.png", content)
        self.assertEqual(content.count("releases/latest/download/BOPAVI-Android-"), 22)
        self.assertIn("https://brendigo.com", content)
        self.assertNotIn("BOPAVI-Android-Splash.png",content)
        self.assertIn("odmah otvara",content)
        # Public store URL is a requested navigation link, not a claim
        # that a particular Android release has reached production.
        self.assertIn('href="https://play.google.com/store/apps/details?id=com.brendigo.bopavi&hl=hr"', content)
        self.assertIn('alt="Preuzmi BOPAVI na Google Playu"', content)
        self.assertIn('play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png', content)
        self.assertNotIn("apps.apple.com/", content)

    def test_readme_is_not_a_development_report(self):
        content = README.read_text(encoding="utf-8")
        for unwanted in ("feature-matrix", "versionCode", "CURRENT_PROJECT_VERSION", "git clone",
                         "GitHub Actions", "CI/CD", "build 130", "v0.2.24", "leveli"):
            player_text = re.sub(r"<[^>]*>", "", content)
            self.assertNotIn(unwanted.casefold(), player_text.casefold(), unwanted)
        self.assertTrue(re.search(r"Bez oglasa.*Bez praćenja.*Bez registracije", content))
        self.assertIn("Shop, Skins i Settings", content)
        self.assertNotIn("BOPAVI-Android-Progress.png",content)


if __name__ == "__main__":
    unittest.main(verbosity=2)
