# BOPAVI — Next Android release

## v1.0.84 — Home with Shop, Skins and Settings only (build 214)

- Remove Home Play, Achievements, Leaderboard, Sound and all other interactive Home shortcuts on both reference and compact screens.
- Retain exactly three native primary Home actions: Shop, Skins and Settings, with responsive large-text / landscape fallbacks.
- Remove Progress, Achievements, Leaderboard/Records and Level Map destination screens, routes, and shortcut entries; remove unused rendering/support classes and their obsolete regression cases.
- Remove the interactive star/map wallet; show the actual coin balance as passive information only.
- Preserve existing local progress, earned currency, owned skins, backups, gameplay engine and Settings privacy controls to avoid destructive user migrations.
- **Product behavior:** New gameplay no longer starts from Home because Play was explicitly removed. Gameplay/result paths remain in the source for compatibility and internal regression tests; reintroducing gameplay navigation would require a separate design decision.
- Security/QA: prevent orphaned routes, check that the Home hierarchy has exactly three tappable app-owned controls, run Android emulator and offline backup regression tests.
