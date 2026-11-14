# BOPAVI v1.0.88 — evidence-based audit and acceptance (PR #236)

**Status: DRAFT, UNMERGED, UNRELEASED.**
Baseline `main` `ad0cac26b480e57fe318e6996813a02752101d25`,
stable tag `v1.0.87` / build 217. Candidate build 218 retains
`com.brendigo.bopavi`, existing offline wallet, skins, stored v1–v6
backup compatibility, all prior tags and existing core gameplay engine.

## Verified source findings and candidate fixes

| Priority | File | Repro / cause | Change made | Evidence and open validation |
| --- | --- | --- | --- | --- |
| **P1** data durability | `SettingsCloudBackup.kt` | Android SAF `openOutputStream(uri, "wt")` may truncate the previous backup before new bytes are durably accepted | `SafBackupJournal.kt` snapshots previous document before a destructive open, verifies replacement/rollback and journals pending recovery; requires READ+WRITE | `SafBackupJournalTest.kt`: fake-provider interruption and restart scenarios; **physical provider test still missing** |
| **P1** conflicting edits | `SafBackupJournal.kt` | A delayed recovery might overwrite an unrelated external edit on the same URI | Journal now stores *both* intended and original bytes, validates SHA-256, and refuses overwrite when provider contents match neither an intact version nor a recognized prefix | Source tests: manual external edit, incorrect destination, failed rollback; SAF has no general compare-and-swap, so a race remains |
| **P2** URI lifecycle | `SettingsCloudBackup.kt` | Revoking old URI access during in-flight rollback could make the prior document unrecoverable | Release grants only through serialized backup worker after resolving pending journal | JVM gate and source contracts; require repeated opt-out/restart physical tests |
| **P1** licensing consistency | `LICENSE`, F-Droid metadata, `docs/play/index.html`, `docs/play/TERMS_OF_USE.md`, bundled/public legal HTML | Proprietary Terms had prohibited copying/modifying MIT-licensed code, contradicting repo `LICENSE` | Candidate Terms acknowledge MIT freedoms and separate identity/unknown resource licensing; Android/public HTML kept in sync | `tests/test_full_legal_docs.py`, `tests/test_in_app_privacy.py`; **legal approval and per-asset rights remain open** |
| **P1** missing exact screenshots | `art/reference-current/` | Original 50 image hashes indexed but only 40 owner WebPs committed; genuine native 50-state comparison not proven | A real API36 45-state capture map feeds fail-closed RGB status tool; single-pixel mismatch fails release gate | `tools/report_current_reference_status.py`, native PNGs and per-image/third-region metrics; **10 originals and Game Over B capture missing** |
| **P2** purchases | `tools/qa_android_shop_focus.py` | Previous test checked SKU visibility but not all six actual buy callbacks | Native emulator test now presses each purchasable card and checks exact wallet debit or unchanged balance, inventory update, process continuity | API36 Actions run; **await running exact commit** |
| **P2** dependency/dead-code evidence | `android/app/build.gradle.kts`, `tools/audit_android_dependency_report.py`, `tools/audit_android_asset_inventory.py`, `.github/workflows/native-ci.yml` | Source-only scanner cannot prove R8 reachability, transitive licenses or per-asset origins | CI now collects resolved release runtime dependency graph, R8 mapping, lint and hash inventory of audio/art | Partial evidence, **not complete SBOM, CVE analysis, dead-code reachability or F-Droid acceptance** |
| **P2** performance evidence | `tools/qa_android_shop_focus.py` | Emulator smoke did not preserve frame pacing/memory diagnostic reports | Preserve API36 `dumpsys gfxinfo framestats` and `dumpsys meminfo` from actual process | Emulator-only data. Real-device FPS, p95/p99 frame timing, battery, memory, touch latency and ANR testing remain open |

## Acceptance order and requirements

**Shop → Skins → Settings → Home → Gameplay start → active flight → Pause
→ Game Over A → Game Over B → Privacy.** For each, establish a genuine
state and same-native-resolution capture, compare full-frame RGB with the
original owner WebP and record `different_pixels`, percentage and MAE.
The supplementary top/middle/bottom bands do not mask pixels or replace
the full comparison. *Only 0 differing RGB pixels counts as exact 1:1 for
one concrete snapshot.* A responsive or functional PASS is not an exact
RGB PASS.

**Current matrix:** indexed 50/50; original binary presence 40/50;
reference visual identity 0 of 50 certified; Game Over B has no separate
authentic native capture. Current branch measurements must come from its
own exact SHA run and matching artifact. Do not recycle historical
owner-941 comparator values as current-v1.0.88 measurements.

**Shop-specific:** source preserves 3×2 native six-product layout and real
local purchase callbacks. Prior published v1.0.87 notes document a sampled
Shop RGB MAE of 72.62/255 in a *historical* comparison, **not this commit**.
Do not make unmeasured art-position changes based on that number alone.
Fix the largest measured regions once the latest native captures and
source originals are jointly available.

## Functional, security and accessibility acceptance still required

- API 26, 30, 33, 35 and 36 with rotation, font scaling, TalkBack,
  minimum 48dp effective touch surfaces and actual click responses;
  Android 15/16 emulator smoke alone is insufficient.
- Physical SAF test protocol: `docs/qa/SAF-REAL-PROVIDER-ACCEPTANCE.md`.
- All legacy JSON v1–v6 migrations, coins, selected/owned skins, perk
  inventories and historic migrations need fixture + real-device confirmation.
- Root MIT license is not evidence that every icon, texture, WAV and
  reference-derived asset is legal for F-Droid; keep separate explicit
  per-file review and external dependency/CVE verification.
- R8 output shows minified/reachable mapping metadata, not proof of every
  unused Kotlin/resource path. Deletion requires source+runtime reachability
  and save migration impact review.
- Game-launch conflict remains: production Home contains exactly Shop,
  Skins and Settings. Adding a fourth visible button or changing the art
  is not authorized. An existing-Bopi interaction remains an **unapproved
  proposal**, not a changed behavior.

## Release rules

CI source, graphics and build status are **independent** of 50-state visual
acceptance, physical device provider tests and owner/legal approval. Never
merge/publish just because APK/AAB compiles. Existing release v1.0.87 is
preserved; any produced AAB and release APK are **unsigned** and require
authorization from the publisher's private signing key outside GitHub.
