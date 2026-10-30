#!/usr/bin/env python3
"""Fail-closed checks for current reachable UX and version-aligned store claims."""
import json
import re
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from release_metadata import release_metadata
MATRIX = ROOT / "docs/product/FEATURE-MATRIX.md"
LISTING = ROOT / "docs/play/STORE-LISTING-hr-HR.md"
ANDROID = ROOT / "android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt"
MANIFEST = ROOT / "android/app/src/main/AndroidManifest.xml"
GRADLE = ROOT / "android/app/build.gradle.kts"
BANNED_PUBLIC = re.compile(
    r"\b(?:devet|osam|nine|eight|skin(?:s|ovi)?|svjetov\w*|"
    r"kovanic\w*|shop|trgovin\w*|otklju\w*|magnet\w*|"
    r"\b(?:level|levels|profil|profile|import|export|uvoz|izvoz|"
    r"štit(?:ovi|ova)?|shield(?:s)?|postavk\w*|settings))\b",
    re.I,
)

def matrix():
    content = MATRIX.read_text(encoding="utf-8")
    match = re.search(r"~~~json\s*(\{.*?\})\s*~~~", content, re.S)
    if not match:
        raise AssertionError("Missing JSON feature matrix")
    return json.loads(match.group(1))

def listing_parts():
    content = LISTING.read_text(encoding="utf-8")
    short = re.search(r"\*\*Kratki opis[^\n]*:\*\*\s*(.*)", content)
    full = re.search(r"\*\*Puni opis:\*\*\s*(.*?)(?=\n\*\*Kategorija:)", content, re.S)
    ids = re.search(r"<!-- verified-feature-ids: ([a-z0-9_,]+) -->", content)
    if not (short and full and ids):
        raise AssertionError("Store listing headers or declared feature IDs missing")
    return short.group(1), full.group(1), ids.group(1).split(",")

class StoreClaimsTest(unittest.TestCase):
    def test_schema_and_explicit_pending_adr(self):
        data = matrix()
        self.assertEqual(data["schema"], 1)
        self.assertEqual(data["scope_decision"], "pending")
        self.assertEqual(data["screens"], ["home", "flight", "pause", "results"])
        states = [f["status"] for f in data["features"]]
        self.assertTrue(set(states) <= {"visible", "internal-legacy", "planned", "removed"})
        ids = [f["id"] for f in data["features"]]
        self.assertEqual(len(ids), len(set(ids)))
        adr = (ROOT / "docs/product/ADR-0001-product-scope.md").read_text()
        self.assertIn("PENDING PRODUCT OWNER APPROVAL", adr)

    def test_public_release_docs_match_native_build(self):
        # Public docs name only the last published release, not the current candidate.
        changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
        published = re.search(r"^## v(\d+\.\d+\.\d+).*?\(build (\d+)\)", changelog, re.M)
        self.assertIsNotNone(published)
        version, build = published.group(1), int(published.group(2))
        self.assertEqual(matrix()["release"], f"v{version}")
        self.assertEqual(matrix()["build"], build)
        for relative in (
            "docs/product/CURRENT_SCREENS.md",
            "docs/play/STORE-LISTING-hr-HR.md",
            "docs/play/RELEASE-CHECKLIST.md",
        ):
            content = (ROOT / relative).read_text(encoding="utf-8")
            self.assertIn(f"v{version}", content, relative)
            self.assertIn(f"build {build}", content, relative)
        self.assertRegex(matrix()["baseline_sha"], r"^[0-9a-f]{40}$")

    def test_store_claims_must_be_visible(self):
        short, full, claims = listing_parts()
        catalog = {f["id"]: f["status"] for f in matrix()["features"]}
        self.assertEqual(len(claims), len(set(claims)))
        self.assertTrue(claims)
        self.assertTrue(all(catalog.get(claim) == "visible" for claim in claims), claims)
        self.assertLessEqual(len("BOPAVI"), 30)
        self.assertLessEqual(len(short), 80)
        self.assertIsNone(BANNED_PUBLIC.search(short + "\n" + full))
        self.assertIn("Bopiju", short)
        self.assertIn("bobice", full)
        self.assertIn("pauzirati", full)
        self.assertIn("lokalno", full)

    def test_native_nav_and_source_evidence(self):
        a = ANDROID.read_text(encoding="utf-8")
        for source, prefix in ((a, "fun"),):
            for fn in ("showHome", "startGame", "showPremiumPause", "showResult"):
                self.assertRegex(source, rf"private\s+{prefix}\s+{fn}\(")
            for obsolete in ("showWorlds(",
                             "showSettings(", "showProfile(", "showPilotPicker("):
                self.assertNotIn(obsolete, source)
            self.assertIn("progress.recordRun(g)", source)
            self.assertIn("private fun showSkins()",source)
            self.assertIn("private fun showShop()",source)
            self.assertIn("private fun showLevels()",source)
        self.assertIn("prepareFlight(world=0,number=1L,endless=true)", a)
        self.assertIn("LevelEngine.createStream(world,number)", a)
        self.assertNotIn('android.permission.INTERNET', MANIFEST.read_text())
        gradle = GRADLE.read_text()
        version, build = release_metadata()
        self.assertIn(f'versionName = "{version}"', gradle)
        self.assertIn(f'versionCode = {build}', gradle)

    def test_current_public_terms_are_not_legacy_promotion(self):
        # Negated historical notes in privacy/terms are deliberate disclosures,
        # not features; commercial copy is restricted in the listing above.
        for p in ("docs/play/PRIVACY_POLICY.md", "docs/play/TERMS_OF_USE.md",
                  "docs/play/GOOGLE-PLAY-DATA-SAFETY.md"):
            content = (ROOT / p).read_text(encoding="utf-8")
            self.assertNotIn("može ručno izvesti", content)
            self.assertNotIn("optional JSON backup", content)
            self.assertNotIn("osam igrivih svjetova", content)
        screen = (ROOT / "docs/product/CURRENT_SCREENS.md").read_text()
        for name in matrix()["screens"]:
            self.assertIn(name.capitalize(), screen)

if __name__ == "__main__":
    unittest.main(verbosity=2)
