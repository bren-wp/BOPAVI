#!/usr/bin/env python3
"""Hash and classify bundled Android graphic/audio files for manual license audit.

A hash inventory is neither ownership proof nor permission to distribute.
Generated drawables are included only when this runs after CI artwork build.
"""
import argparse
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASES = (
    ROOT / "android/app/src/main/res/drawable",
    ROOT / "android/app/src/main/res/drawable-nodpi",
    ROOT / "android/app/src/main/res/raw",
    ROOT / "docs/assets",
    ROOT / "art/reference-approved",
)
EXTENSIONS = {".png", ".webp", ".jpg", ".jpeg", ".svg", ".wav", ".mp3", ".ogg", ".ttf", ".otf"}


def inventory():
    result = []
    for root in BASES:
        if not root.exists():
            continue
        for entry in sorted(root.rglob("*")):
            if not entry.is_file() or entry.suffix.lower() not in EXTENSIONS:
                continue
            relative = entry.relative_to(ROOT).as_posix()
            data = entry.read_bytes()
            result.append({
                "path": relative,
                "bytes": len(data),
                "sha256": hashlib.sha256(data).hexdigest(),
                "source": "CI-generated or repository resource; ownership not inferred",
                "license_evidence_status": "requires per-file rights and upstream attribution review",
            })
    return result


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--json-out", type=Path, required=True)
    args = p.parse_args()
    records = inventory()
    if not records:
        raise SystemExit("No bundled resources found")
    report = {
        "scope": "Android bundled and documented assets; not third-party license clearance",
        "files": len(records),
        "rights_verified": 0,
        "license_status": "pending independent origin, copyright and F-Droid suitability review",
        "assets": records,
    }
    args.json_out.parent.mkdir(parents=True, exist_ok=True)
    args.json_out.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n",
                             encoding="utf-8")
    print(f'Asset inventory: {len(records)} hashed files; 0 certified license claims. '
          'Copyright and F-Droid approval remain OPEN.')


if __name__ == "__main__":
    main()
