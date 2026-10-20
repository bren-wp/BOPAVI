# Next Android release — BOPAVI v1.0.9 (build 139)

## Scope: stability, fast renderer, and native control consistency

- Optimize the frequent per-frame stone pillar texture draw path by eliminating a captured local function created for every pillar. Retain all course geometry, visuals, foliage, seeded appearance and fixed collision geometry.
- Remove exactly three verified unreferenced gameplay visibility helpers (coinVisible, starVisible, powerVisible). Preserve their collection state, offline saves and all historical migration/test interfaces, including ParallaxScenery, levelProgress, and legacy levelTransition.
- Improve the HUD life indicator: empty hearts have a thin contrasting outline for small displays and low-vision players; numbers, scaling and pause-button safe area remain unchanged.
- Match native Android difficulty radio controls, setting switches, and audio-volume sliders to BOPAVI's established cyan/teal/gold palette without adding UI screens, third-party libraries or permissions.
- A paused flight returning from the background resumes with a single ▶ tap, while the hardware/system Back control continues to open NASTAVI/PONOVO/IZLAZ safely. Canceling that dialog cannot resume gameplay without window focus.
- Extend regression audit and lifecycle tests to cover these controls, absence of unused symbols and static rendering efficiency.
- Retain deterministic 120 Hz physics slices, 18px hitbox, smooth procedural scroll-speed, safe obstacle openings, three-life cap, fair heart/berry pickups and Croatian offline legal notices; do not reset player records or personal preferences.
- Full Android Lint/JVM, static privacy & dead-code audits, API35 and API36 physical emulator game-flow checks, authentic Play imagery and artifact checks required before release. Physical-device battery measurements and Google Play approval are not implied.
