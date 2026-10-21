#!/usr/bin/env python3
"""Protect F-Droid metadata, source pinning and signable Android artifacts."""
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from package_fdroid_submission import fdroid_metadata, LOCALE, SCREENSHOTS
from release_metadata import release_metadata


class FDroidDistributionTests(unittest.TestCase):
    def test_pinned_build_metadata(self):
        version, code = release_metadata(ROOT)
        pinned = "a" * 40
        yml = fdroid_metadata(pinned, ROOT)
        for item in (
            "License: MIT", "RepoType: git", "Repo: https://github.com/bren-wp/BOPAVI.git",
            f"versionName: '{version}'", f"versionCode: {code}",
            f"commit: {pinned}", "subdir: android", "gradle:",
            "python3-pil python3-cairosvg", "tools/generate_images.py",
            "tools/generate_audio.py", f"CurrentVersionCode: {code}",
        ):
            self.assertIn(item, yml, item)
        self.assertEqual(yml.count("Builds:"), 1)
        for invalid in ("main", "v1.0.12", "a" * 39, "A" * 40, "../traversal"):
            with self.assertRaises(ValueError):
                fdroid_metadata(invalid, ROOT)

    def test_upstream_metadata_and_real_assets_contract(self):
        self.assertIn("MIT License", (ROOT / "LICENSE").read_text())
        upstream = (ROOT / "fdroid/metadata/com.brendigo.bopavi.yml").read_text()
        self.assertNotIn("Builds:", upstream)
        self.assertIn("AutoUpdateMode: None", upstream)
        self.assertEqual(len(SCREENSHOTS), 4)
        for filename in ("title.txt", "short_description.txt", "full_description.txt"):
            content = (ROOT / LOCALE / filename).read_text(encoding="utf-8")
            self.assertTrue(content.strip())
        instructions = (ROOT / "docs/fdroid/README.md").read_text()
        self.assertIn("existing", instructions)
        self.assertIn("F-Droid", instructions)

    def test_release_pipeline_guards(self):
        wf = (ROOT / ".github/workflows/native-ci.yml").read_text()
        self.assertIn(":app:assembleRelease", wf)
        self.assertIn("tools/verify_unsigned_apk.py --apk", wf)
        self.assertIn("app-release-unsigned.apk", wf)
        self.assertIn("tools/package_fdroid_submission.py --commit", wf)
        self.assertIn("BOPAVI-FDroid-submission-", wf)
        self.assertIn("SHA256SUMS.txt", wf)
        self.assertIn("test \"$(wc -l < SHA256SUMS.txt)\" -eq 9", wf)


if __name__ == "__main__":
    unittest.main(verbosity=2)
