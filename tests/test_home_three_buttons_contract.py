#!/usr/bin/env python3
"""Verify removed navigation is absent, not just hidden by View visibility."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
activity=(src/"MainActivity.kt").read_text()
home=(src/"HomeReferenceScreen.kt").read_text()
layout=(src/"HomeReferenceLayout.kt").read_text()
routes=(src/"ReferenceMenus.kt").read_text()
compact=activity.split("private fun showHome(){",1)[1].split("private fun confirmGameplayShop(",1)[0]
for key in ('onSkins={showSkins()}','onShop={showShop()}',
            'onSettings={showSettingsScreen()}'):
    assert key in compact,key
for key in ('pill("SKINS"','pill("SHOP"','pill("SETTINGS"'):
    assert home.count(key)==1,key
assert 'val actions=listOf(skins,shop,settings)' in layout
assert 'isClickable=false' in home
assert 'wallet("COINS",progress.coins(),' in home
assert "fun skins(" in routes and "fun shop(" in routes
for forbidden in ('fun levels(', 'LevelMapScreen', 'ProgressOverviewScreen',
                  'LeaderboardScreen','AchievementsScreen'):
    assert forbidden not in routes,forbidden
for marker in ('showLocalRecords(', 'showLocalAchievements(','showLevels(',
               'showProgressOverview(','showQuickOptions(',
               'selectedScreen="records"', 'selectedScreen="progress"',
               'selectedScreen="levels"', 'selectedScreen="achievements"',
               'action(content,"▶  PLAY"', 'shortcut("LEADERBOARD"',
               'shortcut("ACHIEVEMENTS"', 'shortcut("SOUND"'):
    assert marker not in activity,marker
for marker in ('HomePlayWings(', 'HomeMedalDrawable()', 'pill("PLAY"',
               'footerShortcut(', 'onAchievements:', 'onLeaderboard:',
               'onSound:', 'onStars:', '"Otvori kartu razina"'):
    assert marker not in home,marker
for suffix in (
    "ProgressOverviewScreen.kt","ProgressOverviewMetrics.kt",
    "LeaderboardScreen.kt","LeaderboardLayout.kt",
    "LocalLeaderboardPresentation.kt",
    "LevelMapScreen.kt","LevelMapGeometry.kt","LevelMapMarkers.kt",
    "LevelMapReferenceChrome.kt",
    "AchievementsScreen.kt","AchievementsLayout.kt",
    "AchievementCatalog.kt","HomePlayWings.kt","HomeMedalDrawable.kt"):
    assert not (src/suffix).exists(),suffix
assert (src/"ReferenceStage.kt").exists()
assert "internal class LevelMapStage(" in (src/"ReferenceStage.kt").read_text()
print("PASS: exactly three Home actions, no legacy pages, offline data retained")
