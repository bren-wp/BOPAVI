# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.84 — Only Shop, Skins and Settings on Home (build 214)

**Published:** 2026-11-12 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.84)

- Remove standalone Progress, Leaderboard, Records, Achievements and Level Map screens, all navigation routes and associated buttons.
- Home presents exactly three accessible native actions: **Shop, Skins and Settings**, in reference-size and compact modes. Neither Play nor Sound/Records/Map shortcuts are present.
- Eliminate dead screen models, gestures, drawing helpers and stale icon links; retain shared native camera/layout, lock symbol and result artwork used by the three remaining screens.
- Keep existing offline purchases, currency, save format, user-selected skins, SAF backup and Settings controls without deleting user data.
- Restrict the README and the new release to 11 genuine Android emulator screenshots; historical published downloads remain unchanged.
- Retain the underlying gameplay engine and result/pause flows for compatibility and debug-only QA, **but starting a new game is intentionally unavailable from the production Home screen**.
- Validate the three native UI destinations, Android 15/16 startup and controls, Gradle Android APK/AAB builds, screenshot inventory, and source security/dead-code audits.
- This change does not certify zero visual difference, provider atomicity or testing on physical Android devices.

## v1.0.83 — Safe backup lifecycle and stale-destination guards (build 213)

**Published:** 2026-11-12 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.83)

- Prevent background backup/restore tasks from scheduling onto a stopped executor or calling a destroyed Activity.
- Invalidate queued auto-save and manual-save jobs when the user changes or disables the selected SAF document.
- Persist destination removal before revoking provider access; release obsolete grants after successfully changing destinations.
- Preserve work already writing to an external provider during Activity shutdown instead of interrupting a potentially non-atomic document write.
- Add pure Kotlin/JVM regression tests for revision invalidation and closure, and a CI source contract for the backup lifecycle.
- Keep offline operation, old v1–v6 save compatibility, the current launcher and 941×1672 screen artwork.
- Limitations: a provider's SAF write still cannot be claimed atomically recoverable; external failure-injection and real Android-device tests remain outstanding.
