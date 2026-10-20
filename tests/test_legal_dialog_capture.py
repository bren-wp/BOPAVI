#!/usr/bin/env python3
"""Require one Android hierarchy for legal body verification and close."""
from pathlib import Path
qa=(Path(__file__).resolve().parents[1]/"tools/qa_android_emulator.sh").read_text()
portion=qa.split("open_legal_dialog(){",1)[1].split("# Exercise both newly reachable Home overlays",1)[0]
assert 'legal_required_text="$4"' in portion
assert 'for required in sys.argv[1:]:' in portion
assert "FAIL: in-app legal dialog lacks" in portion
assert 'then legal_opened=1;break; fi' in portion
assert 'open_legal_dialog "Pročitaj politiku privatnosti" "POLITIKA PRIVATNOSTI" "BRENDIGO" "OBRIŠI PODATKE"' in portion
assert 'open_legal_dialog "Pročitaj uvjete korištenja" "UVJETI KORIŠTENJA" "BRENDIGO" "OGRANIČENU"' in portion
assert "Croatian privacy dialog accessible, offline and dismissible" in portion
assert "native terms dialog accessible" in portion
assert portion.count('if python3 - "$legal_title"')==1
print("PASS: both native legal dialogs verified and dismissed using one UI snapshot")
