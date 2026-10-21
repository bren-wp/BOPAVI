# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.11 — Endless visual precision and smooth scenery (build 141)

**Published:** 2026-10-21 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.11)

- Fix long-distance scenery freeze/jitter: collision/world positioning already used precise Double distance, but dynamic parallax, sky camera oscillation, mist, foreground leaves and fallback landscape were still using a Float rounded after every physics step. Beyond large marathon distances the Float value stopped changing on small steps, creating visible discontinuity.
- Compute scenery phases using the existing Double monotonic distance, wrapping at each small visual tile period before conversion to Float. No new animation timers, allocations, resources, SDKs or permissions.
- Preserve the original multi-layer landscape speeds and subtle drift: 3-depth islands, background camera sway, clouds, particles, foreground vegetation, ridge and fallback biome silhouettes. Reduced-motion mode also freezes ridges in the rare fallback renderer.
- Dedicated JVM and Python regression tests cover movement at 2^25 world units plus a 0.25px step, invalid phases, all three parallax layers and zero new changes to the swept collision, r18 body or 120Hz physics.
- Existing three playable pilots, local storage, privacy/terms, hearts/berries, HUD, pause and Play imagery retained.
- **Release gate:** full source/privacy/dead-code audit, sound/art verification, Android Lint/JVM/APK/AAB, Android 15 gameplay/collision screenshots, Android 16 navigation and versioned GitHub Release. Hardware battery tests and Google Play approval remain external.

## v1.0.10 — Instant first flap and stable frame pacing (build 140)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.10)

- Remove a reproducible initial-frame timing bug: the idle first-flight preview could save `System.nanoTime()` and incorrectly include many idle seconds when the player first tapped. Previously the simulation clamped this to a sudden 100ms advance. The new monotonic `FlightFrameClock` establishes a fresh baseline on the first active frame.
- Reset the frame baseline on pause, app background, QA image capture, exit, and the actual first flap. A viewport redraw under 1ms accumulates time without simulating a negative or zero delta; long interruptions can no longer cause the first-input jump.
- Add dedicated deterministic Kotlin/JVM tests for delayed first tap, paused/background idle time, QA capture, sub-millisecond drawing requests and rewound timestamps.
- Remove the `withIndex()` wrapper from the high-frequency upcoming-obstacles renderer to avoid unnecessary per-frame iterable/iterator overhead; unchanged procedural spacing, drawn cap geometry and green forest visuals.
- No changes to 120Hz semi-implicit integration, swept r18 collision, score formula, lives/heart fairness, saved preferences, three pilots, offline legal documents, network permission or premium Home design.
- Android Lint, JVM tests, dead-code and privacy audit, Android 15/16 emulator QA, authentic Play screenshots and release artifact verification must all pass before merging and publishing.
