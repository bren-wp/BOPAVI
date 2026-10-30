#!/usr/bin/env python3
"""Native Pause geometry, unique modal, real finite/endless replay (source gate)."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
app=(src/"MainActivity.kt").read_text()
target=(src/"PauseReplayTarget.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/PauseReplayTargetTest.kt").read_text()
pause=app.split("private fun showPremiumPause(",1)[1].split("private fun startGame(){",1)[0]
back=app.split("private fun navigateBack()",1)[1]
for marker in (
    'pauseDialog?.isShowing==true',
    'setCanceledOnTouchOutside(false)',
    'dialog.setOnDismissListener{',
    'if(pauseDialog===dialog)pauseDialog=null',
    'pauseDialog=dialog',
    'dialog.window?.setGravity(Gravity.CENTER)',
    '(resources.displayMetrics.widthPixels*.80f).toInt()',
    'if(gameView===game && selectedScreen=="game" && !game.game.finished)',
    'setImageResource(portraitDrawableIds[selectedPilotChoice])',
    'command("▶  NASTAVI",0)',
    'command("↻  PONOVO",1)',
    'command("←  IZLAZ",2)',
    'PauseReplayTarget.fromStage(activeStage)',
    'PauseReplayTarget.Endless -> startGame()',
    'is PauseReplayTarget.Finite -> startLevel(target.world,target.level)',
): assert marker in pause,marker
assert 'if(pauseDialog?.isShowing!=true)showPremiumPause(view)' in back
assert 'command("↻  PONOVO",1){startGame()}' not in pause
assert 'fun fromStage(stage:Pair<Int,Long>?)' in target
assert 'finiteReplayPreservesSelectedWorldAndStage' in unit
assert 'endlessReplayDoesNotInventLevelSelection' in unit
print("PASS: styled Pause, single modal, correct retry, native buttons")
