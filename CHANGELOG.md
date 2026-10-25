# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.30 — Uniform native icons, Records/Results polish and touch accessibility (build 160)

**Published:** 2026-10-25 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.30)

- **Results screen:** replace platform-dependent star and strawberry emoji in personal-best summary with reusable antialiased BOPAVI STAR and BERRY pictograms, preserving genuine persisted best points and food.
- **Records dialog:** show a consistent native icon beside each of five real offline statistics (best points, food, obstacles, flights and flaps). Add new native GATE, FLIGHT and WING drawings; each row has one complete, spoken TalkBack label and value rather than a decorative emoji glyph.
- **Options and Records headers:** use consistent native SETTINGS/RECORDS pictograms; avoid emoji glyphs that change with device and font.
- **Pause:** use the actual PAUSE pictogram (not a trophy icon); improve accessible resume action label to "Nastavi let".
- **Buttons and legal links:** add bounded native ripple feedback and keep explicit 48dp clickable areas, real native input targets and Croatian accessibility descriptions.
- **Native icon correctness:** correctly compose red berry opacity and green foliage alpha with Drawable alpha; preserve colour filter and original approved Bopi launcher artwork, adaptive icon mask and three pilots.
- **QA:** update icon, layout and options regression contracts, add a cross-screen native stats accessibility contract; run full Android lint/unit/gameplay/visual QA on Android 15/16, generate five authentic app screenshots and preserve signed-key policy.
- **Compatibility:** retain original saved progress keys, score logic, collision physics, package name and all existing offline privacy/security behavior.

## v1.0.29 — Gameplay obstacle honesty, original Bopi launcher and safe UI (build 159)

**Published:** 2026-10-25 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.29)

- **Accurate obstacle silhouettes:** the far upper and lower rooted cliffs are constrained to each actual physical pillar shaft (54–104 world units), so scenery does not project outside the collision body. Rounded top/bottom obstacle lips retain their existing 7-unit left and 5-unit right overhang, matched by the existing swept collision exactly. Bird hitbox and game difficulty stay unchanged.
- **Variable obstacle quality:** world-specific rocky roots and decorative material continue to render at every size; source contract and JVM collision tests cover six real generated pillar widths, safe mid-corridors and collision at visible mouth edges.
- **Home and Results UI:** unified viewport padding for real system bars, gesture navigation and camera cutouts across Android API 26–36 (including the legacy insets path), while background art stays edge-to-edge.
- **Native pictograms:** give the fruit score icon a separately green stem and leaf over red fruit instead of a monochrome red blob.
- **App launcher:** make Bopi's authentic approved portrait slightly larger on legacy square launchers, without modifying its pixel content or changing the certified adaptive/themed launcher foreground safe size and original silhouette.
- **All-screen proof:** Android 15 CI now taps the real Pause button, checks `NASTAVI`, `PONOVO`, and `IZLAZ`, captures a distinct genuine Pause screen, resumes the same flight and then tests its final Results/Retry flow. The new Pause PNG joins the GitHub release and five-image Play/F-Droid submission kits, with SHA-256 verification.
- **CI and documentation:** add regression guards for rooted shaft visual geometry, cap-only collision shoulders, all six pillar widths, adaptive-icon provenance, legacy icon sizing and Android inset handling. Existing full static/security/audio/art audits, JVM tests and Android 15/16 emulator QA remain mandatory.
