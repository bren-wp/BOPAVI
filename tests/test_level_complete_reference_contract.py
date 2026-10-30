#!/usr/bin/env python3
"""A finite victory has authentic persisted rewards, 3 stars and native routes."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
main=(src/"MainActivity.kt").read_text()
view=(src/"LevelCompleteScreen.kt").read_text()
model=(src/"LevelCompleteState.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/LevelCompleteStateTest.kt").read_text()
assert "progress.recordLevelStars(completed.first,completed.second,g.completedLevelStars)" in main
assert "earnedReward=progress.completeLevelFromRecordedRun(" in main
assert "val winData=LevelCompleteState.fromRun(" in main
assert "LevelCompleteScreen(this).create(" in main
assert "onShare={" in main
assert "Intent.ACTION_SEND" in main
assert "Intent.createChooser" in main
assert "if(winData.nextAvailable)" in main
for phrase in (
    'stage.place(label("LEVEL COMPLETE!",63f,Color.WHITE)',
    'for(index in 0..2)',
    'index<data.stars',
    '"NO NEW BONUS"',
    'stage.place(LevelCompleteChest(host),338f,1069f,265f,184f)',
    'button(if(data.nextAvailable)"NEXT LEVEL" else "LEVEL MAP"',
    'stage.place(button("HOME",false,onHome)',
    'stage.place(button("SHARE",false,onShare)',
    'stage.setOnApplyWindowInsetsListener',
    'minimumHeight=dp(48)',
):
    assert phrase in view,phrase
assert 'val nextAvailable:Boolean get()=level<LevelEngine.LEVELS_PER_WORLD.toLong()' in model
assert 'if(justAwardedCoins>0)' in model
assert "replayAndNonMilestoneDoNotPretendToGrantChestLoot" in unit
assert "endOfWorldDoesNotProduceInvalidNextLevel" in unit
assert "val electric" not in main
assert "android.permission.INTERNET" not in (root/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: real first-clear Level Complete, no fake chest grants, native share, accessible actions")
