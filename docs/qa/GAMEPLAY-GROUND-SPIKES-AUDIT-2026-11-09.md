# BOPAVI — gameplay ground-edge safety and source audit (2026-11-09)

Baseline: `main` at commit `9e12fd6`, Android v1.0.69 (versionCode 199).
This is a targeted, source-based audit and corrective change, not a claim of
pixel-perfect visual certification, a penetration test, or a completed release.

## Corrected: bottom-edge spikes

The user explicitly disallows bottom-edge golden spikes before the first tap,
on the first flap and throughout flight, even where an older reference shows
them. `GameView.onDraw` previously called
`drawEnchantedForestGroundSpikes` for worlds 0 and 6 in both ready and active
flight. Removed this draw call and the entire private painter. Deleted its
orphaned geometry helper and geometry-only JVM test. Replaced old Python
source assertions that required spikes with regression assertions that forbid
them. This does **not** remove the pre-existing bottom world bound, the
player's 18-unit physical radius, pillar collision, pickups, or actual
position-synchronized hanging-mace hazards appearing later in play.

## Static security review (scoped)

- Android manifest has one exported launcher activity and
  `allowBackup=false`; `usesCleartextTraffic=false`; no INTERNET
  permission is requested.
- Gradle's application module declares JUnit as a test-only dependency and
  has shrinking/minification enabled for release.
- Screenshot QA flags in `GameView` are guarded by the app's
  `FLAG_DEBUGGABLE`, so those flags do not activate screenshot freezing
  in release builds.
- Virtual purchases and player progression are local state; evaluate
  `ProgressStore` save/import parsing and concurrency separately before
  advertising any anti-tamper properties.
- Do not publish signing keys or upload certificates in commits or CI logs.

## Dead-code findings and follow-up

- Confirmed and removed one unused-after-fix geometry object and its
  dedicated unit tests. Kotlin render classes and screens that are built
  dynamically through `MainActivity` must **not** be deleted based on naive
  reference counts.
- The old `docs/product/CURRENT_SCREENS.md` inventory is stale relative
  to `MainActivity`: the activity also routes to Skins, Shop, Settings,
  Level Map, Achievements, Level Complete and leaderboards. Update the
  screen inventory after actual UI navigation captures.
- Full dependency/SBOM scanning, taint analysis, fault injection, user
  journey tests, static analysis of every retained source file and
  real-device regression verification are **not yet complete**.

## Required acceptance before production

1. Android unit tests and all `tests/test_*.py` source contracts pass.
2. Gameplay capture before first flap, after first flap, and later in flight
   contains **no lower ground-edge spikes**, with consistent collisions.
3. Verify nine owner references against same-resolution device captures using
   `tools/compare_owner_ui.py`; publish measured deltas, not guessed percentages.
4. Manual testing at 941×1672 plus smaller, taller, cutout and landscape
   devices; verify no overlap, animation frame drops, or regressions.
5. Inspect final unsigned AAB + release-signing workflow before distributing
   an update to Play; do not infer readiness from source checks alone.

## Additional hardening: exported Activity QA extras

Found `MainActivity` accepted `bopavi_qa_loading_preview` and
first-flap/overlay extras from an external launcher Intent on production
builds, although `GameView` already guarded its screenshot flags.
Hardened all three launch extras behind `FLAG_DEBUGGABLE` in
`MainActivity.qaIntentEnabled`. A release launch now follows the ordinary
user path regardless of user-supplied QA extras. The existing debug emulator
capture workflow retains those flags. Added lifecycle source regression
assertions for this behavior.
