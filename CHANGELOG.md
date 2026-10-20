# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

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

## v1.0.5 — Uninterrupted speed and consistent character artwork (build 135)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.5)

- Smooth horizontal flight speed across invisible procedural challenge-batch boundaries instead of instantly snapping to the next group's speed. Gate and berry collision sweeps now use the exact scroll displacement.
- Keep original 120 Hz semi-implicit physics substeps, fixed body collision circle, obstacle cadence, food collection, saved high scores and continuous incoming obstacles unchanged.
- Show the selected player (Bopi, Sova or Lisac) in Pause, matching Home, Flight and Results. No new menu, login, network permission, tracking or paid feature.
- Add focused JVM and source-level regression checks for boundary speed continuity, pause art selection and save compatibility. Make publisher-only e-mail/HTTPS links inside the complete offline legal notices interactive and enforce an exact legal-link allowlist.
- Carry forward complete, offline legal texts sourced verbatim from the owner's index.html (11 privacy / 12 terms sections with publisher contact); existing MIT licensing history remains preserved.
- CI and emulator tests must pass before publication. Physical battery profiling and Google Play store approval are not implied.
