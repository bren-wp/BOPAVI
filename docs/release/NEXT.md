# Next Android release

## v1.0.27 — Responsive touch targets, consistent iconography and Bopi themed launcher (build 157)

- **All reachable screen states reviewed:** Home (pilot selection / Play / options / records / legal links), Flight HUD and native Pause, Results, Settings and Records dialogs. Preserve the original Bopi illustration and game rules; do not add redundant screens or fake counters.
- **Touch targets no longer shrink:** replace the container-wide scale transform with a fill-viewport native ScrollView. On normal viewports it fits without scrolling; genuinely short/split-screen windows can scroll to every action instead of scaling 48dp touch targets or clipping the bottom links.
- **Unified native icon family:** Play, Pause, Retry, Home, Exit, Options, Records and Results icons now use one consistent antialiased, device-independent Canvas drawable. Croatian content descriptions remain available to accessibility services. The actual Pause button remains a 56dp native touch target.
- **Original launcher art:** retain the owner-approved Bopi portrait unchanged for the colour adaptive launcher and legacy icon. Add Android 13+ themed monochrome icon **derived exclusively from its alpha mask**; no stock/vector impostor of the mascot.
- **Regression gates:** source UI contract, approved original mascot pixel-integrity check, compiler/Android lint, JVM tests and Android 15/16 emulator QA. The home responsive smoke checks Play, Options, Records, both legal links and all three pilot card hit targets at two display/density sizes.
- **Compatibility:** original package name `com.brendigo.bopavi`, offline gameplay and historical local save keys stay unchanged. Distribution remains debug APK plus **unsigned** release APK/AAB, Google Play and F-Droid preparation assets and SHA256SUMS.

**Visual-fidelity status:** the repository contains approved *Bopi character layers* and genuine CI captures, but not a complete, independently verifiable, owner-supplied full-screen 1:1 screenshot set for every UI state. No pixel-by-pixel match is claimed. `tools/compare_reference_ui.py` can compare full-resolution originals after all screen references are available. Physical phone review is still required.
