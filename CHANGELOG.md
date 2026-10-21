# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.12 — Seam allocations and adaptive frame pacing (build 142)

**Published:** 2026-10-21 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.12)

- Endless obstacle transitions reuse all five pickup-flag arrays when the next segment has the same gate count, resetting every boolean to prevent carrying berries, hearts, coins, stars or powers into the new group.
- When adjacent procedural groups have different gate counts, create correctly sized fresh arrays as before; preserve pickup rules, saved progress and the existing 18px bird hitbox.
- The active Android Canvas schedules redraws on the system's next vsync rather than an artificial 14ms delay. This allows 60/90/120Hz displays to use their real refresh rhythm; reduced-motion mode retains its roughly 30fps cap.
- Maintain existing FlightFrameClock behavior: the first flap, pause, backgrounding, QA capture and resumed flight never accumulate idle physics time.
- Add a deterministic Kotlin/JVM seam regression for all five pickup arrays and adapt the Python clock/render contracts. Keep the marathon parallax precision fixes from v1.0.11.
- Distribution now includes the existing QA debug APK, a separate **unsigned release APK** for signing, an **unsigned Google Play AAB** for the existing upload key, and a Git-commit-pinned **F-Droid submission ZIP** with Croatian metadata and real screenshots.
- All release files are listed in SHA256SUMS.txt. The MIT project license is present; official F-Droid dependency/resource licensing review and build validation remain required.
- **Release gate:** pass Android Lint, Kotlin/JVM tests, privacy/security and dead-code audits, Android 15/16 emulator checks, genuine screenshot review, APK/AAB structure and checksum verification before publishing. Device-level frame pacing and battery improvements require measurements; no percentage or Google Play availability claims are implied.

## v1.0.11 — Endless visual precision and smooth scenery (build 141)

**Published:** 2026-10-21 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.11)

- Fix long-distance scenery freeze/jitter: collision/world positioning already used precise Double distance, but dynamic parallax, sky camera oscillation, mist, foreground leaves and fallback landscape were still using a Float rounded after every physics step. Beyond large marathon distances the Float value stopped changing on small steps, creating visible discontinuity.
- Compute scenery phases using the existing Double monotonic distance, wrapping at each small visual tile period before conversion to Float. No new animation timers, allocations, resources, SDKs or permissions.
- Preserve the original multi-layer landscape speeds and subtle drift: 3-depth islands, background camera sway, clouds, particles, foreground vegetation, ridge and fallback biome silhouettes. Reduced-motion mode also freezes ridges in the rare fallback renderer.
- Dedicated JVM and Python regression tests cover movement at 2^25 world units plus a 0.25px step, invalid phases, all three parallax layers and zero new changes to the swept collision, r18 body or 120Hz physics.
- Existing three playable pilots, local storage, privacy/terms, hearts/berries, HUD, pause and Play imagery retained.
- **Release gate:** full source/privacy/dead-code audit, sound/art verification, Android Lint/JVM/APK/AAB, Android 15 gameplay/collision screenshots, Android 16 navigation and versioned GitHub Release. Hardware battery tests and Google Play approval remain external.
