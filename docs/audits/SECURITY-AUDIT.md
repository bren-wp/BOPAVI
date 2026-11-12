# BOPAVI — Security audit (incremental, not complete)
Date: 2026-11-12. Baseline commit: 9d6baf881c3a177524d1effef4e3c7044ba4c01f.

| Priority | Location | Verified issue | Proposed candidate correction / remaining test |
| --- | --- | --- | --- |
| P1 | ProgressStore.kt selectOrBuy | Unsynchronized wallet read-modify-write and apply() permitted racing purchases and acknowledged asynchronous writes | @Synchronized, one editor commit; verify device race and disk failure |
| P1 | ProgressStore.kt advanceCompletedLevel | Frontier check and reward were not synchronized, update used apply() | Serialize check and commit frontier/reward together; restart test pending |
| P2 | ProgressStore.kt consumePerks | Decrement persisted asynchronously while perk was granted | Commit stock before grant; fault test pending |
| P1 | ProgressStore.kt importJson | Restore UI could report success after apply() before durability was known | Single editor commit with checked result after v1–v6 validation; migration/device test pending |
| P1 OPEN | SettingsCloudBackup.kt write | openOutputStream(uri,"wt") can truncate provider document without provider-supported atomic rename or rollback | Stage/rollback and interruption testing needed; no SAF atomicity claimed |
| P2 | .github/workflows/native-ci.yml | Five existing source QA contracts were not executed in primary CI | Register all five with source audit; validate exact new run |

No secrets were retrieved. This is **a scoped source audit**, not a full security certification. Manifest and supply-chain review, dynamic Android tests, proof of signing and physical-device assessment remain open. SharedPreferences commit has limits on storage-failure guarantees; a future transactional storage redesign may be warranted.
