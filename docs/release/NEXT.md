# Next Android release

## v1.0.45 — Splash/Home responsive and fantasy environment (build 175)

- Maintain actual Android Splash/Home controls, stored coin/star balances, selected original pilot and offline gameplay.
- Home reference mode now falls back to the native adaptive layout for enlarged system fonts (1.3x and above); avoid fixed-grid overlap.
- JVM regression matrix covers nine size/orientation profiles and multiple font scaling factors.
- Android 15/16 emulator QA checks representative small, compact, reference, standard, tall and large screen sizes, density and rotation. Not every device/form factor can be physically emulated.
- Continue improving the separately rendered fantasy background; never bake buttons, currency or Bopi's interactive state into scenery.
- Owner 941×1672 reference and original emulator screenshots are the visual baseline, but a verified post-change 1:1 pixel match is still pending.

**Build 175 distribution:** unsigned APK/AAB may be generated for QA. Do not merge, sign or publish until all required checks pass.
