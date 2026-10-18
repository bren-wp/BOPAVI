#!/usr/bin/env python3
"""Fail closed: product and source art edits cannot be skipped by metadata gate."""
import importlib.util
from pathlib import Path
root=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location("gate",root/"tests/release_publish_guard.py")
m=importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)
assert m.is_metadata_only([])
assert m.is_metadata_only(["README.md","docs/qa/CRASH-TRIAGE.md","tests/test_release_contract.py",".github/workflows/native-ci.yml"])
for entries in (
    ["android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt"],
    ["ios/BOPAVI/BopaviCore.swift"],
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
print("PASS: metadata-only release guard rejects app and art changes")
