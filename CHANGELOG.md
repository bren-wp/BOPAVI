# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.29 — Gameplay obstacle honesty, original Bopi launcher and safe UI (build 159)

**Published:** 2026-10-25 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.29)

- **Accurate obstacle silhouettes:** the far upper and lower rooted cliffs are constrained to each actual physical pillar shaft (54–104 world units), so scenery does not project outside the collision body. Rounded top/bottom obstacle lips retain their existing 7-unit left and 5-unit right overhang, matched by the existing swept collision exactly. Bird hitbox and game difficulty stay unchanged.
- **Variable obstacle quality:** world-specific rocky roots and decorative material continue to render at every size; source contract and JVM collision tests cover six real generated pillar widths, safe mid-corridors and collision at visible mouth edges.
- **Home and Results UI:** unified viewport padding for real system bars, gesture navigation and camera cutouts across Android API 26–36 (including the legacy insets path), while background art stays edge-to-edge.
- **Native pictograms:** give the fruit score icon a separately green stem and leaf over red fruit instead of a monochrome red blob.
- **App launcher:** make Bopi's authentic approved portrait slightly larger on legacy square launchers, without modifying its pixel content or changing the certified adaptive/themed launcher foreground safe size and original silhouette.
- **All-screen proof:** Android 15 CI now taps the real Pause button, checks `NASTAVI`, `PONOVO`, and `IZLAZ`, captures a distinct genuine Pause screen, resumes the same flight and then tests its final Results/Retry flow. The new Pause PNG joins the GitHub release and five-image Play/F-Droid submission kits, with SHA-256 verification.
- **CI and documentation:** add regression guards for rooted shaft visual geometry, cap-only collision shoulders, all six pillar widths, adaptive-icon provenance, legacy icon sizing and Android inset handling. Existing full static/security/audio/art audits, JVM tests and Android 15/16 emulator QA remain mandatory.

## v1.0.28 — Android (build 158)

**Published:** 2026-10-24 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.28)

- **Result cards:** fruit counts render a native red berry pictogram (not the gold trophy colour); score keeps a separate native gold star. Pictogram type and tint are provided directly, without guessing from an emoji string.
- **Navigation accessibility:** native return-home button speaks its actual displayed caption rather than an unrelated fixed description.
- **Full-bleed Android 15/16:** Result screen reserves both system bar and display cutout insets, consistent with the Home viewport. Request an initial inset dispatch immediately after attaching the native view. Preserve full-bleed background artwork.
- **Safety:** preserve the original Bopi launcher icon, 3 pilot portraits, actual game physics, saved data, offline privacy and native Pause controls.
- **QA:** source-contract regression test for result pictogram semantics and safe inset handling, complete Android lint/unit/build, Android 15 and Android 16 emulator QA and artifact checksums.
