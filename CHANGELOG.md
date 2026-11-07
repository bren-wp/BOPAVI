# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.55 — Gameplay hazards activate on first real flap (build 185)

**Published:** 2026-11-07 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.55)

- Before the first real flap, the 941×1672 Gameplay preview displays
- After the first actual player tap invokes `GameSimulation.flap()`,
- Pause and background/resume preserve active-flight obstacle rendering.
- Keep the existing 480×800 simulation coordinates, 18px bird hitbox,
- Add `FlightHazardPresentationTest` and a mandatory Python source
- Preserve all Splash, Home, Shop, Skins, Settings and Game Over native
- [ ] Privacy/security, dead-code, asset and source-contract audits pass.
- [ ] Kotlin/JVM and Android APK/AAB builds pass.
- [ ] Android 15 Play → first flap → pause → resume → Retry passes.
- [ ] Android 16 API36 smoke and navigation pass.
- [ ] Genuine emulator screenshots confirm that idle obstacles are hidden
- [ ] Recompare all four priority owner-reference screens and document

## v1.0.54 — Splash / Loading highest pixel discrepancy (build 184)

**Published:** 2026-11-07 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.54)

- Redraw the winged original BOPAVI logo using taller vector geometry
- Reposition the winged, selected native Bopi 512×512 sprite group on a
- Generate **Splash-only**, entirely offline `loadingback.png` from the
- Retain the real asynchronous preparation event and honest indeterminate
- No Gameplay / Game Over physics, lives, hitboxes, Retry/Home actions,
- [ ] Python source, privacy/security, dead-code and asset audits
- [ ] Gradle 9 / Android 36 JVM tests and debug APK + unsigned release APK/AAB
- [ ] Android 15 Play → first flap → lifecycle → Retry QA
- [ ] Android 16 API36 smoke
- [ ] New native 941×1672 Splash screenshot reviewed against original
- [ ] Home, Gameplay and Game Over visually regressions checked
- [ ] Screenshot ROI errors measured; any increase investigated and corrected
