# Next Android release

## v1.0.54 — Splash / Loading highest pixel discrepancy (build 184)

**Candidate only. NOT verified, signed, merged or published.**

The published v1.0.53 main build passed Android 15/16; its real 941×1672
Splash capture differs from the owner reference by **78.201 RGB MAE**,
the highest whole-screen error among the four priority screens.
Numerical source and exact screenshot regions are in
`docs/qa/OWNER-941x1672-BASELINE-v1.0.53.md`.

### Implemented on this candidate branch

- Redraw the winged original BOPAVI logo using taller vector geometry
  (`docs/assets/logo.svg`, natural 780×297 raster). Align the logo
  inside owner-reference rectangle x=91..850, y=95..384 without stretching
  a low-resolution bitmap.
- Reposition the winged, selected native Bopi 512×512 sprite group on a
  704-square reference grid (90,365)–(794,1069). Keep all wing and body
  layers aligned and respect their original alpha silhouette.
- Generate **Splash-only**, entirely offline `loadingback.png` from the
  existing scenery plus owner-located castles, waterfalls, flowered cliffs
  and translucent clouds. Home retains its prior `homeback.png`.
- Retain the real asynchronous preparation event and honest indeterminate
  progress bar, native accessibility, and no fabricated coins/scores.
- No Gameplay / Game Over physics, lives, hitboxes, Retry/Home actions,
  saved data, selected pilots or public API contract is altered.

### Mandatory release gates

- [ ] Python source, privacy/security, dead-code and asset audits
- [ ] Gradle 9 / Android 36 JVM tests and debug APK + unsigned release APK/AAB
- [ ] Android 15 Play → first flap → lifecycle → Retry QA
- [ ] Android 16 API36 smoke
- [ ] New native 941×1672 Splash screenshot reviewed against original
- [ ] Home, Gameplay and Game Over visually regressions checked
- [ ] Screenshot ROI errors measured; any increase investigated and corrected
- [ ] Confirm no inaccessible native touch controls or progress regression

Do not claim 1:1 until verified pixel comparisons prove it. Release AAB/APK
remain unsigned until privately signed with the publisher's correct upload key.
Keep previous GitHub Releases intact.
