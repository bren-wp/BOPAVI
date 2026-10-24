# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.26 — Distinct pilot power icons and adaptive HUD labels (build 156)

**Published:** 2026-10-24 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.26)

- **Recognizable powers in flight:** original BOPAVI native HUD gains a small speed lightning bolt for Bopi, a feather for Sova and an outlined one-use shield for Lisac. The shield shows a check mark while charged and a strike when spent. These icons are painted using the existing Canvas Path and Paint, without any downloaded images, new SDKs or per-frame bitmap allocations.
- **More readable ability card:** the ability label uses the real available space between the icon and card's padded right edge. A measurable 15-unit preferred font shrinks only when the actual text cannot fit; a horizontal-scale fallback preserves the complete Croatian name, including longer states such as `LISAC · ŠTIT POTROŠEN`.
- **Better event readability:** lost/gained life and consumed-shield notice captions use the same measured, bounded typographic policy; card placement, notices and native Pause hit target remain unchanged.
- **Regression safety:** pure Kotlin tests validate caption fitting, invalid measurements, pictogram geometry and separation from the status label. A Python source contract enforces the real renderer wiring; Android visual emulator QA, Android 16 smoke tests and Gradle unit/lint remain release gates.
- **Backward-compatible, offline:** no change to `applicationId`, game rules, character physics, saved progress, screen navigation, advertising/tracking policy or the approved Bopi launcher icon. Unchanged GitHub distribution model: debug APK and unsigned release APK/AAB; publisher-held signing is not configured in this repository.

## v1.0.25 — Varied harder pillars, modern HUD and Bopi-faithful pilots (build 155)

**Published:** 2026-10-24 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.25)

- **More challenging real obstacles:** the first two intro pillars keep their original widths; subsequent shafts follow a deterministic mixture of 54, 68, 74, 80, 92 and 104 logical units. Normal corridor mouths alternate narrow and recovery patterns after the warmup rather than remaining almost constant, but never shrink below 164 units before difficulty/pulses, preserving the proven >=145-unit hard-mode opening and the unchanged 18-unit bird collider. Both the Android Canvas renderer and swept pillar-collision use the same physical `Gate.width` and `LevelEngine.opening`; no fake cosmetic obstacles.
- **World variety:** true tall/short existing contours and moving gates stay in use. The mixed slimmer and wider column shafts produce visibly different traversal windows without changing the deterministic seed signature, stream seams or original save data. New tests cover exact width patterns, true rendering/collision coupling, min/max openings and reachability.
- **Consistent forward flight:** all 11 existing pilot art layers now share one front-facing pitch and flap policy. Large sideways tilts are limited to a gentle -14° to +22° range, using a common motion function for Bopi, Sova, Lisac and every recoloured/alternative pilot. The original approved sprite colours, wings, face and 18-unit hitbox are not stretched or rewritten. Reduced motion uses a stable orientation.
- **Modern premium HUD:** the existing user-reviewed placements of score, berries, 3/3 hearts, ability and native Pause are preserved; cards receive a cleaner dark translucent surface, a lighter single border and subtler corner shape. No full-screen overlays, no new bitmaps/shaders in `onDraw` and no changes to live life/guard values.
- **Faithful app icon:** the adaptive launcher foreground is now **only** the SHA-256-verified owner's approved Bopi winged portrait, centred in the Android safe area. Remove decorative concentric rings and invented halo shapes covering the mascot. The legacy full-bleed launcher uses the *same exact original Bopi pixels* at a larger scale over the existing original world artwork. New pixel-identity checks reject any changed portrait or generic alternate face.
- **Safety and QA:** keep game physics, scores, mode-specific advantages, collectible rewards, offline privacy, device permissions, signed-versus-unsigned publishing model and saved progress unchanged. Full Kotlin/JVM tests, world/obstacle fairness, source/dead-code audit, regenerated approved graphics, Android Lint, API35 gameplay/screenshots, API36 navigation and release SHA-256 verification remain mandatory. Human playtesting on physical devices remains necessary to judge subjective difficulty and final art quality; passing CI does not imply Play/F-Droid acceptance.
