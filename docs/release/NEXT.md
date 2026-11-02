# Next Android release

## v1.0.45 — Splash/Home responsive and fantasy environment (build 175)

- Maintain actual Android Splash/Home controls, stored coin/star balances, selected original pilot and offline gameplay.
- Home reference mode now falls back to the native adaptive layout for enlarged system fonts (1.3x and above); avoid fixed-grid overlap.
- JVM regression matrix covers nine size/orientation profiles and multiple font scaling factors.
- Android 15/16 emulator QA checks representative small, compact, reference, standard, tall and large screen sizes, density and rotation. Not every device/form factor can be physically emulated.
- Continue improving the separately rendered fantasy background; never bake buttons, currency or Bopi's interactive state into scenery.
- Owner 941×1672 reference and original emulator screenshots are the visual baseline, but a verified post-change 1:1 pixel match is still pending.

**Build 175 distribution:** unsigned APK/AAB may be generated for QA. Do not merge, sign or publish until all required checks pass.

- Seven real emulator viewport/density profiles now replace the earlier three-profile loop; the preexisting large-font, insets, accessible native hit target, landscape and screenshot contract checks remain active.

- Added deterministic JVM logical breakpoint sweep across 29 widths and 23 heights, with four system font scales; Splash additionally checks all painted boxes remain onscreen under uniform scaling. This complements seven *real* Android emulator resolutions, but does not replace actual screenshots on devices.

### Compact Home accessibility regression (PR #209)

- Investigated Android 15 emulator run #2029: at 480×800/160dpi the four upper actions were visible, while the three footer shortcuts were below the fold inside the existing native ScrollView. The original viewport assertion incorrectly expected all seven controls in a single UIAutomator snapshot.
- The Android viewport QA now performs real on-device swipes for compact windows, checks that all seven independent actions become fully visible and clickable with at least 48dp hit bounds, and saves post-scroll XML/screenshot evidence. Missing, duplicate and undersized actions still fail CI; the 941×1672 reference Home must fit without a swipe.
- Added offline parser regression tests for above/below-fold controls. This does not assert 1:1 image equality and does not replace successful Android 15/16 emulator execution.
