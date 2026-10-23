# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.22 — Readable lives, balanced flight HUD and pause spacing (build 152)

**Published:** 2026-10-23 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.22)

- **Direct screenshot correction:** replace the cramped top-row cards with an aligned three-column hierarchy. Score (18–146), berries (154–276), and the wider life panel (284–462) now share a taller 16–82 logical range and consistent 8-unit gutters. The life panel receives the most horizontal room.
- **Real life indicator:** centre the three hearts at x=320, 373, 426 on their own lower line (baseline 69). Keep the actual `3/3` counter right-aligned at x=445 above them, at a fixed safe inset rather than pressed against the border or the last heart. The score, fruit, hearts and counter continue reading real GameSimulation state, without any invented values.
- **Text hierarchy:** larger common Croatian labels (BODOVI, HRANA, ŽIVOTI), numbers on an aligned lower baseline; the existing measured-width number renderer preserves long marathon scores without drawing over adjacent indicators.
- **Power and pause:** put the MOĆ heading and its colour-coded, live pilot ability on a separate 91–140 logical card, fully outside the real native pause button's right lane. Place the pause hit target only after the taller top row, updating the Android status-bar/cutout-safe overlay policy from the real HUD transform. Retain a full 56dp accessible touch target, responsive layout and existing 30fps reduced-motion mode.
- **Geometry QA:** new assertions verify identical card heights, precise gaps, visual centring of all hearts, vertical separation of the hearts from the numeric counter, increased life-panel width, and non-overlapping power/pause regions. Revalidated source contracts for the native Canvas HUD and pause layout; full Android Lint, Kotlin/JVM, Android 15 live gameplay screenshots and Android 16 emulator smoke remain mandatory.
- **No gameplay change:** keep movement physics, collision, score formulas, birds and abilities, moving pillar warnings, offline privacy, save formats, permissions and previously approved original graphics unchanged. Real-device optical inspection is still recommended; this release must not claim F-Droid or Google Play approval.

## v1.0.21 — Aligned lives/HUD and readable moving-pillar warnings (build 151)

**Published:** 2026-10-22 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.21)

- **Polished flight HUD:** align score, berries and lives as three equally tall premium cards with deliberate spacing. The lives card is wider, displaying all three separate hearts (bright outlined full hearts and clearly outlined empty hearts), plus a concise 0/3–3/3 numerical indicator. Score/berry text is measured against the actual bold font and shrinks only if a long marathon counter would overflow. The hearts and labels always use the real GameSimulation values.
- **Power hierarchy:** move the pilot ability into a taller, clearly labelled second-row chip, with its existing colour-coded one-time shield state. The ability chip stays fully left of the native pause-control lane; neither HUD row changes physics or touch targets.
- **Shared geometry:** introduce a platform-free FlightHudLayout with four fixed 480x800 rectangles and preallocated heart positions. The renderer reuses these values rather than individually guessed positions. Add Kotlin geometric non-overlap, 18px body-independent indicator, text-layout contract and native pause-lane tests.
- **Moving-pillar warning:** an upcoming moving obstacle now shows a gold exclamation mark in its solid upper pillar, before reaching the bird. Static, already-passed and offscreen pillars never show the sign; the warning does not blink (also respects reduced-motion preferences), change openings or add fake collisions. Existing glints remain visible.
- **Stability and privacy:** no extra images, frame-loop allocations, network, ads, tracking, new permissions, save migration or changes to approved 11 character variants. Reuse existing shared Canvas Path/Paint and cached text.
- **Verification:** source contract, Kotlin/JVM, Lint, responsive layout constraints, Android 15 live gameplay and screenshots, Android 16 smoke, privacy and dead-code audit, unsigned AAB/APK integrity and checksums remain mandatory. Manual TalkBack perception and physical-device balancing are not substituted by emulator testing.
