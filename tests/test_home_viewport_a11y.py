#!/usr/bin/env python3
"""Actual native Home targets are exactly three on compact and owner cameras."""
from pathlib import Path
import importlib.util
import xml.etree.ElementTree as ET
p=Path(__file__).resolve().parents[1]/"tools/qa_home_viewports.py"
spec=importlib.util.spec_from_file_location("home_viewports",p)
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
assert set(m.TARGETS)=={"Settings","Skins","Shop"}
root=ET.Element("hierarchy")
for i,(name,label) in enumerate((
  ("Settings","Otvori opcije igre"),
  ("Skins","Otvori izbor likova"),
  ("Shop","Otvori trgovinu nadogradnji"))):
    ET.SubElement(root,"node",{"package":m.PACKAGE,"clickable":"true",
       "bounds":f"[50,{100+i*140}][500,{170+i*140}]","content-desc":label})
assert m.visible_targets(root,720,1280,160)==set(m.TARGETS)
assert not m.scrollable_home(root)
ET.SubElement(root,"node",{"package":m.PACKAGE,"clickable":"true",
    "bounds":"[50,100][500,170]","content-desc":"Otvori opcije igre"})
try:
    m.visible_targets(root,720,1280,160)
except AssertionError as exc:
    assert "duplicate" in str(exc)
else: raise AssertionError("Duplicate Home action must fail")
print("PASS: 3 native Home targets and duplicate-control guard")
