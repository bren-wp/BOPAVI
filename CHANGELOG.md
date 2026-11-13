# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.85 — New owner references, gameplay cleanup and code modularization (build 215)

**Published:** 2026-11-13 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.85)

- Reconcile the ten new BOPAVI owner mockups across five Android screen sizes (1080×1920, 1080×2280, 1080×2340, 1080×2400 and 1440×3040).
- Remove six small golden ring rewards per gate from both gameplay Canvas rendering and swept-collection physics, including source files and obsolete unit tests. Preserve existing legitimate gate coins, berries, crystals, and local balances.
- Split independent rendering/geometry classes out of large Kotlin files without changing public contracts, save-key migrations, animation resources or collision shapes.
- Audit abandoned screen references, developer-only code, dead symbols and binary asset dependencies; verify Android CI on API 35/36 and preserve three native Home actions (Shop, Skins, Settings).
- Use direct screenshot-to-reference comparisons only when authentic app captures and reference bytes are available at identical dimensions. No claim of 0% visual deviation until verified.

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
