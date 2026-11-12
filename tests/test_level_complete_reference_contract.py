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
    'stage.place(ResultWingDecoration(host),139f,129f,663f,177f)',
    'ResultEmblemLabel(host,"LEVEL",89f,',
    'ResultEmblemLabel(host,"COMPLETE!",88f,',
    'contentDescription="Level ${data.level} complete!"',
    'stage.place(stars,259f,811f,423f,142f)',
    'stage.place(HomeWingActor(host,heroLayers.first,heroLayers.second,',
    '209f,348f,523f,523f)',
    '},100f,929f,741f,278f)',
    'stage.place(statRow,151f,975f,422f,187f)',
    'stage.place(reward,547f,1158f,269f,45f)',
    'for(index in 0..2)',
    'index<data.stars',
    'stage.place(LevelCompleteChest(host),567f,975f,222f,192f)',
    'button(if(data.nextAvailable)"NEXT LEVEL" else "RETRY LEVEL"',
    'stage.place(button("HOME",false,onHome)',
    'stage.place(button("SHARE",false,onShare)',
    'stage.setOnApplyWindowInsetsListener',
    'minimumHeight=dp(48)',
):
    assert phrase in view,phrase
assert 'val nextAvailable:Boolean get()=level<LevelEngine.LEVELS_PER_WORLD.toLong()' in model
assert 'if(justAwardedCoins>0)' in model
assert '"NO NEW BONUS"' in model
assert 'label(data.rewardText,25f,' in view
assert 'R.drawable.bopiportrait9 -> Triple(' in view
assert 'R.drawable.bopiportrait10 -> Triple(' in view
assert 'R.drawable.bopileft0,R.drawable.bopiright0,R.drawable.bopi0' in view
for detail in (
    "canvas.translate(0f,radius*.11f)",
    "if(earned)0xff98521a.toInt() else 0xff344660.toInt()",
    "if(earned) {",
    "paint.color=0xbaffffff.toInt()",
    "canvas.drawRoundRect(118f,86f,147f,97f,4f,4f,paint)",
    "paint.color=0xaafff3ce.toInt()",
):
    assert detail in view,detail
assert "LevelCompleteRewardPlateDrawable()" in view
assert 'status("COINS",data.collectedText,' in view
assert 'status("STARS",data.rating,' in view
assert 'BopaviUiIconDrawable.Kind.CROWN_COIN' in view
assert 'BopaviUiIconDrawable.Kind.STAR' in view
assert 'Score: ${data.scoreText}' in view
assert 'status("SCORE",data.scoreText,' not in view
assert 'R.drawable.reference_screenshot' not in view
assert "stage.place(LevelCompleteActionWings(host),113f,1234f,714f,151f)" in view
assert "ResultActionEnamelDrawable(gold)" in view
assert "replayAndNonMilestoneDoNotPretendToGrantChestLoot" in unit
assert "endOfWorldDoesNotProduceInvalidNextLevel" in unit
assert "val electric" not in main
assert "android.permission.INTERNET" not in (root/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: real first-clear Level Complete, no fake chest grants, native share, accessible actions")
