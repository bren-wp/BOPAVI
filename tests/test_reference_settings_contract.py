#!/usr/bin/env python3
"""Full-screen reference Settings: real persisted controls, opt-in SAF backup, local reminders."""
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
    'SettingsEmbossedTitle(host)','SettingsReferenceLayout.rows[index]',
    'VolumeGesturePolicy.commitAsProgressChanges(fromUser,tracking)',
    'bar?.progress?.let(::save)','setOnCheckedChangeListener',
    'stage.setSafe(','android.widget.ScrollView',
    'contentDescription=name','setOnClickListener{click()}',
    'button("HOME",click=onBack)','minimumHeight=dp(48)',
):
    # ScrollView is imported rather than referenced by fully qualified name.
    assert (marker in settings if marker!="android.widget.ScrollView"
            else "import android.widget.ScrollView" in settings),marker
assert 'val rows=floatArrayOf(610f,706f,802f,898f,994f,1090f,1186f,1282f,1378f)' in geometry
assert 'const val homeTop=1486f' in geometry
assert 'stage.useShopFullBleedReferenceCamera()' in settings
assert 'SettingsReferenceLayout.supportsNativeTouchTargets(' in settings
assert 'col.addView(button("‹  HOME",click=onBack).apply {' in settings
assert 'contentDescription="HOME"' in settings
assert 'fun supportsNativeTouchTargets(screenW:Int,screenH:Int,density:Float)' in geometry
assert 'selectedScreen="settings"\n        applyPremiumSystemBars()' in main
# Density and window-size updates must recompose the actual nine-row screen
# instead of retaining the old ScrollView and its stale scrolled position.
assert 'private var settingsReflowPending=false' in main
assert 'override fun onConfigurationChanged(newConfig:android.content.res.Configuration)' in main
assert 'if(selectedScreen=="settings" && ::progress.isInitialized &&' in main
assert 'if(!isFinishing && !isDestroyed && selectedScreen=="settings")' in main
assert 'showSettingsScreen()' in main.split('override fun onConfigurationChanged(',1)[1].split('private fun showNativeView(',1)[0]
assert 'android-settings-resize-failed-ui.xml' in qa
assert 'Settings was not recomposed after live density/size change' in qa
assert 'Settings retained live native title after 941px/320dpi reflow' in qa
for key in ('"sound_enabled"','"music_volume"','"effects_volume"',
            '"haptic_enabled"','"less_motion"','"difficulty"'):
    assert key in store,key
assert 'node_with("Music",clickable=False)' in qa
assert 'node_with("Glazba:",clickable=False)' in qa
assert 'node_with("Pročitaj politiku privatnosti")' in qa
assert 'node_with("Pročitaj uvjete korištenja")' in qa
assert 'SettingsLegalArrow(host,"Pročitaj politiku privatnosti",onPrivacy)' in settings
assert 'SettingsLegalArrow(host,"Pročitaj uvjete korištenja",onTerms)' in settings
assert '"Selected " else "Choose "' in settings
assert 'capture_png("android-settings-reference-size")' in qa
assert 'line("Cloud Save",BopaviUiIconDrawable.Kind.CLOUD,' in settings
for kind in ("BELL","GLOBE","GAMEPAD","CLOUD"):
    assert "Kind."+kind in (src/"BopaviUiIconDrawable.kt").read_text()
assert "Finish.RED" in (src/"Bopavi3DTheme.kt").read_text()
assert 'button("Restore Purchases",click=action)' in settings
assert 'restoreButton(onRestore)' in settings
assert "BopaviUiIconDrawable.Kind.HEADSET" in settings
assert 'val rows=if(reference) listOf(' in settings
assert 'legacyRows[0],legacyRows[1],addedRows[0],addedRows[1],' in settings
assert 'addedRows[2],legacyRows[3],addedRows[3],legacyRows[6],legacyRows[7]' in settings
assert 'setPadding(dp(12),0,dp(9),0)' in settings
assert 'Bopavi3DTheme.panel(when(kind){' in settings
assert 'BopaviUiIconDrawable.Kind.MUSIC->Bopavi3DTheme.Finish.VIOLET' in settings
assert 'FrameLayout.LayoutParams(dp(27),dp(27),Gravity.CENTER)' in settings
assert 'setSingleLine(true)' in settings
assert 'ellipsize=android.text.TextUtils.TruncateAt.END' in settings
# Row labels and difficulty values must never rely on clipped visible text.
assert 'row.addView(title(text,if(text=="Mode")12 else 14,navy).apply{' in settings
# A 13..12sp TextView autosize range crashed both emulator Settings flows.
# The pure policy and JVM test ensure 12sp controls and fallback footer are safe.
sizing=(src/"SettingsTextSizePolicy.kt").read_text()
sizing_test=(root/"android/app/src/test/java/com/brendigo/bopavi/SettingsTextSizePolicyTest.kt").read_text()
assert 'SettingsTextSizePolicy.minSp(size)' in settings
assert 'setAutoSizeTextTypeUniformWithConfiguration(minAutoSp,size,1,' in settings
assert 'if(maxSp>13) 13 else (maxSp-2).coerceAtLeast(1)' in sizing
assert 'for(maximum in 2..100)' in sizing_test
assert 'SettingsTextSizePolicy.minSp(12)' in sizing_test
assert 'setAutoSizeTextTypeUniformWithConfiguration(13,size,1,' not in settings
assert 'setAutoSizeTextTypeUniformWithConfiguration(10,14,1,' in settings
assert 'val shortLabel=when(index)' in settings
for label in ('"EASY"','"NORMAL"','"HARD"'):
    assert label in settings,label
