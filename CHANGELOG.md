# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.9 — Stability, renderer and control polish (build 139)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.9)

- Optimize the frequent per-frame stone pillar texture draw path by eliminating a captured local function created for every pillar. Retain all course geometry, visuals, foliage, seeded appearance and fixed collision geometry.
- Remove exactly three verified unreferenced gameplay visibility helpers (coinVisible, starVisible, powerVisible). Preserve their collection state, offline saves and all historical migration/test interfaces, including ParallaxScenery, levelProgress, and legacy levelTransition.
- Improve the HUD life indicator: empty hearts have a thin contrasting outline for small displays and low-vision players; numbers, scaling and pause-button safe area remain unchanged.
- Match native Android difficulty radio controls, setting switches, and audio-volume sliders to BOPAVI's established cyan/teal/gold palette without adding UI screens, third-party libraries or permissions.
- A paused flight returning from the background resumes with a single ▶ tap, while the hardware/system Back control continues to open NASTAVI/PONOVO/IZLAZ safely. Canceling that dialog cannot resume gameplay without window focus.
- Extend regression audit and lifecycle tests to cover these controls, absence of unused symbols and static rendering efficiency.
- Retain deterministic 120 Hz physics slices, 18px hitbox, smooth procedural scroll-speed, safe obstacle openings, three-life cap, fair heart/berry pickups and Croatian offline legal notices; do not reset player records or personal preferences.
- Full Android Lint/JVM, static privacy & dead-code audits, API35 and API36 physical emulator game-flow checks, authentic Play imagery and artifact checks required before release. Physical-device battery measurements and Google Play approval are not implied.

## v1.0.8 — Heart and berry fairness, impact clarity, polish (build 138)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.8)

- Repair the actual 3-life pickup edge case: every heart-gate with a normal berry now shows a berry while at full health instead of appearing empty.
- The game grants **exactly one reward per gate**: collecting the berry prevents the heart from reappearing after damage; collecting the heart prevents a berry from appearing immediately after life restoration.
- Restoring health now triggers the same **one-shot collection sound and haptic event** as berries, with no extra audio engine allocations.
- Losing an arcade life has a subtle warm-rose impact effect and a steady grace-period outline, distinct from the existing cyan legacy shield and respecting reduced-motion preferences.
- Correct the developer-only collision overlay to draw the currently available pickup, consistent with the exact game model.
- Add deterministic JVM tests that assert rare pickup behaviour at full/partial health and prevent a second reward when health changes. Keep original 18px collider and procedural obstacle positions unchanged.
- Maintain v1.0.7 published README/store statements until GitHub's confirmed v1.0.8 release. Keep all local save keys, legal HTML, three pilots, offline/no-ads behavior and privacy unchanged.
- Gate publication on passing Android Lint/JVM, full static/security/dead-code audits, Android 15/16 emulator QA and real release screenshots. Production signing/physical-device battery tests are not inferred.
