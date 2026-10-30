# v1.0.38 — Home reference redesign, screen 1 / 9 (build 168)

Based on the owner's 941×1672 Home reference, reorganize the real native Home screen into a resource strip, centered BOPAVI logo, selected-pilot portrait, primary PLAY, SKINS/SHOP buttons, SETTINGS and ACHIEVEMENTS/LEADERBOARD/SOUND shortcuts.

Changes:
- Remove legacy inline pilot cards and intrusive Home legal links. Pilot selection remains in Skins. Native offline privacy and terms remain accessible under Settings.
- Show only **real** earned coins and progress stars, never hard-coded reference screenshot figures, fabricated gem wallet, purchases or cloud sync.
- Achievements shows actual local records; Leaderboard opens existing offline records; Sound opens the functional options dialog. Level Map remains reachable through Settings and dismisses that modal safely.
- Improve responsive hero sizing based on available screenHeightDp and preserve 48dp touch targets, safe insets and scrollability on truly undersized layouts.
- Retain Gradle 9.6.0, AGP 9.5.0, Android API36, saved data, offline functionality, existing gameplay physics/obstacle difficulty, lives-only flight HUD and Android 15/16 compatibility.

This is a **reference-oriented native Home layout**, not yet a verified pixel-identical 1:1 recreation: the attached full-screen visual reference is not separately decomposed into production-ready layered artwork. Additional screen-by-screen visual assets and pixel-difference QA still required. Never ship if Lint, source audit, emulator UI, lifecycle, screenshots or artifact integrity gates fail.
