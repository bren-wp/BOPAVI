#!/usr/bin/env python3
"""Regression guard: public/in-app Terms may not revoke MIT-licensed rights."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
license_text = (ROOT / "LICENSE").read_text(encoding="utf-8")
fdroid = (ROOT / "fdroid/metadata/com.brendigo.bopavi.yml").read_text(encoding="utf-8")
assert license_text.startswith("MIT License")
assert "License: MIT" in fdroid

paths = (
    "docs/play/TERMS_OF_USE.md",
    "docs/play/index.html",
    "docs/play/terms-of-use.html",
    "android/app/src/main/assets/legal/terms.html",
)
for path in paths:
    terms = (ROOT / path).read_text(encoding="utf-8")
    normalized = terms.replace("<strong>", "").replace("</strong>", "")
    for phrase in (
        "MIT licencom",
        "kopirati, mijenjati, distribuirati",
        "Ranije objavljene licence.",
        "pravo gospodarskog iskorištavanja",
    ):
        assert phrase in normalized, (path, phrase)
    for wrong in (
        "Vlasnička licenca, a ne slobodna licenca",
        "nije dodijeljena opća dozvola za kopiranje izvornog koda",
        "mijenjati programski kod i stvarati ili javno distribuirati neovlaštene izvedenice",
        "Ova vlasnička licenca ne opoziva",
    ):
        assert wrong not in normalized, (path, wrong)
print("PASS: candidate public and native Terms honor root MIT grants; art rights still require proof")
