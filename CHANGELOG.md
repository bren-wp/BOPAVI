# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.46 — Splash/Home 3D UI and pilot consistency (build 176)

**Published:** 2026-11-02 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.46)

- Keep Splash, Home and gameplay character selection consistent through one canonical pilot/sprite mapping (Bopi 0, Sova 9, Lisac 10).
- Improve the reference Home's raised 3D button depth, rounded enamel shape, clipped specular shine and thick borders.
- Render a dedicated 3D leaderboard podium instead of a generic fallback glyph.
- Prevent circular Home footer buttons from stretching to oval shapes; fit embossed labels and wallet controls to reference bounds.
- Honor reduced-motion preference for Home animated wings and press scaling.
- Update UI/character regression contracts and document acceptance for all nine supplied 941×1672 references.
- Full Android test, Android Lint, APK/AAB build and emulator screenshot comparison with the original references.
- Visually inspect every page and actual touch function. **No 1:1 screenshot acceptance has been established.**
- This build does not add fictional gems, in-app purchase restoration, cloud backup, or payment-related features simply because they appear in artwork.

## v1.0.45 — Splash/Home responsive and fantasy environment (build 175)

**Published:** 2026-11-02 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.45)

- Maintain actual Android Splash/Home controls, stored coin/star balances, selected original pilot and offline gameplay.
- Home reference mode now falls back to the native adaptive layout for enlarged system fonts (1.3x and above); avoid fixed-grid overlap.
- JVM regression matrix covers nine size/orientation profiles and multiple font scaling factors.
- Android 15/16 emulator QA checks representative small, compact, reference, standard, tall and large screen sizes, density and rotation. Not every device/form factor can be physically emulated.
- Continue improving the separately rendered fantasy background; never bake buttons, currency or Bopi's interactive state into scenery.
- Owner 941×1672 reference and original emulator screenshots are the visual baseline, but a verified post-change 1:1 pixel match is still pending.
