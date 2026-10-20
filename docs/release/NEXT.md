# Next Android release

## v1.0.6 — Quick options and local records (build 136)

- Avoid redundant Home slogan (already present inside the branded logo) so the 48dp legal and secondary hit targets remain fully sized on compact phones.\n- Home has two small native 48dp actions **OPCIJE** and **REKORDI**, without a fifth navigation screen, external SDK or extra full-screen menu.
- Options allow choosing one of three difficulty levels for the next run. The selected difficulty now actually reaches GameSimulation (speed, gravity and passage opening), while the preexisting save key and fixed 18px hitbox are preserved.
- Existing offline preferences are reachable: sound on/off, haptic on/off, reduced-motion rendering (33ms active vs 14ms default), independent 0–100% music/effects volumes.
- Volume slider changes apply live to the native audio engine; persistent writes occur once on release to avoid excess disk operations. Gameplay options that need a new GameView apply on the next flight.
- Read-only local record overlay displays verified best score, single-run berry record, most passed gates, completed runs and historical flap count. No online leaderboard, telemetry, login, scores upload or reset control.
- Original three pilots, uninterrupted endless flight, collision geometry, original art, complete offline legal/privacy texts and previous progress remain intact.
- Add source regression tests for clickable controls, stored option persistence, difficulty wiring and absence of network permissions.
- Native CI Android Lint, unit tests, emulator API35 and API36, gameplay visual QA and Play screenshots must pass before merging/release. Physical battery measurements and Play approval are not implied.
