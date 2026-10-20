#!/usr/bin/env python3
"""Android version, release notes and unsigned bundle safeguards."""
from pathlib import Path
import sys
import tempfile
import unittest
from zipfile import ZipFile
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/"tools"))
from release_metadata import release_metadata
from release_notes import release_notes
from verify_play_release import signature_entries

class ReleaseContractTests(unittest.TestCase):
    def test_android_only_metadata(self):
        version,build=release_metadata()
        self.assertRegex(version,r"^\d+\.\d+\.\d+$")
        self.assertGreater(build,130)
        self.assertFalse((ROOT/"ios").exists())

    def test_pending_release_notes_and_published_changelog_are_separate(self):
        version,build=release_metadata()
        published=(ROOT/"CHANGELOG.md").read_text(encoding="utf-8")
        self.assertTrue(1 <= published.count("## v") <= 2)
        # The current Gradle version can be staged or already published; only
        # GitHub's public release inventory may promote it into CHANGELOG.
        notes=release_notes()
        self.assertIn(f"## v{version} —",notes)
        self.assertIn(f"(build {build})",notes)
        self.assertIn(f"Build {build} distribution",notes)
        self.assertIn("unsigned",notes)

    def test_reject_ambiguous_build_metadata(self):
        with tempfile.TemporaryDirectory() as tmp:
            root=Path(tmp)
            a=root/"android/app";a.mkdir(parents=True)
            (a/"build.gradle.kts").write_text('versionName = "1.0.1"\nversionCode = 131\nversionCode = 132')
            with self.assertRaisesRegex(ValueError,"missing or ambiguous"):
                release_metadata(root)

    def test_notes_do_not_splice_previous_release(self):
        with tempfile.TemporaryDirectory() as tmp:
            root=Path(tmp);(root/"android/app").mkdir(parents=True)
            (root/"docs/release").mkdir(parents=True)
            (root/"android/app/build.gradle.kts").write_text('versionName = "1.0.1"\nversionCode = 131')
            (root/"CHANGELOG.md").write_text("## v1.0.0 — Published (build 130)\n- Historical.\n")
            (root/"docs/release/NEXT.md").write_text("## v1.0.1 — Pending (build 131)\n- Android only.\n")
            notes=release_notes(root)
            self.assertIn("Android only.",notes)
            self.assertNotIn("Historical.",notes)
            (root/"docs/release/NEXT.md").write_text("## v1.0.2 — Wrong (build 132)\n")
            with self.assertRaisesRegex(ValueError,"missing"):
                release_notes(root)
            (root/"CHANGELOG.md").write_text("## v1.0.1 — Published (build 131)\n- Already published.\n")
            published_notes=release_notes(root)
            self.assertIn("Already published.",published_notes)
            self.assertNotIn("Historical.",published_notes)

    def test_unsigned_bundle_has_no_signing_entries(self):
        with tempfile.TemporaryDirectory() as tmp:
            for signed in (True,False):
                artifact=Path(tmp)/("signed.aab" if signed else "unsigned.aab")
                with ZipFile(artifact,"w") as bundle:
                    bundle.writestr("BundleConfig.pb",b"test")
                    bundle.writestr("base/manifest/AndroidManifest.xml",b"test")
                    if signed:
                        bundle.writestr("META-INF/UPLOAD.SF",b"signature")
                        bundle.writestr("META-INF/UPLOAD.RSA",b"certificate")
                with ZipFile(artifact) as bundle:
                    self.assertEqual(signed,bool(signature_entries(bundle.namelist())))

if __name__ == "__main__":
    unittest.main(verbosity=2)
