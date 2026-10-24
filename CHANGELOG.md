# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.27 — Responsive touch targets, consistent iconography and Bopi themed launcher (build 157)

**Published:** 2026-10-24 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.27)

- **All reachable screen states reviewed:** Home (pilot selection / Play / options / records / legal links), Flight HUD and native Pause, Results, Settings and Records dialogs. Preserve the original Bopi illustration and game rules; do not add redundant screens or fake counters.
- **Touch targets no longer shrink:** replace the container-wide scale transform with a fill-viewport native ScrollView. On normal viewports it fits without scrolling; genuinely short/split-screen windows can scroll to every action instead of scaling 48dp touch targets or clipping the bottom links.
- **Unified native icon family:** Play, Pause, Retry, Home, Exit, Options, Records and Results icons now use one consistent antialiased, device-independent Canvas drawable. Croatian content descriptions remain available to accessibility services. The actual Pause button remains a 56dp native touch target.
- **Original launcher art:** retain the owner-approved Bopi portrait unchanged for the colour adaptive launcher and legacy icon. Add Android 13+ themed monochrome icon **derived exclusively from its alpha mask**; no stock/vector impostor of the mascot.
- **Regression gates:** source UI contract, approved original mascot pixel-integrity check, compiler/Android lint, JVM tests and Android 15/16 emulator QA. The home responsive smoke checks Play, Options, Records, both legal links and all three pilot card hit targets at two display/density sizes.
- **Compatibility:** original package name `com.brendigo.bopavi`, offline gameplay and historical local save keys stay unchanged. Distribution remains debug APK plus **unsigned** release APK/AAB, Google Play and F-Droid preparation assets and SHA256SUMS.

## v1.0.26 — Distinct pilot power icons and adaptive HUD labels (build 156)

**Published:** 2026-10-24 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.26)

- **Recognizable powers in flight:** original BOPAVI native HUD gains a small speed lightning bolt for Bopi, a feather for Sova and an outlined one-use shield for Lisac. The shield shows a check mark while charged and a strike when spent. These icons are painted using the existing Canvas Path and Paint, without any downloaded images, new SDKs or per-frame bitmap allocations.
- **More readable ability card:** the ability label uses the real available space between the icon and card's padded right edge. A measurable 15-unit preferred font shrinks only when the actual text cannot fit; a horizontal-scale fallback preserves the complete Croatian name, including longer states such as `LISAC · ŠTIT POTROŠEN`.
- **Better event readability:** lost/gained life and consumed-shield notice captions use the same measured, bounded typographic policy; card placement, notices and native Pause hit target remain unchanged.
- **Regression safety:** pure Kotlin tests validate caption fitting, invalid measurements, pictogram geometry and separation from the status label. A Python source contract enforces the real renderer wiring; Android visual emulator QA, Android 16 smoke tests and Gradle unit/lint remain release gates.
- **Backward-compatible, offline:** no change to `applicationId`, game rules, character physics, saved progress, screen navigation, advertising/tracking policy or the approved Bopi launcher icon. Unchanged GitHub distribution model: debug APK and unsigned release APK/AAB; publisher-held signing is not configured in this repository.
