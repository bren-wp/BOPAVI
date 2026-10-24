# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.28 — Results icon fidelity and device-safe UI (build 158)

**Published:** 2026-10-24 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.28)

- **Result cards:** fruit counts render a native red berry pictogram (not the gold trophy colour); score keeps a separate native gold star. Pictogram type and tint are provided directly, without guessing from an emoji string.
- **Navigation accessibility:** native return-home button speaks its actual displayed caption rather than an unrelated fixed description.
- **Full-bleed Android 15/16:** Result screen reserves both system bar and display cutout insets, consistent with the Home viewport. Request an initial inset dispatch immediately after attaching the native view. Preserve full-bleed background artwork.
- **Safety:** preserve the original Bopi launcher icon, 3 pilot portraits, actual game physics, saved data, offline privacy and native Pause controls.
- **QA:** source-contract regression test for result pictogram semantics and safe inset handling, complete Android lint/unit/build, Android 15 and Android 16 emulator QA and artifact checksums.

## v1.0.27 — Responsive touch targets, consistent iconography and Bopi themed launcher (build 157)

**Published:** 2026-10-24 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.27)

- **All reachable screen states reviewed:** Home (pilot selection / Play / options / records / legal links), Flight HUD and native Pause, Results, Settings and Records dialogs. Preserve the original Bopi illustration and game rules; do not add redundant screens or fake counters.
- **Touch targets no longer shrink:** replace the container-wide scale transform with a fill-viewport native ScrollView. On normal viewports it fits without scrolling; genuinely short/split-screen windows can scroll to every action instead of scaling 48dp touch targets or clipping the bottom links.
- **Unified native icon family:** Play, Pause, Retry, Home, Exit, Options, Records and Results icons now use one consistent antialiased, device-independent Canvas drawable. Croatian content descriptions remain available to accessibility services. The actual Pause button remains a 56dp native touch target.
- **Original launcher art:** retain the owner-approved Bopi portrait unchanged for the colour adaptive launcher and legacy icon. Add Android 13+ themed monochrome icon **derived exclusively from its alpha mask**; no stock/vector impostor of the mascot.
- **Regression gates:** source UI contract, approved original mascot pixel-integrity check, compiler/Android lint, JVM tests and Android 15/16 emulator QA. The home responsive smoke checks Play, Options, Records, both legal links and all three pilot card hit targets at two display/density sizes.
- **Compatibility:** original package name `com.brendigo.bopavi`, offline gameplay and historical local save keys stay unchanged. Distribution remains debug APK plus **unsigned** release APK/AAB, Google Play and F-Droid preparation assets and SHA256SUMS.
