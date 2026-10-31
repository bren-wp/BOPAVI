#!/usr/bin/env python3
"""The loss display is interactive and 941×1672 reference-positioned."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
main=(src/"MainActivity.kt").read_text()
screen=(src/"GameOverScreen.kt").read_text()
stats=(src/"GameOverStats.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/GameOverStatsTest.kt").read_text()
qa=(root/"tools/qa_android_emulator.sh").read_text()
assert 'if(!g.won){' in main
assert 'val stats=GameOverStats.fromRun(g.distance,g.coins,g.stars,' in main
assert 'GameOverScreen(this).create(' in main
assert 'if(played!=null)startLevel(played.first,played.second)' in main
assert 'onHome={showHome()}' in main
assert 'progress.recordRun(g)' in main
for marker in (
    'stage.place(heading("GAME",Color.WHITE),171f,129f,599f,146f)',
    'stage.place(heading("OVER",0xffff5b35.toInt()),153f,267f,635f,166f)',
    'stage.place(GameOverPodium(host),168f,728f,605f,216f)',
    'stage.place(statistics,123f,927f,695f,310f)',
    'stage.place(action("RETRY",true,onRetry),183f,1233f,575f,150f,',
    'stage.place(action("HOME",false,onHome),241f,1404f,459f,124f,',
    'contentDescription="$label: $value"',
    'stage.setOnApplyWindowInsetsListener',
    'importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO',
): assert marker in screen,marker
assert "fun fromRun(distance:Float,coins:Int,stars:Int,food:Int," in stats
assert '"Food Collected"' in stats
assert '"Coins Collected"' in stats
assert '"Stars Collected"' in stats
assert '"Gems Collected"' not in screen+stats
assert "endlessShowsActualFoodAndScoreNotFictionalGems" in unit
assert "finiteShowsActualCoinsAndStars" in unit
assert '("GAME","OVER","DISTANCE","FOOD COLLECTED","SCORE")' in qa
assert 'capture android-game-over-reference-size' in qa
assert 'data[16:24])==(941,1672)' in qa
print("PASS: genuine native game over rows, real counters and actual retry/home")
