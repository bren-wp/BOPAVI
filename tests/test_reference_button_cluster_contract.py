#!/usr/bin/env python3
"""Four-screen 3D button/HUD geometry source contract, not visual acceptance."""
from pathlib import Path
src=Path(__file__).resolve().parents[1]/"android/app/src/main/java/com/brendigo/bopavi"
home=(src/"HomeReferenceScreen.kt").read_text()
rail=(src/"HomeTopWalletRail.kt").read_text()
over=(src/"GameOverScreen.kt").read_text()
model=(src/"ReferenceButtonCluster.kt").read_text()
assert 'ReferenceButtonCluster.measure(' in home
assert 'ReferenceButtonCluster.measure(' in over
assert 'measure.measureText(text)' in home
assert 'textMeasure.measureText(label)' in over
assert 'marginStart=cluster.gap' in home and 'marginStart=cluster.gap' in over
assert 'val gap=(width*.016f).toInt().coerceIn(4,12)' in model
assert 'safeTop+e.b.y*scale' not in rail
assert 'maxOf(safeTop,(it.b.y*scale).toInt())' in rail
assert 'setOnClickListener{onClick()}' in over
print("PASS: compact 3D CTA spacing and pinned safe wallet geometry")
