#!/usr/bin/env python3
"""Regression contract: Android OS tutorial cannot mask Settings -> Home."""
from pathlib import Path
shell=(Path(__file__).resolve().parents[1]/"tools/qa_android_emulator.sh").read_text()
assert 'return_home_from_settings(){' in shell
assert 'android:id/immersive_cling_title' in shell
assert 'android:id/ok' in shell
assert 'n.get("package")=="android"' in shell
assert 'dismissed exact Android immersive-mode education dialog' in shell
assert 'Settings native HOME button did not return Home' in shell
print("PASS: Settings -> Home handles only the exact Android OS immersive tutorial")
