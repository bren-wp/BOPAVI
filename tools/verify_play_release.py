#!/usr/bin/env python3
"""Fail Play builds without API 36, valid version, truthful permissions or signed AAB."""
import argparse
from pathlib import Path
from zipfile import ZipFile
import re
import subprocess

p = argparse.ArgumentParser()
p.add_argument("--aab", type=Path, required=True)
p.add_argument("--signed", action="store_true")
args = p.parse_args()
root = Path(__file__).resolve().parents[1]
gradle = (root/"android/app/build.gradle.kts").read_text()
manifest = (root/"android/app/src/main/AndroidManifest.xml").read_text()
assert "compileSdk = 36" in gradle and "targetSdk = 36" in gradle
assert 'applicationId = "com.brendigo.bopavi"' in gradle
assert re.search(r'versionCode\s*=\s*34\b', gradle)
assert re.search(r'versionName\s*=\s*"0\.1\.31"', gradle)
assert 'android:appCategory="game"' in manifest
assert "android.permission.INTERNET" not in manifest
assert "android:allowBackup=\"false\"" in manifest
assert args.aab.is_file() and args.aab.stat().st_size > 100_000
with ZipFile(args.aab) as zf:
    members = set(zf.namelist())
    assert "base/manifest/AndroidManifest.xml" in members
    assert "BundleConfig.pb" in members
    # BOPAVI currently has no NDK dependencies. Fail closed if any .so enters
    # the bundle until its ELF alignment and 16 KB page compatibility are tested.
    native_libraries = sorted(name for name in members if name.startswith("base/lib/") and name.endswith(".so"))
    assert not native_libraries, f"16 KB page-size audit required for added native libraries: {native_libraries}"
if args.signed:
    verify = subprocess.run(
        ["jarsigner", "-verify", "-verbose", str(args.aab)],
        capture_output=True, text=True, check=True,
    )
    assert "jar verified." in verify.stdout.lower(), "Play AAB upload signature not verified"
print(f"PASS: API 36, BOPAVI v0.1.31, package/permissions and {'SIGNED' if args.signed else 'bundle structure'}")
