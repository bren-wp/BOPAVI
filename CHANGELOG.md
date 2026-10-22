# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.21 — Aligned lives/HUD and readable moving-pillar warnings (build 151)

**Published:** 2026-10-22 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.21)

- **Polished flight HUD:** align score, berries and lives as three equally tall premium cards with deliberate spacing. The lives card is wider, displaying all three separate hearts (bright outlined full hearts and clearly outlined empty hearts), plus a concise 0/3–3/3 numerical indicator. Score/berry text is measured against the actual bold font and shrinks only if a long marathon counter would overflow. The hearts and labels always use the real GameSimulation values.
- **Power hierarchy:** move the pilot ability into a taller, clearly labelled second-row chip, with its existing colour-coded one-time shield state. The ability chip stays fully left of the native pause-control lane; neither HUD row changes physics or touch targets.
- **Shared geometry:** introduce a platform-free FlightHudLayout with four fixed 480x800 rectangles and preallocated heart positions. The renderer reuses these values rather than individually guessed positions. Add Kotlin geometric non-overlap, 18px body-independent indicator, text-layout contract and native pause-lane tests.
- **Moving-pillar warning:** an upcoming moving obstacle now shows a gold exclamation mark in its solid upper pillar, before reaching the bird. Static, already-passed and offscreen pillars never show the sign; the warning does not blink (also respects reduced-motion preferences), change openings or add fake collisions. Existing glints remain visible.
- **Stability and privacy:** no extra images, frame-loop allocations, network, ads, tracking, new permissions, save migration or changes to approved 11 character variants. Reuse existing shared Canvas Path/Paint and cached text.
- **Verification:** source contract, Kotlin/JVM, Lint, responsive layout constraints, Android 15 live gameplay and screenshots, Android 16 smoke, privacy and dead-code audit, unsigned AAB/APK integrity and checksums remain mandatory. Manual TalkBack perception and physical-device balancing are not substituted by emulator testing.

## v1.0.20 — Better owl glide, clearer abilities and frame-rate QA (build 150)

**Published:** 2026-10-22 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.20)

- **Sova / real gameplay:** retains its 8% lower gravity and now caps downward velocity at 315 logical units/s instead of 365. This gives more reaction time under moving pillars during sustained falls, while preserving the same -255 tap impulse and 18px body collider as every other pilot. The change is bounded to the Sova pilot; all other physics remains unchanged.
- **More readable character powers:** the native HUD ability chip now uses an accessible distinct high-contrast tint for each pilot. Lisac's one-use shield is clearly green while ready and red/pink after consumption, backed by its existing changing Croatian TalkBack status; a slim visual accent stays inside the existing HUD rectangle, left of native pause control.
- **Pause and results:** a concise ability explanation appears in the native Pause dialog, and Results' accessibility description explains the selected pilot's advantage rather than only giving a generic ability name. The Home pilot cards likewise describe numerical characteristics (Bopi +6% speed, Sova less gravity/fall, Lisac one absorbed hit).
- **Performance and compatibility:** no bitmap generation, network call, additional fragment, or new animation in the drawing loop. The HUD tint and status are inexpensive enum lookups. Existing saved scores, player choice, store/legal links and all three difficulty settings are unchanged; historic CLASSIC game instances keep 365-unit descent.
- **Regression QA:** deterministic Kotlin/JVM checks verify actual sustained owl descent, equal collider and flap impulse, contrasting Fox shield states, and matching pilot movement after one second at 30, 60 and 120 Hz. The source contract guards native HUD and all screens; Android Lint, JVM tests, Android 15 gameplay/screenshots, Android 16 smoke, package integrity, privacy and dead-code audits remain mandatory.
- **Limitations:** physical-device TalkBack comfort and subjective play balance should still be checked manually; passing GitHub CI does not constitute Google Play/F-Droid approval or official distribution.
