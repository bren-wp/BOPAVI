# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.10 — Instant first flap and stable frame pacing (build 140)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.10)

- Remove a reproducible initial-frame timing bug: the idle first-flight preview could save `System.nanoTime()` and incorrectly include many idle seconds when the player first tapped. Previously the simulation clamped this to a sudden 100ms advance. The new monotonic `FlightFrameClock` establishes a fresh baseline on the first active frame.
- Reset the frame baseline on pause, app background, QA image capture, exit, and the actual first flap. A viewport redraw under 1ms accumulates time without simulating a negative or zero delta; long interruptions can no longer cause the first-input jump.
- Add dedicated deterministic Kotlin/JVM tests for delayed first tap, paused/background idle time, QA capture, sub-millisecond drawing requests and rewound timestamps.
- Remove the `withIndex()` wrapper from the high-frequency upcoming-obstacles renderer to avoid unnecessary per-frame iterable/iterator overhead; unchanged procedural spacing, drawn cap geometry and green forest visuals.
- No changes to 120Hz semi-implicit integration, swept r18 collision, score formula, lives/heart fairness, saved preferences, three pilots, offline legal documents, network permission or premium Home design.
- Android Lint, JVM tests, dead-code and privacy audit, Android 15/16 emulator QA, authentic Play screenshots and release artifact verification must all pass before merging and publishing.

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
