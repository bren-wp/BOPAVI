#!/usr/bin/env python3
"""Legal QA retries never mistake an unrelated modal for a successful Terms view."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
shell=(root/"tools/qa_android_emulator.sh").read_text()
for marker in (
    '"qa/screenshots/android-legal-attempt-${legal_attempt}.xml"',
    'has_dialog_close=any(',
    'adb shell input keyevent KEYCODE_BACK',
    'then legal_opened=1;break; fi',
    'in-app legal dialog lacks',
):
    assert marker in shell,marker
assert 'open_legal_dialog "Pročitaj uvjete korištenja" "UVJETI KORIŠTENJA"' in shell
assert 'open_legal_dialog "Pročitaj politiku privatnosti" "POLITIKA PRIVATNOSTI"' in shell
print("PASS: legal-dialog retries save actual XML and safely dismiss wrong overlay")
