#!/usr/bin/env python3
"""Prepare reviewable Android release notes from pending or published metadata."""
from pathlib import Path
import re
from release_metadata import ROOT,release_metadata
def release_notes(root:Path=ROOT)->str:
    version,build=release_metadata(root)
    for path in (root/"docs/release/NEXT.md",root/"CHANGELOG.md"):
        if not path.exists():continue
        source=path.read_text(encoding="utf-8")
        entry=re.search(rf"(?ms)^## v{re.escape(version)}\s+[—-]\s+[^\n]+\n.*?(?=^## v\d+\.\d+\.\d+\b|\Z)",source)
        if entry is not None and f"(build {build})" in entry.group(0).splitlines()[0]:
            return (entry.group(0).strip()+f"\n\n**Build {build} distribution:** "
                    "APK is a debug build; the Android AAB is unsigned. "
                    "Physical-device and 1:1 visual certification are not claimed.\n")
    raise ValueError(f"Pending or published release notes missing for v{version} build {build}")
if __name__=="__main__":print(release_notes(),end="")
