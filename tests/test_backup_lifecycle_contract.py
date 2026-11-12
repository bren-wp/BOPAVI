#!/usr/bin/env python3
"""Backup lifecycle source invariants; emulator/provider fault tests still required."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
backup=(src/"SettingsCloudBackup.kt").read_text()
gate=(src/"BackupTaskGate.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/BackupTaskGateTest.kt").read_text()

for token in ("AtomicBoolean(false)","AtomicLong(0L)",
              "fun canRun(expectedRevision:Long):Boolean",
              "!closed.get() && revision.get()==expectedRevision",
              "closed.set(true)","revision.incrementAndGet()"):
    assert token in gate,token
for token in ("private val gate=BackupTaskGate()",
              "catch(_:RejectedExecutionException)",
              "executor.execute { if(!gate.isClosed())work() }",
              "if(!gate.isClosed() && !activity.isFinishing && !activity.isDestroyed)",
              "gate.close()","executor.shutdown()",
              'if(!uriPrefs.edit().remove("document_uri").commit())return',
              "if(previous!=null && previous!=uri)releaseGrant(previous)",
              "if(!gate.canRun(revision) || destination()!=uri)return@saveNow",
              "check(gate.canRun(revision) && destination()==uri)",
              'openOutputStream(uri,"wt")',
              "if(pending.get() && started && enabled() && !gate.isClosed())",
              "check(!gate.isClosed())",
              "progress.importJson(backup)",
              "activity.runOnUiThread"):
    assert token in backup,token
for token in ("changingDocumentInvalidatesQueuedWork",
              "stoppingActivityInvalidatesAllQueuedWork",
              "revisionsRemainMonotonicAcrossRapidOptInChanges"):
    assert token in unit,token
assert "android.permission.INTERNET" not in (
    root/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: backup generation, closure, stale URI and callback guards")
