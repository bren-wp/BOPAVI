#!/usr/bin/env python3
"""Croatian gameplay/legal text and reference English Home labels; keep saves."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]/"android/app/src/main/java/com/brendigo/bopavi"
a=(r/"MainActivity.kt").read_text()
v=(r/"GameView.kt").read_text()
s=(r/"ProgressStore.kt").read_text()
l=(r/"LevelEngine.kt").read_text()
for forbidden in ("GameLanguages", "hudVocabulary", "languageCode=", "Tap to fly with",
                  "▶  RETRY", "BEST SCORE:"):
    assert forbidden not in a+v+l, forbidden
for term in ('"▶  PLAY"', '"REZULTAT"', '"HRANA"', "PONOVO",
             '"←  IZLAZ"', 'backAction(panel,"POČETNA")',
             'contentDescription=caption'):
    assert term in a,term
for term in ('"DODIRNI ZA LET"', '"DODIR = ZAMAH KRILIMA"',
             'Životi: $lives/', '"Bopi leti. Dodirni za zamah krilima."'):
    assert term in v,term
assert "fun importJson(contents: String)" in s and "fun exportJson()" in s
assert "ui_language" in s and "fun languageCode()" not in s
print("PASS: Croatian live UI and historic save keys")
