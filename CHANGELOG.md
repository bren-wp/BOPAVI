# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.36 — Canvas transparency and safe return to flight (build 166)

**Published:** 2026-10-29 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.36)

- Fix common Android Canvas `fill(color)` resetting supplied ARGB alpha to 100%. Respect semi-transparent glass, ghosted/empty hearts, pillar materials, bevel highlights, atmospheric layers and effect rings, restoring intended depth while leaving collision bounds unchanged.
- Prevent foreground re-entry into a stuck paused flight with no Pause HUD. On background, freeze an active run; when its Activity safely regains focus, present the existing native Continue/Retry/Exit dialog **once**. Already-paused sessions do not receive a duplicate dialog. Reset any pending modal request on Home, new Flight or Results.
- Add deterministic Android 15 background→foreground gameplay QA (same PID, preserved active flight/lives, native single Pause, no HUD Pause button) alongside rotation and viewport tests; add source contracts for paint transparency.
- Retain Bopi/Sova/Lisac abilities, the 18-unit hitbox, deliberately harder 54–110-unit gates and 164-unit safety floor from v1.0.35.
- Keep Gradle 9.6.0, AGP 9.5.0, JDK 17, `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`, offline-only app and existing shared preferences/backups.

## v1.0.35 — Lives-only Flight, frontal pilots and harder polished obstacles (build 165)

**Published:** 2026-10-29 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.35)

- Remove the in-game Pause button completely and strip the score, food, power and status chips from the live flight canvas; only the centered lives and three actual heart indicators remain. Android system Back retains the accessible Continue/Retry/Exit dialog. Scoring and saved records remain functional on Results/Records.
- Improve Sova/Lisac's frontal flight by limiting full-body pitch equally for all pilots; the fox tail is recentered behind the body in original vector art while wings keep the existing flap cadence and the real 18-unit collider.
- Redraw collision-aligned obstacle lips with depth highlights and cleaner shafts, removing busy flecks without changing the actual collider. Improve difficulty after the introductory pillars via narrower/recovery openings and alternating 54–110-unit real pillar widths, while retaining the 164-unit minimum gate gap and the existing hard-mode 145-unit physical-opening floor.
- Upgrade the Android build toolchain to Android Gradle Plugin 9.5.0, Gradle 9.6.0 and JDK 17, keeping compileSdk/targetSdk 36 and minSdk 26.
- Remove retired gameplay overlay/status helpers and replace outdated source/UI contracts with lives-only and real system-Back checks. Keep original score/history/ability save data, offline privacy policy, motion-accessibility and lifecycle protections.
