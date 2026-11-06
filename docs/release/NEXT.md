# Next Android release

## v1.0.52 — Splash / Home visual geometry and fantasy scenery (build 182)

**Candidate only — not yet verified, signed or published.** This version continues
the merged BOPAVI 1.0.51, and does not change gameplay physics, progression,
wallet balances, event dispatch or the real Android touch actions.

- **Splash / Loading:** reduce the oversized logo from 885×268 to 757×229
  reference pixels, preserving the natural 780:236 aspect ratio and restoring
  the owner's blue-sky margins. Keep the honest indeterminate preparation scan
  and genuine completion signal introduced in v1.0.51.
- **Home:** replace the non-square 745×483 aviator container with a square
  645×645 composition. Because the approved Bopi layers occupy a 512×512
  transparent canvas, FIT_CENTER previously shrank the entire pilot to only
  483 pixels. The square composition enlarges his painted silhouette while
  keeping the logo and functional PLAY button unobscured. The extents are
  validated against the actual classic 512×512 source alpha union.
- **Splash + Home scenery:** add deterministic, offline clouds, lush side
  cliffs, waterfall highlights and foreground foliage to the existing
  landscape rasterization. It contains no button images, fake currency,
  character overlays or clickable screenshot elements.
- Preserve native 3D Play, Skins, Shop, Settings, three Home footer actions,
  true wallet values, original BOPAVI branding and safe-area handling.
- Refresh Kotlin/source assertions for these reference coordinates.

## Evidence and release gates

Whole-screen 941×1672 RGB MAE for the **last published v1.0.51**, measured
directly from the unmodified owner's reference PNGs and original Android 15
QA screenshots (main run 42134092837):
Splash 79.961; Home 71.916; Gameplay 76.053; Game Over 74.910.
These are exploratory baselines **not** pixel-parity acceptance results.
Do not claim 1:1 until actual new emulator captures have been compared.

**Before release:** verify all GitHub CI checks, Android 15 Play/first-flap,
background/resume, pause/retry, Android 16 smoke, Kotlin and Python tests,
asset/privacy/dead-code audits, native 941×1672 screenshots and UI accessibility.
Reject regressions; never skip or falsify tests.

**Build 182 signing:** debug APK is for QA. CI produces an unsigned release
APK and AAB. Google Play submission requires the publisher's correct, private
upload signature. No public Release while mandatory checks remain incomplete.
