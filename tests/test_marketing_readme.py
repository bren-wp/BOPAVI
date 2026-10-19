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
        for name in ("Home", "Flight-Preview", "Flight-Active", "Results"):
            self.assertIn(f"releases/latest/download/BOPAVI-Android-{name}.png", content)
        self.assertIn("https://brendigo.com", content)
        self.assertNotIn("play.google.com/store/apps/details", content)
        self.assertNotIn("apps.apple.com/", content)

    def test_readme_is_not_a_development_report(self):
        content = README.read_text(encoding="utf-8")
        for unwanted in ("feature-matrix", "versionCode", "CURRENT_PROJECT_VERSION", "git clone",
                         "GitHub Actions", "CI/CD", "build 130", "v0.2.24", "shop", "leveli"):
            self.assertNotIn(unwanted.casefold(), content.casefold(), unwanted)
        self.assertTrue(re.search(r"Bez oglasa.*Bez praćenja.*Bez registracije", content))
        self.assertIn("IGRAJ", content)


if __name__ == "__main__":
    unittest.main(verbosity=2)
