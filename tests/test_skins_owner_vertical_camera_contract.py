#!/usr/bin/env python3
"""Skins camera follows owner tall screenshot, but compact UI stays safe."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
stage=(src/"ReferenceStage.kt").read_text()
layout=(src/"PilotSelectionLayout.kt").read_text()
screen=(src/"PilotSelectionScreen.kt").read_text()
assert "private var defaultVerticalAnchor=.5f" in stage
assert "fun setReferenceVerticalAnchor(anchor:Float)" in stage
assert "defaultVerticalAnchor=anchor.coerceIn(0f,1f)" in stage
assert "verticalAnchor:Float=defaultVerticalAnchor" in stage
assert "if(!slot.referenceAligned && !shopFullBleed)return safe" in stage
assert "const val ownerVerticalAnchor=.18f" in layout
assert "stage.setReferenceVerticalAnchor(PilotSelectionLayout.ownerVerticalAnchor)" in screen
reference=screen.split("if(host.resources.configuration.orientation==",1)[1].split(
    "root.addView(stage,",1)[0]
assert reference.index("stage.useShopFullBleedReferenceCamera()") < reference.index(
    "stage.setReferenceVerticalAnchor(PilotSelectionLayout.ownerVerticalAnchor)")
assert "fontScale<1.25f" in reference
assert "stage.place(card,left,PilotSelectionLayout.cardTop," in screen
assert "stage.place(selectedInfo,30f,1299f,881f,193f)" in screen
# Real upper hero/podium/selection groups must NOT all inherit the same
# .18f anchor on tall phones; it put real cards >150px too high.
decor=(src/"SkinsReferenceDecor.kt").read_text()
assert "private val podiumOnly:Boolean=false" in decor
assert "if(!podiumOnly)" in decor and "if(podiumOnly)" in decor
assert "SkinsReferenceDecor(host,podiumOnly=true)" in screen
assert "const val heroVerticalAnchor=.25f" in layout
assert "const val podiumVerticalAnchor=.37f" in layout
assert "const val cardsVerticalAnchor=.5f" in layout
assert "verticalAnchor=PilotSelectionLayout.heroVerticalAnchor" in screen
assert "verticalAnchor=PilotSelectionLayout.podiumVerticalAnchor" in screen
assert screen.count("verticalAnchor=PilotSelectionLayout.cardsVerticalAnchor")>=4
# Preserve card selection, unlock and purchase gating, no screenshot buttons.
assert "setOnClickListener{chooseOrUnlock(index)}" in screen
assert "if(PilotSelectionModel.locked(selected,orionOwned))onUnlock()" in screen
assert "else onChoose(selected)" in screen
def excess(h,w):
    scale=min(w/941,h/1672)
    return max(0,h-1672*scale)
assert excess(1920,1080)<4
assert 145<(0.5-0.18)*excess(2400,1080)<160
print("PASS: Skins tall owner camera shift and compact-safe fallback")
