#!/usr/bin/env python3
"""Validate Android-only versionName/versionCode for distribution."""
from pathlib import Path
import argparse
import re

ROOT = Path(__file__).resolve().parents[1]


def release_metadata(root: Path = ROOT) -> tuple[str, int]:
    gradle = (root / "android/app/build.gradle.kts").read_text(encoding="utf-8")
    versions = re.findall(r'versionName\s*=\s*"(\d+\.\d+\.\d+)"', gradle)
    codes = re.findall(r"versionCode\s*=\s*(\d+)", gradle)
    if len(versions) != 1 or len(codes) != 1:
        raise ValueError("Android marketing or build version is missing or ambiguous")
    build = int(codes[0])
    if build <= 0:
        raise ValueError("Build number must be positive")
    return versions[0], build


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--tag", action="store_true", help="Print v-prefixed release tag")
    parser.add_argument("--play-archive", action="store_true", help="Print Play listing ZIP filename")
    parser.add_argument("--build", action="store_true", help="Print Android build number")
    args = parser.parse_args()
    version, build = release_metadata()
    if args.play_archive:
        print(f"BOPAVI-Google-Play-listing-v{version}.zip")
    elif args.tag:
        print(f"v{version}")
    elif args.build:
        print(build)
    else:
        print(version)
