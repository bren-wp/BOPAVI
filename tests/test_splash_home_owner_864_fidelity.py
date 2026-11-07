#!/usr/bin/env python3
"""Splash & Home-only fidelity gate: require source-derived logo contours,
perspective rings, real native controls, and an honest 864px ROI tool."""
from pathlib import Path
from xml.etree import ElementTree as ET

root=Path(__file__).resolve().parents[1]
svg=(root/"docs/assets/logo.svg").read_text()
loading=(root/"android/app/src/main/java/com/brendigo/bopavi/FlightLoadingView.kt").read_text()
home=(root/"android/app/src/main/java/com/brendigo/bopavi/HomeReferenceScreen.kt").read_text()
enamel=(root/"android/app/src/main/java/com/brendigo/bopavi/HomeEnamelDrawable.kt").read_text()
qa=(root/"tools/qa_owner_864_splash_home.py").read_text()
xml=ET.fromstring(svg)
assert xml.tag.endswith("svg")
for identifier in ("ownerWarm","ownerBlue"):
    assert any(x.attrib.get("id")==identifier for x in xml.iter()),identifier
assert "DejaVu Sans" not in svg
assert "fill-rule=\"evenodd\"" in svg
assert "0 37" in svg and "0 43" in svg
for trace in (
    "ring(canvas,80f,1230f,150f,190f,-21f)",
    "ring(canvas,350f,989f,72f,106f,-25f)",
    "ring(canvas,546f,1057f,37f,54f,-21f)",
    "ring(canvas,746f,1097f,14f,23f,-18f)",
):
    assert trace in loading,trace
for fragment in ("R.drawable.logo", "R.drawable.homeback",
                 "HomeWingActor(host", "HomeReferenceLayout.play",
                 "HomeReferenceLayout.skins", "HomeReferenceLayout.shop",
                 "HomeReferenceLayout.settings", "sourceHome=true",
                 "progress.coins()", "progress.worldStars(it)"):
    assert fragment in home,fragment
assert "private val sourceHome:Boolean=false" in enamel
assert "if(sourceHome)30f else 22f" in enamel
assert "MAX_RGB_MAE = 25.5" in qa
assert "REFERENCE_SIZE = (864, 1536)" in qa
assert "CAPTURE_SIZE = (941, 1672)" in qa
assert "reference.convert(\"RGB\").resize(" in qa
assert "real = frame.convert(\"RGB\")" in qa
assert "ImageOps.fit" not in qa
print("PASS: both source-facing screens retain true UI, vector brand, "
      "gold perspective and honest pixel-ROI comparison")
