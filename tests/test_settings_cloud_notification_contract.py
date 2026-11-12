#!/usr/bin/env python3
"""Security, consent and data-flow contracts for optional BOPAVI Settings services."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
backup=(src/"SettingsCloudBackup.kt").read_text()
reminder=(src/"SettingsReminder.kt").read_text()
store=(src/"ProgressStore.kt").read_text()
settings=(src/"SettingsScreen.kt").read_text()
activity=(src/"MainActivity.kt").read_text()
manifest=(root/"android/app/src/main/AndroidManifest.xml").read_text()
privacy=(root/"android/app/src/main/assets/legal/privacy.html").read_text()

# Cloud access is opt-in and scoped to one user-chosen Android document.
for marker in (
    "Intent.ACTION_CREATE_DOCUMENT","Intent.ACTION_OPEN_DOCUMENT",
    "Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION",
    "takePersistableUriPermission",
    "cloudBackup.activate(uri,data?.flags ?: 0,::reportBackup)",
    "cloudBackup.restoreFrom(uri,::reportBackup)",
    "cloudBackup.deactivate()",
):
    assert marker in activity or marker in backup,marker
for marker in (
    'openOutputStream(uri,"wt")',
    "openInputStream(uri)",
    "require(content.length<=550_000)",
    "progress.importJson(backup)",
    "val json=progress.exportJson()",
    'remove("document_uri")',
):
    assert marker in backup,marker
assert 'getSharedPreferences("bopavi_cloud_destination_v1"' in backup
assert "contentResolver.takePersistableUriPermission" in backup
assert "if(flags and android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION==0)" in backup
assert "takePersistableUriPermission(uri,flags)" in backup
assert "return if(result.isSuccess)" not in backup  # completion is reported only after writing
assert "activity.runOnUiThread" in backup
assert "activity.contentResolver.persistedUriPermissions.firstOrNull" in backup
assert "releasePersistableUriPermission(" in backup
assert "if(!ok)deactivate()" in backup
assert "if(!gate.canRun(revision) || destination()!=uri)return@saveNow" in backup
assert 'if(!uriPrefs.edit().remove("document_uri").commit())return' in backup
assert "if(previous!=null && previous!=uri)releaseGrant(previous)" in backup
assert "executor.shutdown()" in backup
assert "if(!enabled || !allowed(context))return" in reminder
assert 'prefs.getBoolean("notifications_enabled",false)' in store
assert "SettingsReminder.configure(this,granted)" in activity
assert "requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS)" in activity
assert "AlarmManager.INTERVAL_DAY" in reminder
assert "Intent.ACTION_BOOT_COMPLETED" in reminder
assert "android.permission.POST_NOTIFICATIONS" in manifest
assert "android.permission.RECEIVE_BOOT_COMPLETED" in manifest
assert "android.permission.INTERNET" not in manifest
assert "com.brendigo.bopavi.DAILY_REMINDER" in reminder
assert "onCloud={enabled->" in activity
assert "onRestore={chooseBackupToRestore()}" in activity
assert "onNotifications={value->changeNotifications(value)}" in activity
assert "button(\"Restore Purchases\",click=action)" in settings
assert "restoreButton(onRestore)" in settings
assert "BopaviUiIconDrawable.Kind.HEADSET" in settings

# Backup must never restore permissions or a document URI just because the
# user imported a third-party JSON file. Language may safely survive import.
assert 's.put("uiLanguage",uiLanguage())' in store
assert 'val importedLanguage=s.optString("uiLanguage","en")' in store
assert 'e.putString("ui_language",importedLanguage)' in store
assert 's.put("notificationsEnabled"' not in store
assert 's.put("document_uri"' not in store
assert 'e.putBoolean("notifications_enabled"' not in store
assert 'e.putString("document_uri"' not in store
assert "pružatelja pohrane" in privacy
assert "dnevne lokalne obavijesti" in privacy
print("PASS: opt-in local reminders, scoped SAF provider, validated backup and consent isolation")
