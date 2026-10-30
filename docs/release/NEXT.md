## v1.0.38 — Reference Home redesign (build 168)

This update begins the nine-screen reference overhaul, concentrating on the Home screen based on the owner's 941 × 1672 portrait design.

- Reorganize Home into an earned-resource strip, existing BOPAVI logo, large selected-pilot hero, gold PLAY, purple SKINS, green SHOP, blue SETTINGS, and three genuine shortcuts for ACHIEVEMENTS, LEADERBOARD and SOUND.
- Remove redundant inline pilot cards and old legal links from Home. Retain character selection in Skins; move full original Privacy Policy and Terms of Service behind accessible native Settings controls.
- Keep the Level Map reachable from Settings, using a real dismiss-and-navigate action rather than a dead button.
- Display only actual locally earned coins and stars; do not present fictional gems, online purchase controls or placeholder achievements.
- Add live local achievements sourced from saved wins, total runs, wing flaps and best obstacles.
- Adapt hero height to available screenHeightDp, retain native 48dp minimum touch targets, safe Android 15/16 window insets, large-font and compact portrait QA.
- Preserve Gradle 9.6.0, Android Gradle Plugin 9.5.0, compile/target API36, physical flight simulation, stronger obstacles, lives-only HUD, offline operation and stored records.

**Visual validation scope:** This is phase one, not yet a verified pixel-perfect 1:1 composition. The reference screenshot's rich illustrative layers have not been imported as isolated production sprites, and physical-device comparisons are still needed. Do not publish until the complete source, Lint/JVM, API35/API36 emulators, screenshot and artifact integrity gates pass.
