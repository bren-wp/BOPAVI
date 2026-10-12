#!/usr/bin/env python3
"""Regression tests for version parity and accidental GitHub AAB signing."""
from pathlib import Path
import sys
import tempfile
import unittest
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from release_metadata import release_metadata
from release_notes import release_notes
from verify_play_release import signature_entries


class ReleaseContractTests(unittest.TestCase):
    def test_android_and_ios_versions_match(self):
        version, build = release_metadata()
        self.assertRegex(version, r"^\d+\.\d+\.\d+$")
        self.assertGreater(build, 0)

    def test_rejects_version_mismatch(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            android = root / "android/app"
            ios = root / "ios/BOPAVI.xcodeproj"
            android.mkdir(parents=True)
            ios.mkdir(parents=True)
            (android / "build.gradle.kts").write_text('versionName = "0.1.33"\nversionCode = 36')
            (ios / "project.pbxproj").write_text(
                "MARKETING_VERSION = 0.1.33; CURRENT_PROJECT_VERSION = 35;\n"
                "MARKETING_VERSION = 0.1.33; CURRENT_PROJECT_VERSION = 35;"
            )
            with self.assertRaisesRegex(ValueError, "versions/builds differ"):
                release_metadata(root)

    def test_release_notes_use_only_current_version(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            android = root / "android/app"
            ios = root / "ios/BOPAVI.xcodeproj"
            android.mkdir(parents=True)
            ios.mkdir(parents=True)
            (android / "build.gradle.kts").write_text('versionName = "0.1.48"\nversionCode = 51')
            (ios / "project.pbxproj").write_text(
                "MARKETING_VERSION = 0.1.48; CURRENT_PROJECT_VERSION = 51;\n"
                "MARKETING_VERSION = 0.1.48; CURRENT_PROJECT_VERSION = 51;"
            )
            changelog = root / "CHANGELOG.md"
            changelog.write_text(
                "## v0.1.48 — Actual first flap fix (build 51)\n\n"
                "- Valid action.\n\n"
                "## v0.1.47 — Outdated (build 50)\n\n"
                "- Outdated action.\n"
            )
            notes = release_notes(root)
            self.assertIn("Valid action", notes)
            self.assertIn("unsigned", notes)
            self.assertNotIn("Outdated action", notes)
            changelog.write_text("## v0.1.47 — Outdated\n")
            with self.assertRaisesRegex(ValueError, "CHANGELOG entry missing"):
                release_notes(root)

    def test_unsigned_bundle_has_no_signing_entries(self):
        with tempfile.TemporaryDirectory() as tmp:
            for signed in (True, False):
                artifact = Path(tmp) / ("signed.aab" if signed else "unsigned.aab")
                with ZipFile(artifact, "w") as bundle:
                    bundle.writestr("BundleConfig.pb", b"test")
                    bundle.writestr("base/manifest/AndroidManifest.xml", b"test")
                    if signed:
                        bundle.writestr("META-INF/UPLOAD.SF", b"signature")
                        bundle.writestr("META-INF/UPLOAD.RSA", b"certificate")
                with ZipFile(artifact) as bundle:
                    entries = signature_entries(bundle.namelist())
                    self.assertEqual(signed, bool(entries))


if __name__ == "__main__":
    unittest.main(verbosity=2)
