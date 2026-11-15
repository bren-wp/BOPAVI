#!/usr/bin/env python3
"""Owner Home wordmark: three cyan glyphs followed by three gold glyphs.

Checks actual vector source and independently rendered letter geometry.
Does not substitute artwork for screenshots or claim RGB equality.
"""
from pathlib import Path
from xml.etree import ElementTree as ET
root=Path(__file__).resolve().parents[1]
svg=(root/"docs/assets/logo.svg").read_text(encoding="utf-8")
xml=ET.fromstring(svg)
ns="{http://www.w3.org/2000/svg}"
uses=list(xml.iter(ns+"use"))
for glyph,colour in (("ownerB","cyanText"),("ownerO","cyanText"),
                     ("ownerP","cyanText"),("ownerA","goldText"),
                     ("ownerBlue","goldText"),("ownerI","goldText")):
    assert sum(u.attrib.get("href")=="#"+glyph and
        u.attrib.get("fill")=="url(#"+colour+")" for u in uses)==1,glyph
for glyph in ("ownerB","ownerO","ownerP","ownerA","ownerBlue","ownerI"):
    assert any(node.attrib.get("id")==glyph for node in xml.iter()),glyph
assert 'fill="#ffd041"' in svg
assert 'fill="#19bdf9" stroke="#092553"' not in svg
assert "B/O/P icy cyan, A/V/I warm gold" in svg
print("PASS: BOP cyan + AVI gold in vector logo, counters preserved")
