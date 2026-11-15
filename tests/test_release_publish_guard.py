#!/usr/bin/env python3
"""Fail closed: product and source art edits cannot be skipped by metadata gate."""
import importlib.util
from pathlib import Path
root=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location("gate",root/"tests/release_publish_guard.py")
m=importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)
assert m.is_metadata_only([])
assert m.is_metadata_only(["CHANGELOG.md", "README.md","docs/qa/CRASH-TRIAGE.md","tests/test_release_contract.py","android/app/src/test/java/com/brendigo/bopavi/LevelEngineTest.kt",".github/workflows/native-ci.yml","tools/cleanup_legacy_releases.py","tools/sync_release_docs.py"])

# Regression: exact changed paths from v1.0.88's published commit to PR #237 merge.
# Main Actions 43761835605 failed solely because CHANGELOG.md was omitted.
POST_MERGE_DOCS_DIFF = [
    ".github/workflows/native-ci.yml",
    "CHANGELOG.md",
    "README.md",
    "docs/audits/BOPAVI-v1.0.88-CYCLE.md",
    "docs/play/RELEASE-CHECKLIST.md",
    "docs/play/STORE-LISTING-hr-HR.md",
    "docs/product/CURRENT_SCREENS.md",
    "docs/product/FEATURE-MATRIX.md",
    "docs/release/NEXT.md",
    "docs/release/PRODUCTION-READINESS.md",
    "docs/release/PUBLISHED-VS-CANDIDATE.md",
    "tests/test_marketing_readme.py",
    "tests/test_published_play_guard.py",
    "tests/test_store_claims.py",
    "tests/test_sync_release_docs.py",
    "tools/sync_release_docs.py",
]
assert m.is_metadata_only(POST_MERGE_DOCS_DIFF), "Metadata-only merges must not require a new Android version"

# Fail closed for every product/runtime/asset path, and near-miss metadata names.
for path in ("CHANGELOG.md.bak", "CHANGELOG.txt",
             "android/app/src/main/java/com/brendigo/bopavi/GameView.kt",
             "android/app/src/main/res/raw/click.wav", "tools/release_notes.py",
             "android/app/proguard-rules.pro", "docs/assets/hero.svg",
             "docs/../android/app/build.gradle.kts"):
    assert not m.is_metadata_only(["CHANGELOG.md", path]), path
for entries in (
    ["android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt"],
    ["tools/generate_audio.py"],
    ["android/app/build.gradle.kts"],
    ["design/ART-DIRECTION.md"],
    ["tools/generate_images.py"],
    ["docs/assets/logo.svg"],
    ["docs/qa/CRASH-TRIAGE.md","android/app/src/main/AndroidManifest.xml"],
    ["../evil"],[""],
):
    assert not m.is_metadata_only(entries),entries
c=(root/".github/workflows/native-ci.yml").read_text()
assert "git diff --name-only" in c
assert "tests/release_publish_guard.py" in c
assert r"""if printf '%s\n' "$changed" | python3 tests/release_publish_guard.py; then""" in c
assert "grep -Ev" not in c
print("PASS: metadata-only release guard rejects app and art changes")
