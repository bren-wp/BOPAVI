# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.39 — Native reference Loading screen (build 169)

**Published:** 2026-10-30 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.39)

- Add native Canvas-based `FlightLoadingView` for the genuine flight preparation step, using the original locally bundled BOPAVI logo, selected bird and fantasy-island sky.
- Reconstruct the large center hero, decorative collectible coins, golden winged frame, cyan progress track, and accessible native loading label. Fit uniformly on tall/compact devices with a center-cropped backdrop; no external service or fake wallet.
- Only mark the track complete after deterministic level generation succeeds; do not fabricate intermediate percentages, add an arbitrary timer, or display a second OS startup splash.
- Generate levels asynchronously before entering the existing GameView, with generation-token cancellation if Back returns Home, process is destroyed or the Activity loses focus. Consume purchased run perks only after successful loading; existing save keys and flight physics are untouched.
- Add an explicit emulator-only loading preview intent for reliable screenshot and accessibility QA; the real loading view uses the same code.
- Keep Gradle 9.6.0, Android Gradle Plugin 9.5.0, target API 36; verify Android 15/16 device QA, native Lint, tests, dead-code audit, and release checks.

## v1.0.38 — Reference Home redesign (build 168)

**Published:** 2026-10-30 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.38)

- Reorganize Home into an earned-resource strip, existing BOPAVI logo, large selected-pilot hero, gold PLAY, purple SKINS, green SHOP, blue SETTINGS, and three genuine shortcuts for ACHIEVEMENTS, LEADERBOARD and SOUND.
- Remove redundant inline pilot cards and old legal links from Home. Retain character selection in Skins; move full original Privacy Policy and Terms of Service behind accessible native Settings controls.
- Keep the Level Map reachable from Settings, using a real dismiss-and-navigate action rather than a dead button.
- Display only actual locally earned coins and stars; do not present fictional gems, online purchase controls or placeholder achievements.
- Add live local achievements sourced from saved wins, total runs, wing flaps and best obstacles.
- Adapt hero height to available screenHeightDp, retain native 48dp minimum touch targets, safe Android 15/16 window insets, large-font and compact portrait QA.
- Preserve Gradle 9.6.0, Android Gradle Plugin 9.5.0, compile/target API36, physical flight simulation, stronger obstacles, lives-only HUD, offline operation and stored records.
