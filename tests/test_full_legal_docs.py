#!/usr/bin/env python3
"""Static source-level check of exact offline HTML legal resources."""
from pathlib import Path
import re
from html.parser import HTMLParser

class Structure(HTMLParser):
    def __init__(self):
        super().__init__()
        self.headers=[]
        self.anchors=[]
    def handle_starttag(self,tag,attrs):
        if tag=="h3":self.headers.append(tag)
        if tag=="a":self.anchors.append(dict(attrs).get("href",""))

root=Path(__file__).resolve().parents[1]
canonical=(root/"docs/play/index.html").read_text(encoding="utf-8")
assert "<script" not in canonical.lower()
assert "bopavi.com.hr" in canonical
for section,filename,count in (("privatnost","privacy.html",11),("uvjeti","terms.html",12)):
    match=re.search(r'<article\s+id="'+section+r'"[\s\S]*?</article>',canonical)
    assert match,section
    data=(root/"android/app/src/main/assets/legal"/filename).read_text(encoding="utf-8")
    contact=re.search(r'<section\s+id="kontakt"[\s\S]*?</section>',canonical) if section=="uvjeti" else None
    expected=match.group()+("\n"+contact.group() if contact else "")
    assert data==expected
    parsed=Structure();parsed.feed(data);parsed.close()
    assert len(parsed.headers)==count
    assert all(h.startswith(("mailto:","https://")) for h in parsed.anchors)
    assert "info@brendigo.com" in data
    assert "20. listopada 2026." in data
    assert data in (root/"docs/play"/("privacy-policy.html" if section=="privatnost" else "terms-of-use.html")).read_text(encoding="utf-8")
for path in ("PRIVACY_POLICY.md","TERMS_OF_USE.md"):
    assert "20. listopada 2026." in (root/"docs/play"/path).read_text(encoding="utf-8")
print("PASS: full offline legal HTML parity, original sections, no scripts or unapproved links")
