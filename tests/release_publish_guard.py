#!/usr/bin/env python3
"""Allow skipping a duplicate tag only when distributable inputs are unchanged."""
import sys
from pathlib import PurePosixPath

SAFE_FILES = {"README.md", ".github/workflows/native-ci.yml"}
SAFE_PREFIXES = ("docs/", "tests/", "android/app/src/test/")
UNSAFE_PREFIXES = ("docs/assets/",)

def is_metadata_only(paths):
    for raw in paths:
        path=raw.strip()
        if not path or path.startswith("/") or ".." in PurePosixPath(path).parts:
            return False
        if path in SAFE_FILES:
            continue
        if any(path.startswith(prefix) for prefix in UNSAFE_PREFIXES):
            return False
        if any(path.startswith(prefix) for prefix in SAFE_PREFIXES):
            continue
        return False
    return True

if __name__ == "__main__":
    changed=sys.stdin.read().splitlines()
    if is_metadata_only(changed):
        print("PASS: unchanged distributable app inputs; skip duplicate GitHub release.")
        sys.exit(0)
    print("BLOCK: code/build/assets change requires a new version.",file=sys.stderr)
    sys.exit(1)
