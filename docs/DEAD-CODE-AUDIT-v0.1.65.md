# BOPAVI — Full dead-code and production artifact audit (v0.1.65, build 68)

## Scope: actual current repository

Audited **12 platform source files** (6 Kotlin Android, 6 Swift iOS), **282 private declarations**, **50 explicit Kotlin imports**, compiled-resource references, **eight environments**, **nine pilots with separate body/left/right-wing rendering**, cross-platform audio, iOS project integration, Android permissions, and the complete Android 15/16 screenshot-test pathway. The exact implementation is `tools/audit_native_dead_code.py`; it runs in required GitHub Actions source-audit jobs.

The audit is intentionally conservative: textual references alone do **not prove runtime reachability**; Objective-C selectors, Android lifecycle callbacks, accessibility handlers, persisted keys, backup migration branches, dynamic UIImage names and generated bitmap slots are intentionally preserved.

## Confirmed and corrected findings

1. **Unreachable generated composite Hero PNG**: `android/app/src/main/res/drawable-nodpi/hero.png` and `ios/BOPAVI/Assets.xcassets/Hero.imageset/hero.png` were generated and packaged, but neither platform referenced this drawable/image set. Removed both generator outputs, workflow artifact uploads and the stale iOS Hero asset catalog. **Kept** `docs/assets/hero.svg` because it is the actual source for dynamically generated Bopi sprites and README artwork.
2. **Android 15 failure was not proven a BOPAVI navigation defect**. The failed `main` CI run `38312182026` produced `android-pilot-launch-diagnostic.xml` with a seven-node **Pixel Launcher** UI, not GameView, after tapping the actual pilot confirmation. The earlier picker had correctly rendered all nine pilots. Improved QA now captures Android crash logs, activity and focused-window diagnostics; it permits at most two **verified recovery attempts only when the live BOPAVI process has been obscured by focused Pixel Launcher**. Missing app process, any BOPAVI fatal exception, unknown system screen, wrong screen, or missing actual GameView still **fails**. Existing game/level content is never synthesized or bypassed.
3. **Consumer-facing residual dev/privacy noise**: simplified the pilot-pick description on Android and iOS. Actual offline storage, restore/export mechanisms, copyright and privacy disclosures are retained.
4. **Audit coverage gap**: original script flagged only single-use private identifiers and imports. Expanded to assert runtime drawable inventory, nine pilot/wing slots, orphan generated Hero rejection, full Android/iOS byte-equivalent audio pairing, Xcode target membership and Android offline/privacy flags. Verifies 14 paired WAVs; refuses packaging regressions.

## Source review by subsystem

| Subsystem | Platform files | Result and retained paths |
|---|---|---|
| Simulation, collisions, endless levels | GameSimulation.kt, LevelEngine.kt, BopaviCore.swift | Keep physics/score and procedural-stream paths; Kotlin JUnit/Swift parity exercise them |
| GPU Canvas, bird wings, HUD, particles | GameView.kt, GameCanvas.swift | Keep nine dynamically indexed sprites and 18 wings, scene callbacks, diagnostics, accessibility and actual game HUD |
| Interactive screens | MainActivity.kt, GameController.swift | Keep navigation, pilot selection, profiles, private records, pause, results and Settings; emulator/simulator validate real clicks |
| Local persistence | ProgressStore.kt, ProgressStore.swift | Keep save v1–v6 migration, personal leaderboard, achievements, score-coins anti-duplication and import validation |
| Music and feedback | Soundscape.kt, Soundscape.swift | Keep 14 referenced WAVs per platform; byte parity and game haptic callbacks |
| Launch and signing | BopaviApp.swift, AndroidManifest.xml, Info.plist, project.pbxproj | Keep signed-store separation, real platform entry points and privacy flags |

## 1:1 visual truth and release gate

The supplied illustrations are reference targets, **not actual screenshots**. The flat vector/cartoon presentation of the currently generated backgrounds and characters is still visibly different from the painterly, depth-rich 3D fantasy screenshots. Do not claim a 1:1 result. Keep authentic emulator/iOS simulator PNG evidence and compare screen-by-screen at identical viewport sizes. Maintain functional real labels, hit targets, safe-area layouts and performance rather than embedding reference screenshots as fake interactive screens.

**Acceptance:** require green static resource/dead-code audit, test_native_files, Kotlin unit/lint + APK/AAB, Android 15/16 visual smoke, Swift save/physics parity + simulator and device build, real screenshot artifact checks, GitHub release assets and SHA256SUMS. No merge or release on failures.

**Limitations:** regex reachability does not prove whole-program liveness, no memory leak, or pixel-perfect 1:1 UI. True deep dead-code proof also requires compiler reachability, device memory/profiler runs, screenshot overlay comparisons and accessibility QA on real phones.
