# Next Android release

## v1.0.36 — Canvas transparency and safe return to flight (build 166)

- Fix common Android Canvas `fill(color)` resetting supplied ARGB alpha to 100%. Respect semi-transparent glass, ghosted/empty hearts, pillar materials, bevel highlights, atmospheric layers and effect rings, restoring intended depth while leaving collision bounds unchanged.
- Prevent foreground re-entry into a stuck paused flight with no Pause HUD. On background, freeze an active run; when its Activity safely regains focus, present the existing native Continue/Retry/Exit dialog **once**. Already-paused sessions do not receive a duplicate dialog. Reset any pending modal request on Home, new Flight or Results.
- Add deterministic Android 15 background→foreground gameplay QA (same PID, preserved active flight/lives, native single Pause, no HUD Pause button) alongside rotation and viewport tests; add source contracts for paint transparency.
- Retain Bopi/Sova/Lisac abilities, the 18-unit hitbox, deliberately harder 54–110-unit gates and 164-unit safety floor from v1.0.35.
- Keep Gradle 9.6.0, AGP 9.5.0, JDK 17, `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`, offline-only app and existing shared preferences/backups.

Only publish when Python audits, Lint, JVM unit tests, Android 15 real-window gameplay/background/rotation QA, Android 16 navigation QA, release binary validation and SHA-256 verification all succeed.

GitHub provides unsigned Release APK and AAB; Play Store upload still requires the publisher's existing private upload key. Pixel-for-pixel identity against reference images not available in this conversation and tests on real physical devices are not independently verified.
