# Next Android release

## v1.0.58 — Splash and Home deeper owner-reference 3D artwork (build 188)

**Pending Android QA and owner-reference pixel comparison — not yet published.**

- Splash and Home only: replace a rough warm BOPA letter silhouette with six much more accurate source-contour vector paths, including independently hollow counters, preserving the existing crown, wings and native vector layers.
- Splash: relocate the gold/navy/cyan winged loading track and raised caption downward by 32 source px. The cyan highlight is now a broad 66% *moving indeterminate scanner*, **not** a fake readiness percentage. The sweep begins relative to this view's creation; only successful level preparation fills the complete track.
- Home: three bottom shortcut medallions gain reference-dark navy enamel centres and gold sidewalls. Their embossed white captions have smaller shadow offsets and accurately raised strokes.
- Original blue Bopi on Splash/Home receives independently drawn native red flying scarf, gold crown clasp and second cyan aviator lens. Existing three sprite layers and animated wings remain active. Other skins, game characters, hitboxes and gameplay stay untouched.
- Offline 900×1600 fantasy landscape now has translucent, painterly stacked cloud banks at the top, sides and distance. The still-inexact source towers and waterfalls are not replaced with a flattened screenshot.
- Baseline measured from published **v1.0.57** native Android 941×1672 screenshots against two user-owned 864×1536 JPEG originals, resampled for diagnostic comparison only: Splash **70.673/255 = 27.715%** normalized RGB MAE, Home **64.734/255 = 25.386%**. These are baseline values, **not measured results for v1.0.58** and not 1:1.
- Every major button, route, saved currency, audio control, indeterminate loading and TalkBack interaction remains truly native; no extra network permission, online purchases or fictional game scores.

**QA gate:** deterministic unit/source checks, Gradle lint/build, real Android 15/16 emulator smoke tests and screenshots, then owner-to-native global and regional RGB MAE. Do not claim exact parity or under 10% until independently verified. Debug APK is for testing; unsigned release APK/AAB require private signing before publishing to Google Play.
