#!/usr/bin/env python3
"""Native Home uses one three-button vector composition, no deleted footer routes."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
src=r/"android/app/src/main/java/com/brendigo/bopavi"
h=(src/"HomeReferenceScreen.kt").read_text()
l=(src/"HomeReferenceLayout.kt").read_text()
m=(src/"MainActivity.kt").read_text()
e=(src/"HomeEnamelDrawable.kt").read_text()
assert 'HomeReferenceScreen(this,progress).create(' in m
for item in ('R.drawable.homeback','R.drawable.logo','HomeWingActor(host',
             'HomeOrbitDecoration(host)','HomeTopWalletRail(host)',
             'pill("SKINS"','pill("SHOP"','pill("SETTINGS"',
             'ReferenceButtonCluster.measure(','HomeEnamelDrawable(',
             'setOnClickListener{onClick()}','minimumHeight=dp(48)',
             'stage.setOnApplyWindowInsetsListener','stage.setSafe('):
    assert item in h,item
for deleted in ('pill("PLAY"','HomePlayWings(', 'footerShortcut(',
                'wallet("STARS"', 'onSound:','onLeaderboard:'):
    assert deleted not in h,deleted
assert 'isClickable=false' in h
assert 'val actions=listOf(skins,shop,settings)' in l
for button in ('val skins=HomeBounds(', 'val shop=HomeBounds(',
               'val settings=HomeBounds('):
    assert button in l,button
assert 'val play=HomeBounds(' not in l
assert 'val achievements=HomeBounds(' not in l
assert 'val leaderboard=HomeBounds(' not in l
assert 'val sound=HomeBounds(' not in l
assert "if(sourceHome)0xff102347.toInt() else 0x77001b4e" in e
print("PASS: three functional 3D Home buttons, no dead CTA artwork")
