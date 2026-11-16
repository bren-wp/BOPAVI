#!/usr/bin/env python3
"""Real results selected-skin fidelity and reduced-motion preference."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
p=r/"android/app/src/main/java/com/brendigo/bopavi"
s=(p/"LevelCompleteScreen.kt").read_text()
m=(p/"MainActivity.kt").read_text()
assert 'R.drawable.bopiportrait8 -> Triple(' in s
assert 'R.drawable.bopileft8,R.drawable.bopiright8,R.drawable.bopi8)' in s
assert 'reducedMotion:Boolean=false):View{' in s
assert 'heroLayers.third,allowMotion=!reducedMotion &&' in s
assert 'reducedMotion=progress.lessMotion())' in m
for t in ('onNext','onHome','onShare','data.collectedText',
 'data.rewardText','data.rating','data.scoreText'):
 assert t in s,t
assert 'HomeWingActor(host' in s
print("PASS: actual Orion victory results and saved reduced-motion preference")
