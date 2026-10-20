# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

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

## v1.0.6 — Quick options and local records (build 136)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.6)

- Avoid redundant Home slogan (already present inside the branded logo) so the 48dp legal and secondary hit targets remain fully sized on compact phones.
- Home has two small native 48dp actions **OPCIJE** and **REKORDI**, without a fifth navigation screen, external SDK or extra full-screen menu.
- Options allow choosing one of three difficulty levels for the next run. The selected difficulty now actually reaches GameSimulation (speed, gravity and passage opening), while the preexisting save key and fixed 18px hitbox are preserved.
- Existing offline preferences are reachable: sound on/off, haptic on/off, reduced-motion rendering (33ms active vs 14ms default), independent 0–100% music/effects volumes.
- Volume slider changes apply live to the native audio engine; persistent writes occur once on release to avoid excess disk operations. Gameplay options that need a new GameView apply on the next flight.
- Read-only local record overlay displays verified best score, single-run berry record, most passed gates, completed runs and historical flap count. No online leaderboard, telemetry, login, scores upload or reset control.
- Original three pilots, uninterrupted endless flight, collision geometry, original art, complete offline legal/privacy texts and previous progress remain intact.
- Add source regression tests for clickable controls, stored option persistence, difficulty wiring and absence of network permissions.
- Native CI Android Lint, unit tests, emulator API35 and API36, gameplay visual QA and Play screenshots must pass before merging/release. Physical battery measurements and Play approval are not implied.
