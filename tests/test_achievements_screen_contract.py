#!/usr/bin/env python3
"""Full-screen achievements reflect existing offline counters, not invented awards."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
main=(src/"MainActivity.kt").read_text()
screen=(src/"AchievementsScreen.kt").read_text()
model=(src/"AchievementCatalog.kt").read_text()
layout=(src/"AchievementsLayout.kt").read_text()
test=(root/"android/app/src/test/java/com/brendigo/bopavi/AchievementCatalogTest.kt").read_text()
qa=(root/"tools/qa_android_reference_menus.py").read_text()
assert 'private fun showLocalAchievements(){' in main
assert 'selectedScreen="achievements"' in main
assert 'AchievementsScreen(this).create(snapshot,onBack={showHome()})' in main
for field in ('progress.wins()','progress.totalRuns()','progress.totalFlaps()',
              'progress.bestFood()','progress.endlessBest()','progress.bestPoints()',
              'progress.collectibles(0)','progress.deaths()'):
    assert field in main,field
for name in ('FIRST VICTORY','SKY EXPLORER','WING MASTER','BERRY HUNTER',
             'GATE RUNNER','HIGH FLYER','MEADOW FINDER','KEEP FLYING'):
    assert name in model,name
for term in ('AchievementCatalog.goals(snapshot)','g.percent','contentDescription=g.accessibility',
             'setOnClickListener{onBack()}','stage.setSafe(','minimumHeight=dp(48)',
             'ScrollView(host)','AchievementsLayout.cols[index%2]'):
    assert term in screen,term
assert 'val rows=floatArrayOf(517f,743f,969f,1195f)' in layout
assert 'freshInstallHasEightUnreachedGoals' in test
assert 'cappedPercentHandlesExtremeFlapsAndFailingRuns' in test
assert 'capture_png("android-achievements-reference-size")' in qa
assert 'progress.buyPerk' not in screen and 'progress.claimBonusCoins' not in screen
print("PASS: eight real goals, accessibility and responsive native progress cards")
