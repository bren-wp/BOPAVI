# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.5 — Uninterrupted speed and consistent character artwork (build 135)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.5)

- Smooth horizontal flight speed across invisible procedural challenge-batch boundaries instead of instantly snapping to the next group's speed. Gate and berry collision sweeps now use the exact scroll displacement.
- Keep original 120 Hz semi-implicit physics substeps, fixed body collision circle, obstacle cadence, food collection, saved high scores and continuous incoming obstacles unchanged.
- Show the selected player (Bopi, Sova or Lisac) in Pause, matching Home, Flight and Results. No new menu, login, network permission, tracking or paid feature.
- Add focused JVM and source-level regression checks for boundary speed continuity, pause art selection and save compatibility. Make publisher-only e-mail/HTTPS links inside the complete offline legal notices interactive and enforce an exact legal-link allowlist.
- Carry forward complete, offline legal texts sourced verbatim from the owner's index.html (11 privacy / 12 terms sections with publisher contact); existing MIT licensing history remains preserved.
- CI and emulator tests must pass before publication. Physical battery profiling and Google Play store approval are not implied.

## v1.0.4 — Three animal pilots, refined terrain, legal links and power usage (build 134)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.4)

- Add original Bopi, Sova (owl) and Lisac (winged fox) as immediately selectable Home characters with unique body, animated wing and portrait sprites; retain historical saves.
- Replace the prominent privacy button with two compact, independently accessible offline native footer links for Privacy Policy and Terms of Use.
- Improve the natural forest green obstacle material and HUD contrast without widening or changing any collision geometry.
- Stop the Home screen's endless decorative animation and pace automatic gameplay redraws at approximately 60fps-class or 30fps with reduced-motion preference; keep immediate touch response and fixed-substep physics.
- Keep procedural hazards flowing continuously across internal generation boundaries; no level menus, artificial completion overlays or restart between obstacle groups.
- Expand CI source, legal/character and emulator contract checks. On-device battery consumption, physical crash-free operation, public HTTPS hosting and Play publication require separate verification.
