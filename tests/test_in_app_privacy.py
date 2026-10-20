#!/usr/bin/env python3
"""BOPAVI offline legal links must remain native, readable and reachable."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
main=(r/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
manifest=(r/"android/app/src/main/AndroidManifest.xml").read_text()
gradle=(r/"android/app/build.gradle.kts").read_text()
for term in ("private fun showPrivacyNotice()", '.setTitle("Politika privatnosti")',
             "Izdavač: Brendigo","ne šalje rezultate","Obriši podatke",
             "Pročitaj politiku privatnosti","private fun showTermsNotice()",
             '.setTitle("Uvjeti korištenja")',"Pročitaj uvjete korištenja",
             "paintFlags=paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG",
             "LinearLayout.LayoutParams(0,d(48),1f)"):
    assert term in main,term
assert 'legalLink("Politika privatnosti"' in main
assert 'legalLink("Uvjeti korištenja"' in main
assert "setOnClickListener{click()}" in main
assert "compactDisplay=resources.configuration.screenHeightDp<700" in main
assert "tinyDisplay=resources.configuration.screenHeightDp<610" in main
assert "github.com" not in main.split("private fun showPrivacyNotice()",1)[1].split("private fun showHome()",1)[0]
assert "android.permission.INTERNET" not in manifest
assert "targetSdk = 36" in gradle
qa=(r/"tools/qa_android_emulator.sh").read_text()
assert "Croatian privacy dialog accessible, offline and dismissible" in qa
assert "privacy button below 48dp" in qa
assert "native terms dialog accessible" in qa
print("PASS: offline Croatian legal links, accessible targets and no extra permissions")
