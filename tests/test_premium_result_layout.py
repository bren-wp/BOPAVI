#!/usr/bin/env python3
"""Presentation: avoid long empty modal/result regions on tall Android screens."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
main=(r/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
pause=main.split("private fun showPremiumPause(",1)[1].split("private fun startGame(){",1)[0]
result=main.split("private fun showResult(",1)[1].split("override fun onBackPressed()",1)[0]
assert "orientation=LinearLayout.VERTICAL\n            gravity=Gravity.CENTER" in pause
assert "minOf((resources.displayMetrics.heightPixels-d(48)).coerceAtLeast(1)," in pause
# Height is now centralized to preserve safe, testable compact geometry.
layout=(r/"android/app/src/main/java/com/brendigo/bopavi/PauseModalLayout.kt").read_text()
assert "d(PauseModalLayout.dialogHeightDp(compactPause))" in pause
assert "fun dialogHeightDp(compact:Boolean):Int=if(compact)480 else 590" in layout
assert "fun buttonHeightDp(compact:Boolean):Int=if(compact)55 else 63" in layout
assert "fun compact(screenHeightDp:Int,fontScale:Float):Boolean" in layout
assert "(resources.displayMetrics.widthPixels*.80f).toInt()" in pause
assert "pauseDialog?.isShowing==true" in pause
assert "setCanceledOnTouchOutside(false)" in pause
assert "orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER" in result
assert "FitViewportColumn(this)" in pause and "FitViewportColumn(this)" in result
assert "setImageResource(portraitDrawableIds[selectedPilotChoice])" in pause
assert "applySafeViewportInsets(view,insets)" in result
assert "android.view.WindowInsets.Type.systemBars()" in main
print("PASS: vertically centered results, bounded pause dialog and viewport fitting")
