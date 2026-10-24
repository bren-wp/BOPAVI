# Next Android release

## v1.0.29 — Gameplay obstacle honesty, original Bopi launcher and safe UI (build 159)

- **Accurate obstacle silhouettes:** the far upper and lower rooted cliffs are constrained to each actual physical pillar shaft (54–104 world units), so scenery does not project outside the collision body. Rounded top/bottom obstacle lips retain their existing 7-unit left and 5-unit right overhang, matched by the existing swept collision exactly. Bird hitbox and game difficulty stay unchanged.
- **Variable obstacle quality:** world-specific rocky roots and decorative material continue to render at every size; source contract and JVM collision tests cover six real generated pillar widths, safe mid-corridors and collision at visible mouth edges.
- **Home and Results UI:** unified viewport padding for real system bars, gesture navigation and camera cutouts across Android API 26–36 (including the legacy insets path), while background art stays edge-to-edge.
- **Native pictograms:** give the fruit score icon a separately green stem and leaf over red fruit instead of a monochrome red blob.
- **App launcher:** make Bopi's authentic approved portrait slightly larger on legacy square launchers, without modifying its pixel content or changing the certified adaptive/themed launcher foreground safe size and original silhouette.
- **CI and documentation:** add regression guards for rooted shaft visual geometry, cap-only collision shoulders, all six pillar widths, adaptive-icon provenance, legacy icon sizing and Android inset handling. Existing full static/security/audio/art audits, JVM tests and Android 15/16 emulator QA remain mandatory.

No app identity, save-data, physics, networking, tracking, billing or signing-policy changes. Android release APK and AAB distributed by GitHub remain unsigned; the debug APK is for direct installation and testing.
