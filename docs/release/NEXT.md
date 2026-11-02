# Next Android release — v1.0.46 (build 176)

**Candidate only; not yet published.** This version is staged for BOPAVI PR #210 and must pass GitHub Actions, Android emulator QA, accessibility checks and reference screenshot review before any merge or release.

## Changes
- Keep Splash, Home and gameplay character selection consistent through one canonical pilot/sprite mapping (Bopi 0, Sova 9, Lisac 10).
- Improve the reference Home's raised 3D button depth, rounded enamel shape, clipped specular shine and thick borders.
- Render a dedicated 3D leaderboard podium instead of a generic fallback glyph.
- Prevent circular Home footer buttons from stretching to oval shapes; fit embossed labels and wallet controls to reference bounds.
- Honor reduced-motion preference for Home animated wings and press scaling.
- Update UI/character regression contracts and document acceptance for all nine supplied 941×1672 references.

## Pending validation
- Full Android test, Android Lint, APK/AAB build and emulator screenshot comparison with the original references.
- Visually inspect every page and actual touch function. **No 1:1 screenshot acceptance has been established.**
- This build does not add fictional gems, in-app purchase restoration, cloud backup, or payment-related features simply because they appear in artwork.

The published baseline remains **v1.0.45 (build 175)**.
