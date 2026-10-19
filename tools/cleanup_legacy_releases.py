#!/usr/bin/env python3
"""Safely prune superseded pre-1.0 GitHub releases; v1+ are permanently protected.

Default is a dry-run. Apply mode is allowed only after verifying the new
published release and SHA256SUMS of every downloadable artifact.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile

LEGACY = re.compile(r"^v0\.[0-9]+\.[0-9]+$")
STABLE = re.compile(r"^v([1-9][0-9]*)\.[0-9]+\.[0-9]+$")
REPO = re.compile(r"^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$")
EXPECTED = {
    "app-debug.apk", "app-release.aab",
    "BOPAVI-iOS-Simulator.zip", "BOPAVI-iOS-unsigned-device.zip",
    "BOPAVI-Android-Home.png", "BOPAVI-Android-Flight-Preview.png",
    "BOPAVI-Android-Flight-Active.png", "BOPAVI-Android-Results.png",
    "BOPAVI-iOS-Home.png", "SHA256SUMS.txt",
}


def plan_cleanup(releases: list[dict], *, keep_legacy: int = 3) -> tuple[list[str], list[str]]:
    """Only remove older, public non-prerelease v0.x tags. Keep every v1+ tag."""
    if keep_legacy < 3:
        raise ValueError("At least three legacy releases must be preserved")
    legacy = [
        release for release in releases
        if not release.get("draft") and not release.get("prerelease")
        and LEGACY.fullmatch(str(release.get("tag_name", "")))
        and release.get("published_at")
    ]
    legacy.sort(key=lambda item: (item["published_at"], item["tag_name"]), reverse=True)
    kept = [item["tag_name"] for item in legacy[:keep_legacy]]
    candidates = [item["tag_name"] for item in reversed(legacy[keep_legacy:])]
    if len(set(kept + candidates)) != len(kept + candidates):
        raise ValueError("Duplicate release tag in GitHub API response")
    return kept, candidates


def gh(*args: str) -> str:
    proc = subprocess.run(["gh", *args], capture_output=True, text=True, check=True)
    return proc.stdout


def list_releases(repository: str) -> list[dict]:
    raw = gh("api", "--paginate", f"repos/{repository}/releases?per_page=100")
    # gh outputs one JSON array per page. Decode all pages, even when pretty-
    # printed or concatenated without separators; never silently stop at 100.
    decoder = json.JSONDecoder()
    offset = 0
    releases: list[dict] = []
    while offset < len(raw):
        while offset < len(raw) and raw[offset].isspace():
            offset += 1
        if offset == len(raw):
            break
        page, offset = decoder.raw_decode(raw, offset)
        if not isinstance(page, list):
            raise ValueError("GitHub releases API did not return an array")
        for release in page:
            if not isinstance(release, dict):
                raise ValueError("Invalid release in GitHub API response")
            assets = release.get("assets", [])
            if not isinstance(assets, list):
                raise ValueError("Invalid release assets in GitHub API response")
            releases.append({
                "tag_name": release.get("tag_name"),
                "published_at": release.get("published_at"),
                "draft": release.get("draft"),
                "prerelease": release.get("prerelease"),
                "assets": [a.get("name") for a in assets],
            })
    return releases


def verify_current_release(repository: str, tag: str, releases: list[dict]) -> None:
    """Fail before any delete if the new v1+ release/assets/checksums aren't valid."""
    if not STABLE.fullmatch(tag):
        raise ValueError("Only a published v1+ tag may trigger cleanup")
    published = [
        r for r in releases if r.get("tag_name") == tag
        and not r.get("draft") and not r.get("prerelease") and r.get("published_at")
    ]
    if len(published) != 1:
        raise ValueError(f"New release {tag} is not uniquely published")
    expected = EXPECTED | {f"BOPAVI-Google-Play-listing-{tag}.zip"}
    assets = published[0].get("assets")
    if not isinstance(assets, list) or set(assets) != expected or len(assets) != len(expected):
        raise ValueError(f"New release {tag} has missing or unexpected assets")
    with tempfile.TemporaryDirectory(prefix="bopavi-release-verify-") as tmp:
        gh("release", "download", tag, "--repo", repository, "--dir", tmp)
        directory = Path(tmp)
        files = {item.name for item in directory.iterdir() if item.is_file()}
        if files != expected:
            raise ValueError("Downloaded assets do not match the release manifest")
        lines = (directory / "SHA256SUMS.txt").read_text(encoding="utf-8").splitlines()
        seen: set[str] = set()
        for line in lines:
            match = re.fullmatch(r"([0-9a-fA-F]{64})  ([A-Za-z0-9_.-]+)", line)
            if not match:
                raise ValueError("Invalid SHA256SUMS line")
            digest, name = match.groups()
            if name in seen or name not in expected - {"SHA256SUMS.txt"}:
                raise ValueError("Duplicate or unexpected SHA256SUMS asset")
            seen.add(name)
            actual = hashlib.sha256((directory / name).read_bytes()).hexdigest()
            if actual != digest.lower():
                raise ValueError(f"Checksum mismatch: {name}")
        if seen != expected - {"SHA256SUMS.txt"}:
            raise ValueError("SHA256SUMS does not list all release assets")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", default=os.environ.get("GITHUB_REPOSITORY", ""))
    parser.add_argument("--new-tag", help="Verified new public stable tag (required for --apply)")
    parser.add_argument("--apply", action="store_true", help="Actually delete obsolete v0.x releases/tags")
    args = parser.parse_args()
    if not REPO.fullmatch(args.repo):
        parser.error("--repo must be an owner/name repository")
    releases = list_releases(args.repo)
    kept, candidates = plan_cleanup(releases)
    print(f"KEEP legacy releases: {', '.join(kept) or '(none)'}")
    print(f"KEEP all v1+ releases, drafts, prereleases and other tags (never delete)")
    print(f"PRUNE candidate count: {len(candidates)}")
    for tag in candidates:
        print(f"{'DELETE' if args.apply else 'DRY-RUN'} {tag}")
    if not args.apply:
        return
    if not args.new_tag:
        parser.error("--new-tag required with --apply")
    # No destructive request until real published artifacts have been verified.
    verify_current_release(args.repo, args.new_tag, releases)
    for tag in candidates:
        if not LEGACY.fullmatch(tag) or tag in kept or tag == args.new_tag:
            raise ValueError(f"Unsafe legacy cleanup target: {tag}")
        # A fresh remote read aborts if the release has disappeared or was changed.
        updated = list_releases(args.repo)
        now_kept, now_candidates = plan_cleanup(updated)
        if tag not in now_candidates or any(k not in [r["tag_name"] for r in updated] for k in kept):
            raise ValueError(f"Release inventory changed; aborting before deleting {tag}")
        if now_kept != kept:
            raise ValueError("Protected release set changed during cleanup")
        gh("release", "delete", tag, "--repo", args.repo, "--yes", "--cleanup-tag")
        print(f"REMOVED {tag} and its version tag")
    print("PASS: legacy cleanup complete; all v1+ releases preserved")


if __name__ == "__main__":
    main()
