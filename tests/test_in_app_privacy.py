#!/usr/bin/env python3
"""In-app privacy text is accessible offline without adding permissions."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
main=(r/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
manifest=(r/"android/app/src/main/AndroidManifest.xml").read_text()
gradle=(r/"android/app/build.gradle.kts").read_text()
assert "private fun showPrivacyNotice()" in main
assert ".setTitle(\"Politika privatnosti\")" in main
for term in ("Izdavač: Brendigo","ne šalje rezultate","Obriši podatke",
             "POLITIKA PRIVATNOSTI","Pročitaj politiku privatnosti"):
    assert term in main,term
assert "setOnClickListener { showPrivacyNotice() }" in main
assert "LinearLayout.LayoutParams(-1,d(58))" in main
assert "compactDisplay=resources.configuration.screenHeightDp<700" in main
assert "android.permission.INTERNET" not in manifest
assert "targetSdk = 36" in gradle
print("PASS: native Croatian privacy dialog, responsive legal action, offline API36")

# The emulator must verify the real dialog, not merely assert a source string.
qa=(r/"tools/qa_android_emulator.sh").read_text()
assert "Croatian privacy dialog accessible, offline and dismissible" in qa
assert "privacy button below 48dp" in qa