assert 'contentDescription=(if(index==progress.difficulty())' in settings
assert 'line("Mode",BopaviUiIconDrawable.Kind.STAR,' in settings
assert 'stage.place(SettingsEmbossedTitle(host).apply {' in settings
assert 'title("SETTINGS",31).apply{' in settings
assert 'background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.GOLD)' in settings
assert 'setTextColor(0xff61330a.toInt())' in settings
# Large-font reference canvases shrink native switches below the required
# 64dp accessibility height; those devices must use a scrollable layout.
assert 'host.resources.configuration.fontScale<1.25f' in settings
assert 'isFillViewport=true' in settings
# Three independent difficulty options need at least 48dp width each.
# The group allocates 180dp; two 2dp margins per button leave 56dp.
# Do not silently regress this to the old 140dp selector.
assert 'controlWidthDp:Int=96' in settings
assert 'LinearLayout.LayoutParams(dp(controlWidthDp),-1)' in settings
assert 'difficulty(onDifficulty),controlWidthDp=162' in settings
assert 'minWidth=0' in settings and 'minimumWidth=0' in settings
assert 'SettingsLegalArrow(host,"Pročitaj politiku privatnosti",onPrivacy)' in settings
assert 'SettingsLegalArrow(host,"Pročitaj uvjete korištenja",onTerms)' in settings
assert 'controlWidthDp=52' in settings
assert (162-3*4)//3>=48
# Exact-reference artwork is decorative only; native widgets own all state.
decor=(src/"SettingsReferenceDecor.kt").read_text()
assert "class SettingsWoodPanelDrawable : Drawable()" in decor
assert "class SettingsGoldTitleDrawable : Drawable()" in decor
assert "no text," in decor and "hit targets" in decor
assert "background=SettingsWoodPanelDrawable()" in settings
# The 3D heading and both legal arrows remain accessible Android Views,
# not flattened screenshots or placeholder controls.
lettering=(src/"SettingsReferenceLettering.kt").read_text()
assert 'internal class SettingsEmbossedTitle(context:Context):TextView(context)' in lettering
assert 'internal class SettingsLegalArrow(context:Context,label:String,onTap:()->Unit):Button(context)' in lettering
assert 'contentDescription=label' in lettering
assert 'setOnClickListener { onTap() }' in lettering
assert 'minimumWidth=0' in lettering and 'minimumHeight=0' in lettering
assert 'background=null' in lettering
assert '151f,1244f,638f,4f' in settings
assert "background=SettingsGoldTitleDrawable()" in (src/"SettingsReferenceLettering.kt").read_text()
assert "trackDrawable=StateListDrawable().apply" in settings
assert "android.R.attr.state_checked" in settings
assert "thumbDrawable=GradientDrawable(" in settings
assert "progressDrawable=LayerDrawable(arrayOf(trackBack," in settings
assert "ClipDrawable(trackFill,Gravity.START,ClipDrawable.HORIZONTAL)" in settings
assert "bar?.progress?.let(::save)" in settings
# Settings footer follows the installed build (not a fictional v1.0.0).
assert 'private fun installedVersion():String' in settings
assert 'host.packageManager.getPackageInfo(host.packageName,0).versionName' in settings
assert 'stage.place(title("v"+installedVersion(),13,' in settings
assert 'col.addView(title("v"+installedVersion(),12,' in settings
# Consent-based notifications and user-selected document backup, not fake buttons.
backup=(src/"SettingsCloudBackup.kt").read_text()
reminder=(src/"SettingsReminder.kt").read_text()
manifest=(root/"android/app/src/main/AndroidManifest.xml").read_text()
assert 'Intent.ACTION_CREATE_DOCUMENT' in main
assert 'Intent.ACTION_OPEN_DOCUMENT' in main
assert 'cloudBackup.activate(uri,data?.flags ?: 0,::reportBackup)' in main
assert 'cloudBackup.restoreFrom(uri,::reportBackup)' in main
assert 'SettingsReminder.configure(this,enabled)' in main
assert 'onCloud={enabled->' in main
assert 'onRestore={chooseBackupToRestore()}' in main
assert 'onLanguage={changeLanguage()}' in main
assert 'onControls={controlPreferences()}' in main
assert 'onNotifications={value->changeNotifications(value)}' in main
assert 'fun notificationsEnabled():Boolean' in store
assert 'fun uiLanguage():String' in store
assert 'takePersistableUriPermission' in backup
assert 'val json=progress.exportJson()' in backup
assert 'progress.importJson(backup)' in backup
assert 'SettingsReminderReceiver:BroadcastReceiver()' in reminder
assert 'AlarmManager.setInexactRepeating' not in reminder # uses manager.setInexactRepeating
assert 'manager.setInexactRepeating(AlarmManager.RTC_WAKEUP' in reminder
assert 'POST_NOTIFICATIONS' in manifest
assert "http://" not in settings and "https://" not in settings
print("PASS: native Settings reference art, real persisted sliders/toggles, mobile scroll and legal routes")
