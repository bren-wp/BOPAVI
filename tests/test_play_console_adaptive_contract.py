#!/usr/bin/env python3
"""Play Console edge-to-edge/orientation/R8 regression contract for BOPAVI."""
from pathlib import Path

R=Path(__file__).resolve().parents[1]
app=(R/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
game=(R/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
manifest=(R/"android/app/src/main/AndroidManifest.xml").read_text()
theme=(R/"android/app/src/main/res/values/styles.xml").read_text()
build=(R/"android/app/build.gradle.kts").read_text()
root=(R/"android/build.gradle.kts").read_text()
gradle=(R/"android/gradle/wrapper/gradle-wrapper.properties").read_text()
ci=(R/".github/workflows/native-ci.yml").read_text()
qa=(R/"tools/qa_android_emulator.sh").read_text()
rotate=(R/"tools/qa_android_rotation.py").read_text()

for outdated in ("window.statusBarColor", "window.navigationBarColor",
                 "setStatusBarColor(", "setNavigationBarColor("):
    assert outdated not in app, outdated
for old_theme in ('name="android:statusBarColor"', 'name="android:navigationBarColor"'):
    assert old_theme not in theme, old_theme
assert "window.setDecorFitsSystemWindows(false)" in app
assert "decor.post {" in app
assert "if(decor.isAttachedToWindow)" in app
assert "decor.windowInsetsController?.hide(" in app
assert "window.insetsController?.hide(" not in app
assert "View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION" in app
assert "applySafeViewportInsets(view,insets)" in app
assert "Type.displayCutout()" in app
assert "android:screenOrientation" not in manifest
assert 'android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout"' in manifest
assert "override fun onConfigurationChanged(" in app
assert "gameView?.requestLayout()" in app
assert "frameClock.reset()" in game.split("override fun onSizeChanged(",1)[1].split("private fun updateWorldCover",1)[0]
assert "id(\"com.android.application\") version \"9.0.1\"" in root
assert "org.jetbrains.kotlin.android" not in root and "org.jetbrains.kotlin.android" not in build
assert "kotlinOptions" not in build
assert "compilerOptions" in build and "JvmTarget.JVM_17" in build
assert "isShrinkResources = true" in build and "isMinifyEnabled = true" in build
assert "proguard-android-optimize.txt" in build
assert "gradle-9.1.0-bin.zip" in gradle
assert "gradle-version: '9.1.0'" in ci
assert "python tests/test_play_console_adaptive_contract.py" in ci
assert "python3 tools/qa_android_rotation.py" in qa
assert '"user-rotation", "lock"' in rotate
assert "android-home-landscape" in rotate and "android-flight-landscape" in rotate
assert '"bopavi_qa_capture_first_flap", "true"' in rotate
assert "BODOVI:" in rotate and "ŽIVOTI:" in rotate
assert "PAUZIRAJ IGRU" in rotate and "flight_before" in rotate
assert "android-flight-portrait-active" in rotate
assert "versionCode = 164" in build and 'versionName = "1.0.34"' in build
print("PASS: edge-to-edge, adaptive orientation, stable game lifecycle and AGP9 R8 contracts")
