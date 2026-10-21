#!/usr/bin/env python3
"""Require a real release-mode APK that has no APK or JAR signature.

An unsigned APK is an input for the private distribution keystore, not an
installable production binary. QA continues to install the debug-signed APK.
"""
import argparse
import os
import shutil
import subprocess
from pathlib import Path
from zipfile import ZipFile


def apksigner_path() -> str:
    binary = shutil.which("apksigner")
    if binary:
        return binary
    sdk = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    if sdk:
        versions = sorted((Path(sdk) / "build-tools").glob("*/apksigner"), reverse=True)
        if versions:
            return str(versions[0])
    raise RuntimeError("Android apksigner is required to confirm unsigned release APK")


def verify_unsigned_apk(apk: Path) -> None:
    assert apk.is_file() and apk.stat().st_size > 100_000, "Release APK missing"
    assert apk.name.endswith("-unsigned.apk"), "Refusing ambiguous APK filename"
    with ZipFile(apk) as z:
        assert z.testzip() is None, "Damaged release APK ZIP"
        names = set(z.namelist())
        assert "AndroidManifest.xml" in names, "Missing APK AndroidManifest.xml"
        assert "classes.dex" in names, "Missing classes.dex"
        jar_signatures = sorted(
            name for name in names if name.upper().startswith("META-INF/")
            and name.upper().endswith((".RSA", ".SF", ".DSA", ".EC"))
        )
        assert not jar_signatures, f"Release APK contains a v1 signature: {jar_signatures}"
    verify = subprocess.run(
        [apksigner_path(), "verify", "--verbose", str(apk)],
        capture_output=True, text=True, check=False
    )
    assert verify.returncode != 0, (
        "Release APK is signed: do not publish a file advertised as unsigned"
    )
    print(f"PASS: {apk.name} is a valid ZIP with classes and manifest, "
          "and apksigner rejects it as unsigned")


def main() -> None:
    p = argparse.ArgumentParser()
    p.add_argument("--apk", required=True, type=Path)
    args = p.parse_args()
    verify_unsigned_apk(args.apk)


if __name__ == "__main__":
    main()
