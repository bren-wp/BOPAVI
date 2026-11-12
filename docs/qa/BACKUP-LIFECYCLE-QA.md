# BOPAVI — Backup lifecycle QA, v1.0.83 candidate

**Scope:** SAF backup destination/lifecycle and consent; no UI/pixel or gameplay changes.

| Scenario | Expected result | Evidence |
| --- | --- | --- |
| Queued work after Activity destruction | No new write starts and no user-facing callback runs | BackupTaskGateTest and static source guard; device test pending |
| Change SAF destination | Existing queued save for old URI invalidated | Revision token test; provider test pending |
| Disable Cloud Save | URI preference removal committed before grant release | Source contract; fault injection pending |
| Selecting a new document fails to persist | New grant is released, old destination remains configured | Source contract; provider test pending |
| Repeated opt-in revisions | Only latest revision accepted | Kotlin/JVM unit test |
| Provider rejects / interrupts write | Error reported when Activity remains active; original document not guaranteed intact | Existing handling, destructive-write recovery OPEN |
| Android 15/16 normal gameplay, screen UI | No visual change intended | Full CI emulator runs required |

Not validated by this document: real-device FPS/battery, absolute 0% RGB difference, SAF atomicity, physical-device restore test.
