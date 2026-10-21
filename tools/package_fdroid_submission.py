#!/usr/bin/env python3
"""Package F-Droid source-build submission materials for a verified release commit.

This creates metadata for maintainers; it does not publish to or claim acceptance
by the official F-Droid repository. It never includes upload keys or signed APKs.
"""
import argparse
import re
import struct
from pathlib import Path
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED

from release_metadata import ROOT, release_metadata

SCREENSHOTS = {
    "BOPAVI-Android-Home.png": "01-home.png",
    "BOPAVI-Android-Flight-Preview.png": "02-flight-preview.png",
    "BOPAVI-Android-Flight-Active.png": "03-flight-active.png",
    "BOPAVI-Android-Results.png": "04-results.png",
}
LOCALE = "fastlane/metadata/android/hr-HR"


def fdroid_metadata(commit: str, root: Path = ROOT) -> str:
    if not re.fullmatch(r"[0-9a-f]{40}", commit):
        raise ValueError("F-Droid recipe must pin a 40-character Git commit SHA")
    version, code = release_metadata(root)
    upstream = (root / "fdroid/metadata/com.brendigo.bopavi.yml").read_text(
        encoding="utf-8"
    ).rstrip()
    if "Builds:" in upstream:
        raise ValueError("Upstream template must not have an unverified Builds entry")
    return (
        upstream
        + "\nBuilds:\n"
        + f"  - versionName: '{version}'\n"
        + f"    versionCode: {code}\n"
        + f"    commit: {commit}\n"
        + "    subdir: android\n"
        + "    sudo:\n"
        + "      - apt-get update\n"
        + "      - apt-get install -y python3-pil python3-cairosvg\n"
        + "    prebuild: cd .. && python3 tools/generate_images.py && python3 tools/generate_audio.py\n"
        + "    gradle:\n"
        + "      - yes\n"
        + f"CurrentVersion: '{version}'\n"
        + f"CurrentVersionCode: {code}\n"
    )


def png_size(data: bytes) -> tuple[int, int]:
    if len(data) < 2000 or data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
        raise ValueError("F-Droid screenshot must be a genuine, non-empty PNG")
    w, h = struct.unpack(">II", data[16:24])
    if w < 320 or h < 600 or w >= h:
        raise ValueError(f"F-Droid screenshot dimensions invalid: {w}x{h}")
    return w, h


def build_archive(commit: str, shots: Path, output: Path, root: Path = ROOT) -> Path:
    version, _ = release_metadata(root)
    payload = {
        "metadata/com.brendigo.bopavi.yml": fdroid_metadata(commit, root).encode(),
        "LICENSE": (root / "LICENSE").read_bytes(),
        "README-FDROID.md": (root / "docs/fdroid/README.md").read_bytes(),
        "SOURCE-COMMIT.txt": (commit + "\n").encode(),
        "docs/assets/icon.svg": (root / "docs/assets/icon.svg").read_bytes(),
    }
    for file in ("title.txt", "short_description.txt", "full_description.txt"):
        path = f"{LOCALE}/{file}"
        payload[path] = (root / path).read_bytes()
    seen = set()
    for source, name in SCREENSHOTS.items():
        data = (shots / source).read_bytes()
        png_size(data)
        if data in seen:
            raise ValueError(f"Repeated screenshot bytes: {source}")
        seen.add(data)
        payload[f"{LOCALE}/images/phoneScreenshots/{name}"] = data
    output.mkdir(parents=True, exist_ok=True)
    target = output / f"BOPAVI-FDroid-submission-v{version}.zip"
    with ZipFile(target, "w", compression=ZIP_DEFLATED, compresslevel=9) as z:
        for name, data in sorted(payload.items()):
            info = ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            info.compress_type = ZIP_DEFLATED
            info.external_attr = 0o644 << 16
            z.writestr(info, data, compress_type=ZIP_DEFLATED, compresslevel=9)
    with ZipFile(target) as z:
        if z.testzip() is not None or set(z.namelist()) != set(payload):
            raise ValueError("Corrupt F-Droid submission archive")
    print(f"PASS: F-Droid submission ZIP {target} pinned to {commit}; "
          "official fdroid build/acceptance still pending")
    return target


def main() -> None:
    p = argparse.ArgumentParser()
    p.add_argument("--commit", required=True, help="Exact published Git commit SHA")
    p.add_argument("--screenshots-dir", type=Path, required=True)
    p.add_argument("--output-dir", type=Path, default=Path("dist"))
    args = p.parse_args()
    build_archive(args.commit, args.screenshots_dir, args.output_dir)


if __name__ == "__main__":
    main()
