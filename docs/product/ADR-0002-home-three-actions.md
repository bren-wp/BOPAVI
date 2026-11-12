# ADR-0002 — Three-action Home and removal of legacy score/map screens

**Decision: APPROVED by product owner in the current development request, 2026-11-12.**

## Context

The BOPAVI Android interface previously exposed standalone Progress, Leaderboard,
Records, Achievements and Level Map screens and shortcuts. The owner explicitly
requested complete removal of those screens, options, navigation connections
and Home buttons, leaving **exactly Shop, Skins and Settings**.

## Decision

- Home has exactly three native accessible controls: Shop, Skins and Settings,
  in the reference and compact responsive layouts.
- Remove Play, Sound and every obsolete Progress, Records, Leaderboard,
  Achievements and Level Map action or shortcut from Home and menus.
- Remove their Kotlin screen/rendering classes and unused test fixtures.
  Retain shared layout and lock-icon classes still needed by Shop/Skins/Settings.
- Do not delete stored balances, owned skins, virtual purchases or imported
  progress, because an app update must not erase players' local data.
- Retain the gameplay simulation, Pause and Results implementation only for
  historical compatibility and debuggable-only QA; **the production Home no
  longer starts a new game**. Do not claim otherwise in store descriptions.
- Keep privacy/legal/SAF backups in Settings. No new INTERNET permission,
  advertising libraries, accounts or external services.

## Verification before merge

Verify Android Lint and compilation of APK/AAB, source/dead-code contracts,
Android 15/16 emulator navigation for all three Home controls, screenshots
from the real build, responsive layout/accessibility and save compatibility.

The exact pixel difference relative to earlier artwork is not certified and
external SAF-provider rollback/failure handling remains separately open.

This decision is specific to Home and legacy-screen removal; it does not
approve unrelated future premium feature expansion under ADR-0001.
