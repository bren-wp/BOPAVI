#!/usr/bin/env python3
"""Full-screen reference Settings: real persisted controls, not fictional IAP/cloud."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
main=(src/"MainActivity.kt").read_text()
settings=(src/"SettingsScreen.kt").read_text()
geometry=(src/"SettingsReferenceLayout.kt").read_text()
store=(src/"ProgressStore.kt").read_text()
qa=(root/"tools/qa_android_reference_menus.py").read_text()
assert 'private fun showSettingsScreen(){' in main
assert 'selectedScreen="settings"' in main
assert 'setOnClickListener{sound.effect("click");showSettingsScreen()}' in main
assert 'onBack={showHome()}' in main
assert 'progress.setMusicVolume(value)' in main
assert 'progress.setEffectsVolume(value)' in main
assert 'progress.setSoundEnabled(value)' in main
assert 'progress.setHapticEnabled(value)' in main
assert 'progress.setLessMotion(value)' in main
assert 'progress.setDifficulty(value)' in main
assert 'onPrivacy={showPrivacyNotice()}' in main
assert 'onTerms={showTermsNotice()}' in main
assert 'selectedScreen=="game"' in main
for marker in (
    'R.drawable.homeback','R.drawable.bopiportrait0','R.drawable.logo',
    'title("SETTINGS",57)','SettingsReferenceLayout.rows[index]',
    'VolumeGesturePolicy.commitAsProgressChanges(fromUser,tracking)',
    'bar?.progress?.let(::save)','setOnCheckedChangeListener',
    'stage.setSafe(','android.widget.ScrollView',
    'contentDescription=name','setOnClickListener{click()}',
    'button("HOME",click=onBack)','minimumHeight=dp(48)',
):
    # ScrollView is imported rather than referenced by fully qualified name.
    assert (marker in settings if marker!="android.widget.ScrollView"
            else "import android.widget.ScrollView" in settings),marker
assert 'val rows=floatArrayOf(620f,720f,820f,920f,1020f,1120f,1220f,1320f)' in geometry
for key in ('"sound_enabled"','"music_volume"','"effects_volume"',
            '"haptic_enabled"','"less_motion"','"difficulty"'):
    assert key in store,key
assert 'node_with("Music",clickable=False)' in qa
assert 'node_with("Glazba:",clickable=False)' in qa
assert 'node_with("Pročitaj politiku privatnosti")' in qa
assert 'node_with("Pročitaj uvjete korištenja")' in qa
assert 'contentDescription="Pročitaj politiku privatnosti"' in settings
assert 'contentDescription="Pročitaj uvjete korištenja"' in settings
assert '"Selected " else "Choose "' in settings
assert 'capture_png("android-settings-reference-size")' in qa
assert 'line("Cloud Save"' not in settings
assert 'line("Restore Purchases"' not in settings
assert 'button("Restore Purchases"' not in settings
assert 'setPadding(dp(8),0,dp(9),0)' in settings
# Large-font reference canvases shrink native switches below the required
# 64dp accessibility height; those devices must use a scrollable layout.
assert 'host.resources.configuration.fontScale<1.25f' in settings
assert 'isFillViewport=true' in settings
# Three independent difficulty options need at least 48dp width each.
# The group allocates 180dp; two 2dp margins per button leave 56dp.
# Do not silently regress this to the old 140dp selector.
assert 'controlWidthDp:Int=140' in settings
assert 'LinearLayout.LayoutParams(dp(controlWidthDp),-1)' in settings
assert 'difficulty(onDifficulty),controlWidthDp=180' in settings
assert 'minWidth=0' in settings and 'minimumWidth=0' in settings
assert (180-3*4)//3>=48
assert "http://" not in settings and "https://" not in settings
print("PASS: native Settings full page, real preferences, small-screen scroll and legal routes")
