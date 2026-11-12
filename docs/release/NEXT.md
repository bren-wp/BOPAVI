# BOPAVI — Next Android release

## v1.0.84 — Only Shop, Skins and Settings on Home (build 214)

- Remove standalone Progress, Leaderboard, Records, Achievements and Level Map screens, all navigation routes and associated buttons.
- Home presents exactly three accessible native actions: **Shop, Skins and Settings**, in reference-size and compact modes. Neither Play nor Sound/Records/Map shortcuts are present.
- Eliminate dead screen models, gestures, drawing helpers and stale icon links; retain shared native camera/layout, lock symbol and result artwork used by the three remaining screens.
- Keep existing offline purchases, currency, save format, user-selected skins, SAF backup and Settings controls without deleting user data.
- Restrict the README and the new release to 11 genuine Android emulator screenshots; historical published downloads remain unchanged.
- Retain the underlying gameplay engine and result/pause flows for compatibility and debug-only QA, **but starting a new game is intentionally unavailable from the production Home screen**.
- Validate the three native UI destinations, Android 15/16 startup and controls, Gradle Android APK/AAB builds, screenshot inventory, and source security/dead-code audits.
- This change does not certify zero visual difference, provider atomicity or testing on physical Android devices.
