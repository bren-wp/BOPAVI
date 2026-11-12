#!/usr/bin/env python3
"""Guard real Shop/Skins emulator helpers against removal during Home cleanup.

Pure source/fixture checks run without an Android emulator. Actual UI interactions
and authentic screenshots remain mandatory in the Android 15/16 jobs.
"""
from pathlib import Path
import sys
from xml.etree import ElementTree as ET

root=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(root/"tools"))
import qa_android_reference_menus as native

def fixture(entries):
    hierarchy=ET.Element("hierarchy")
    for package,caption,clickable,enabled in entries:
        ET.SubElement(hierarchy,"node",{
            "package":package,"content-desc":caption,"text":caption,
            "clickable":str(clickable).lower(),
            "enabled":str(enabled).lower(),
            "bounds":"[20,30][280,150]",
        })
    return hierarchy

nodes=fixture([
    (native.APP,"Otvori trgovinu nadogradnji",True,True),
    (native.APP,"Otvori izbor likova",True,True),
    (native.APP,"Earned coins: 50",False,True),
    ("com.android.systemui","Otvori trgovinu nadogradnji",True,True),
    (native.APP,"Otvori trgovinu nadogradnji",False,True),
    (native.APP,"Otvori trgovinu nadogradnji",True,False),
])
assert len(native.matching_nodes(nodes,"OTVORI TRGOVINU NADOGRADNJI",
                                  clickable=True))==1
assert len(native.matching_nodes(nodes,"earned COINS:",clickable=False))==1
assert native.matching_nodes(nodes,"Progress",clickable=True)==[]
assert not native.acknowledge_native_fullscreen_education(nodes)
original_hierarchy=native.hierarchy
original_tap=native.tap
try:
    native.hierarchy=lambda:nodes
    selected=native.node_with("OTVORI TRGOVINU NADOGRADNJI",attempts=1)
    assert selected.get("package")==native.APP
    duplicate=fixture([
        (native.APP,"HOME",True,True),
        (native.APP,"HOME",True,True),
    ])
    native.hierarchy=lambda:duplicate
    try:
        native.node_with("HOME",attempts=1)
        raise AssertionError("Duplicate native controls must fail closed")
    except AssertionError as exc:
        assert "Duplicate native action" in str(exc)
    native.hierarchy=lambda:fixture([(native.APP,"SKINS",False,True)])
    try:
        native.node_with("SKINS",clickable=True,attempts=1)
        raise AssertionError("Non-clickable label must not count as a button")
    except AssertionError as exc:
        assert "Missing native" in str(exc)
    education=fixture([
        ("android","Viewing full screen",False,True),
        ("android","To exit, swipe down from the top of your screen",False,True),
        ("android","Got it",True,True),
    ])
    tapped=[]
    native.tap=lambda n:tapped.append(n.get("content-desc"))
    assert native.acknowledge_native_fullscreen_education(education)
    assert tapped==["Got it"]
    education=fixture([
        (native.APP,"Viewing full screen",False,True),
        (native.APP,"To exit, swipe down from the top of your screen",False,True),
        (native.APP,"Got it",True,True),
    ])
    assert not native.acknowledge_native_fullscreen_education(education)
    assert tapped==["Got it"],"Never dismiss an app-owned UI as system overlay"
finally:
    native.hierarchy=original_hierarchy
    native.tap=original_tap
focus=(root/"tools/qa_android_shop_focus.py").read_text()
for needed in ("matching_nodes", "node_with",
               "acknowledge_native_fullscreen_education",
               "android-shop-focus-reference-size",
               "android-skins-focus-reference-size"):
    assert needed in focus,needed
print("PASS: strict native Shop/Skins helper bindings, unique controls and OS-only overlay dismissal")
