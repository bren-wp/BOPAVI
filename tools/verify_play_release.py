#!/usr/bin/env python3
"""Fail closed if a GitHub release AAB is signed, malformed, or mismatched."""
import argparse
from pathlib import Path
from zipfile import ZipFile
import subprocess

from release_metadata import release_metadata


def signature_entries(members) -> list[str]:
    """Jar signing produces META-INF/*.SF and a matching certificate block."""
    suffixes = (".SF", ".RSA", ".DSA", ".EC")
    return sorted(name for name in members if name.upper().startswith("META-INF/")
                  and name.upper().endswith(suffixes))


def verify_unsigned_bundle(aab: Path) -> None:
    assert aab.is_file() and aab.stat().st_size > 100_000, "AAB missing or empty"
    with ZipFile(aab) as bundle:
        members = set(bundle.namelist())
        assert "base/manifest/AndroidManifest.xml" in members, "Missing AAB manifest"
        assert "BundleConfig.pb" in members, "Missing AAB bundle configuration"
        libs = sorted(name for name in members
                      if name.startswith("base/lib/") and name.endswith(".so"))
        assert not libs, f"16 KB page-size audit required for added native libraries: {libs}"
        certificates = signature_entries(members)
        assert not certificates, f"Signed bundle must never be published on GitHub: {certificates}"
    result = subprocess.run(["jarsigner", "-verify", str(aab)],
                            capture_output=True, text=True, check=False)
    output = (result.stdout + result.stderr).lower()
    assert result.returncode == 0 and "jar is unsigned" in output, (
        "Expected an unsigned AAB; jarsigner did not confirm unsigned status: " + output[-900:]
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--aab", type=Path, required=True)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    gradle = (root / "android/app/build.gradle.kts").read_text()
    manifest = (root / "android/app/src/main/AndroidManifest.xml").read_text()
    version, build = release_metadata(root)
    assert "compileSdk = 36" in gradle and "targetSdk = 36" in gradle
    assert 'applicationId = "com.brendigo.bopavi"' in gradle
    assert 'android:appCategory="game"' in manifest
    assert "android.permission.INTERNET" not in manifest
    assert 'android:allowBackup="false"' in manifest
    assert "signingConfigs" not in gradle and "BOPAVI_UPLOAD_" not in gradle
    verify_unsigned_bundle(args.aab)
    print(f"PASS: API 36, BOPAVI v{version} build {build}, privacy and VERIFIED UNSIGNED AAB")


if __name__ == "__main__":
    main()
