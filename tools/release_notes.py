#!/usr/bin/env python3
"""Use the exact current CHANGELOG entry as GitHub Release notes."""
from pathlib import Path
import re

from release_metadata import ROOT, release_metadata


def release_notes(root: Path = ROOT) -> str:
    version, build = release_metadata(root)
    text = (root / "CHANGELOG.md").read_text(encoding="utf-8")
    entry = re.search(
        rf"(?ms)^## v{re.escape(version)}\s+[—-]\s+[^\n]+\n.*?(?=^## v\d+\.\d+\.\d+\b|\Z)",
        text,
    )
    if entry is None:
        raise ValueError(f"CHANGELOG entry missing for v{version}")
    return (
        entry.group(0).strip()
        + f"\n\n**Build {build} distribution:** the APK is a debug build; "
        "the Android release AAB is unsigned. "
        "Store signing, hardware testing and 1:1 reference artwork parity "
        "are not claimed.\n"
    )


if __name__ == "__main__":
    print(release_notes(), end="")
