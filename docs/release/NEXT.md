# Next Android release

## v1.0.10 — Instant first flap and stable frame pacing (build 140)

- Remove a reproducible initial-frame timing bug: the idle first-flight preview could save `System.nanoTime()` and incorrectly include many idle seconds when the player first tapped. Previously the simulation clamped this to a sudden 100ms advance. The new monotonic `FlightFrameClock` establishes a fresh baseline on the first active frame.
- Reset the frame baseline on pause, app background, QA image capture, exit, and the actual first flap. A viewport redraw under 1ms accumulates time without simulating a negative or zero delta; long interruptions can no longer cause the first-input jump.
- Add dedicated deterministic Kotlin/JVM tests for delayed first tap, paused/background idle time, QA capture, sub-millisecond drawing requests and rewound timestamps.
- Remove the `withIndex()` wrapper from the high-frequency upcoming-obstacles renderer to avoid unnecessary per-frame iterable/iterator overhead; unchanged procedural spacing, drawn cap geometry and green forest visuals.
- No changes to 120Hz semi-implicit integration, swept r18 collision, score formula, lives/heart fairness, saved preferences, three pilots, offline legal documents, network permission or premium Home design.
- Android Lint, JVM tests, dead-code and privacy audit, Android 15/16 emulator QA, authentic Play screenshots and release artifact verification must all pass before merging and publishing.
