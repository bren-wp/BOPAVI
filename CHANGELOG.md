# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.37 — Reference-inspired native navigation (build 167)

**Published:** 2026-10-30 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.37)

- New **SKINS**, **SHOP**, and **LEVELS** entry points on Home, preserving existing
- Native, responsive Skins selector using real Bopi/Sova/Lisac portraits and
- Offline Shop using the six existing earned-coin perk items, real costs and
- Real level map for all eight existing generated worlds; locked/unlocked status
- Finite level start, genuine victory/failure result, actual next-level unlock and
- The existing 18-unit collider, challenging pillars, Android 15/16 lifecycle

## v1.0.36 — Canvas transparency and safe return to flight (build 166)

**Published:** 2026-10-29 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.36)

- Fix common Android Canvas `fill(color)` resetting supplied ARGB alpha to 100%. Respect semi-transparent glass, ghosted/empty hearts, pillar materials, bevel highlights, atmospheric layers and effect rings, restoring intended depth while leaving collision bounds unchanged.
- Prevent foreground re-entry into a stuck paused flight with no Pause HUD. On background, freeze an active run; when its Activity safely regains focus, present the existing native Continue/Retry/Exit dialog **once**. Already-paused sessions do not receive a duplicate dialog. Reset any pending modal request on Home, new Flight or Results.
- Add deterministic Android 15 background→foreground gameplay QA (same PID, preserved active flight/lives, native single Pause, no HUD Pause button) alongside rotation and viewport tests; add source contracts for paint transparency.
- Retain Bopi/Sova/Lisac abilities, the 18-unit hitbox, deliberately harder 54–110-unit gates and 164-unit safety floor from v1.0.35.
- Keep Gradle 9.6.0, AGP 9.5.0, JDK 17, `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`, offline-only app and existing shared preferences/backups.
