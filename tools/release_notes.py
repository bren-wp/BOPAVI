#!/usr/bin/env python3
"""Use reviewed candidate notes for Android release; preserve published changelog."""
from pathlib import Path
import re
from release_metadata import ROOT,release_metadata
def release_notes(root:Path=ROOT)->str:
    version,build=release_metadata(root)
    source=(root/"docs/release/NEXT.md").read_text(encoding="utf-8")
    found=re.search(rf"(?ms)^## v{re.escape(version)}\s+[—-]\s+[^\n]+\n.*?(?=^## v\d+\.\d+\.\d+\b|\Z)",source)
    if found is None or f"(build {build})" not in found.group(0).splitlines()[0]:
        raise ValueError(f"Pending release notes missing for v{version} build {build}")
    return found.group(0).strip()+f"\n\n**Build {build} distribution:** APK is a debug build; the Android AAB is unsigned. Physical-device and 1:1 visual certification are not claimed.\n"
if __name__=="__main__":print(release_notes(),end="")
