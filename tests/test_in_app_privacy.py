#!/usr/bin/env python3
"""Both complete legal documents must be preserved verbatim from owner index.html."""
from pathlib import Path
import re

root=Path(__file__).resolve().parents[1]
main=(root/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
manifest=(root/"android/app/src/main/AndroidManifest.xml").read_text()
gradle=(root/"android/app/build.gradle.kts").read_text()
html=(root/"docs/play/index.html").read_text(encoding="utf-8")
legal=root/"android/app/src/main/assets/legal"

for source_id,asset,sections in (("privatnost","privacy.html",12),("uvjeti","terms.html",13)):
    match=re.search(r'<article\s+id="'+source_id+r'"[\s\S]*?</article>',html)
    assert match,source_id
    bundled=(legal/asset).read_text(encoding="utf-8")
    contact=re.search(r'<section\s+id="kontakt"[\s\S]*?</section>',html) if source_id=="uvjeti" else None
    expected=match.group(0)+("\n"+contact.group(0) if contact else "")
    assert bundled==expected,f"{asset}: legal text differs from owner source"
    assert len(re.findall(r"<h3(?:\s[^>]*)?>",bundled))==sections,(asset,"missing section")
    assert "<script" not in bundled.lower()
    assert "info@brendigo.com" in bundled
    assert "20. listopada 2026." in bundled

terms=(legal/"terms.html").read_text(encoding="utf-8")
privacy=(legal/"privacy.html").read_text(encoding="utf-8")
for term in ("Vlasnička licenca, a ne slobodna licenca","Ranije objavljene licence.",
             "prava potrošača","pravo gospodarskog iskorištavanja"):
    assert term in terms,term
for term in ("Agenciji za zaštitu osobnih podataka (AZOP)","Obriši podatke",
             "Međunarodni prijenosi","Škurinjskih žrtava 2"):
    assert term in privacy,term

for term in ('private fun showPrivacyNotice()', 'private fun showTermsNotice()',
             'showLegalNotice("Politika privatnosti","privacy.html")',
             'showLegalNotice("Uvjeti korištenja","terms.html")',
             'android.text.Html.fromHtml(markup,android.text.Html.FROM_HTML_MODE_COMPACT)',
             'android.widget.ScrollView(this)','assets.open("legal/$file")',
             'movementMethod=android.text.method.LinkMovementMethod.getInstance()',
             ):
    assert term in main,term
settings=(root/"android/app/src/main/java/com/brendigo/bopavi/SettingsScreen.kt").read_text()
assert 'SettingsLegalArrow(host,"Pročitaj politiku privatnosti",onPrivacy)' in settings
assert 'SettingsLegalArrow(host,"Pročitaj uvjete korištenja",onTerms)' in settings
assert 'onPrivacy={showPrivacyNotice()}' in main
assert 'onTerms={showTermsNotice()}' in main
assert "compactDisplay=resources.configuration.screenHeightDp<770" in main
assert "tinyDisplay=resources.configuration.screenHeightDp<610" in main
assert "github.com" not in main.split("private fun showLegalNotice(",1)[1].split("private fun showHome()",1)[0]
assert "android.permission.INTERNET" not in manifest
assert "targetSdk = 36" in gradle
qa=(root/"tools/qa_android_emulator.sh").read_text()
assert "Croatian privacy dialog accessible, offline and dismissible" in qa
assert "Settings legal control missing" in qa
assert "native terms dialog accessible" in qa
print("PASS: matched public and in-app legal documents, 25 sections, optional cloud backup disclosures")
