# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

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

## v1.0.7 — Forest arcade: 3 lives, rare hearts and smaller HUD (build 137)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.7)

- Endless mode starts with three lives and shows small hearts beside a reduced score/food HUD; legacy non-endless unit-level behavior stays unchanged.
- One collision consumes at most one heart, with a bounded 1.6-second invulnerability interval; the third unshielded impact ends the run. Existing legacy shields remain independent.
- Every 17th streaming gate offers an optional collectible heart if any life was lost, capped at 3. The rare heart replaces a berry at that gate so it never overlaps a berry and does not alter saved food scores or earnings.
- Native foliage and soft moss added to the first forest biome stone caps; visuals remain confined to already-solid collision geometry, without changing gameplay gaps or generating bitmaps each frame.
- Reduced top HUD dimensions leave more of the flight visible while preserving real score/food values and pause-button clearance.
- Live option/record overlays use dark navy, blue and mint styling consistent with the native Home/Results palette, preserving offline storage and 48dp actions.
- Preserve all legacy save keys, all three playable pilots, original artwork, unchanged legal files, no network, analytics, ads, or sign-in.
- Full Android lint, JVM/physics tests, API35/36 emulator QA, privacy/security/dead-code audit and authentic store screenshots required before merging/publishing.
- Near the halfway point of a procedural obstacle segment, pre-generate the next cached segment. Do not regenerate it inside the precise boundary frame; all original world coordinates, visible obstacle cadence and saved scores remain unchanged.
- Marketing README and current-screen documentation have been synchronized with published v1.0.6 features only; pending v1.0.7 mechanics remain in this candidate changelog until release.
