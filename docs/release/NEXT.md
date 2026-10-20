# Next Android release

## v1.0.7 — Forest arcade: 3 lives, rare hearts and smaller HUD (build 137)

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
