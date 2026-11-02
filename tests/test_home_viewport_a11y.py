#!/usr/bin/env python3
"""Regression for genuine scroll-safe Home QA; no emulator mocked as passing."""
from pathlib import Path
import importlib.util
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
path = root / "tools/qa_home_viewports.py"
spec = importlib.util.spec_from_file_location("qa_home_viewports", path)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

def page(*controls, scrollable=True):
    top = ET.Element("hierarchy")
    ET.SubElement(top, "node", {
        "package": module.PACKAGE, "class": "android.widget.ScrollView",
        "scrollable": str(scrollable).lower(), "clickable": "false",
        "bounds": "[0,0][480,800]"
    })
    for name, top_y, bottom_y in controls:
        ET.SubElement(top, "node", {
            "package": module.PACKAGE, "class": "android.widget.LinearLayout",
            "clickable": "true", "content-desc": name,
            "bounds": f"[10,{top_y}][240,{bottom_y}]"
        })
    return top

first = page(
    ("▶  PLAY", 580, 648), ("Otvori izbor likova", 656, 713),
    ("Otvori trgovinu nadogradnji", 656, 713),
    ("Otvori opcije igre", 720, 777)
)
second = page(
    ("Pogledaj svoja postignuća", 615, 692),
    ("Pogledaj svoje rekorde", 615, 692),
    ("Otvori zvučne postavke", 615, 692)
)
a = module.visible_targets(first, 480, 800, 160)
b = module.visible_targets(second, 480, 800, 160)
assert a | b == set(module.TARGETS), (a, b)
assert "Records" not in a and "Records" in b
assert module.scrollable_home(first)
assert not module.scrollable_home(page(scrollable=False))
# Undersized controls must fail rather than making CI appear green.
try:
    module.visible_targets(page(("Otvori opcije igre", 100, 138)), 480, 800, 160)
except AssertionError as exc:
    assert "smaller than 48dp" in str(exc)
else:
    raise AssertionError("Unsafe native control unexpectedly passed")
# Duplicated controls must fail rather than hiding accessibility regressions.
try:
    module.visible_targets(page(("Otvori opcije igre", 100, 160),
                                ("Otvori opcije igre", 180, 240)),
                           480, 800, 160)
except AssertionError as exc:
    assert "duplicate" in str(exc)
else:
    raise AssertionError("Duplicate accessibility target unexpectedly passed")
print("PASS: compact Home controls scroll into view, keep >=48dp and remain unique")
