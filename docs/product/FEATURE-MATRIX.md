# BOPAVI — FEATURE MATRIX (release v0.2.23, build 128)

Evidence snapshot: main commit fa3e2ede9345759616f31f30a057f11c47f7fa03, checked 2026-10-18. This describes **reachable UX**, not files merely present in the repository. Android: MainActivity.kt; iOS: GameController.swift. Changes require a fresh code/navigation audit and native test.

## Machine-readable source of truth

The JSON block is consumed by tests/test_store_claims.py; valid states are visible, internal-legacy, planned and removed.

~~~json
{
  "schema": 1,
  "baseline_sha": "fa3e2ede9345759616f31f30a057f11c47f7fa03",
  "release": "v0.2.23",
  "build": 128,
  "scope_decision": "pending",
  "screens": ["home", "flight", "pause", "results"],
  "features": [
    {"id": "home", "status": "visible"},
    {"id": "flight", "status": "visible"},
    {"id": "tap_to_flap", "status": "visible"},
    {"id": "procedural_obstacles", "status": "visible"},
    {"id": "pause", "status": "visible"},
    {"id": "results", "status": "visible"},
    {"id": "score", "status": "visible"},
    {"id": "berries", "status": "visible"},
    {"id": "local_best", "status": "visible"},
    {"id": "retry", "status": "visible"},
    {"id": "offline", "status": "visible"},
    {"id": "no_ads", "status": "visible"},
    {"id": "no_account", "status": "visible"},
    {"id": "legacy_save_models", "status": "internal-legacy"},
    {"id": "skins_assets_nine", "status": "internal-legacy"},
    {"id": "world_assets_eight", "status": "internal-legacy"},
    {"id": "json_save_serializer", "status": "internal-legacy"},
    {"id": "virtual_wallet_save", "status": "internal-legacy"},
    {"id": "multiworld_premium_product", "status": "planned"},
    {"id": "skin_selection_screen", "status": "removed"},
    {"id": "world_selection_screen", "status": "removed"},
    {"id": "level_selection_screen", "status": "removed"},
    {"id": "shop_screen", "status": "removed"},
    {"id": "profile_screen", "status": "removed"},
    {"id": "settings_screen", "status": "removed"},
    {"id": "achievements_screen", "status": "removed"},
    {"id": "records_screen", "status": "removed"},
    {"id": "import_export_ui", "status": "removed"},
    {"id": "powerup_shop", "status": "removed"}
  ]
}
~~~

## Evidence: executable flow and code references

| Visible feature | Evidence on Android | Evidence on iOS |
|---|---|---|
| Home with one IGRAJ action | android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt: showHome, startGame | ios/BOPAVI/GameController.swift: showHome, startGame |
| Endless flight, first Bopi, first biome | MainActivity.kt: GameSimulation(LevelEngine.createStream(0,1L)...), GameView | GameController.swift: startGame, GameSimulation, GameCanvas |
| Pause / continue / retry / exit | MainActivity.kt: showPremiumPause | GameController.swift: showPremiumPause |
| Results / retry / home | MainActivity.kt: showResult, recordRun | GameController.swift: showResult, recordRun |
| Real collected red berries and food best | GameSimulation.kt, GameView.kt, ProgressStore.kt | BopaviCore.swift, GameCanvas.swift, ProgressStore.swift |
| No network permission / ads / account | AndroidManifest.xml (no INTERNET), build.gradle.kts (JUnit only), native app entry | GameController.swift, BopaviApp.swift, no runtime online account flow |

| Internal/absent claim | Evidence | Public claim permitted? |
|---|---|---|
| Eight worlds and nine skin asset sets | tools/generate_images.py and Android/iOS art bundles; runtime selects only index 0 | No |
| Legacy JSON import/export and currency fields | ProgressStore.kt / ProgressStore.swift save/migration code, not wired to navigation | No |
| Shop, levels, worlds, skins, settings, achievements | No showShop/showWorlds/showLevels/showSettings navigation in either native controller; tests/test_native_files.py | No |
| Automatic store approval | GitHub CI/release is independent of Google Play review and signing | No |

## Claim → evidence → decision (2026-10-18)

| Existing assertion | Existing location | Contradicting evidence | Action |
|---|---|---|---|
| Choose nine characters | Previous HR store listing / old Play instructions | Home displays BopiPortrait0 / portraitDrawableIds[0]; no character picker | Removed from active description |
| Explore eight playable worlds | Previous HR store listing | First world is hard-coded in the only startGame navigation | Removed from active description |
| Spend coins, use shield/magnet | Previous HR store listing / Terms | Endless run does not expose economy/shop or power-up UI | Removed from active description |
| Manual JSON import/export in Settings | Old Play guide, privacy text | No Settings or import/export UI route | Explicitly marked legacy/non-accessible; no marketing claim |
| Physical QA of eight worlds & nine skins | Old release checklist | Only one playable Bopi/biome | Replaced with reachable-screen verification |
| Version 0.1.x and build 36/83 | Old publishing guide/checklist | Current Android versionName 0.2.21, versionCode 128; latest published tag v0.2.23 | Updated to verified baseline |

## Confidence and gates

Static code navigation, manifests, changelog, current release assets and last green native CI are evidence for this snapshot. CI screenshots from v0.2.23 exist for Home, Flight Preview, Flight Active and Results; **no dedicated Pause screenshot is listed in the release assets**, so pause needs a separately captured same-build image before advertising a pause image. Static evidence plus emulator/simulator CI is not proof of flawless behavior on all physical devices. Review ADR-0001 before any restoration of removed UI.
