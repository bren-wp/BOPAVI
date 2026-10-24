# Next Android release

## v1.0.26 — Distinct pilot power icons and adaptive HUD labels (build 156)

- **Recognizable powers in flight:** original BOPAVI native HUD gains a small speed lightning bolt for Bopi, a feather for Sova and an outlined one-use shield for Lisac. The shield shows a check mark while charged and a strike when spent. These icons are painted using the existing Canvas Path and Paint, without any downloaded images, new SDKs or per-frame bitmap allocations.
- **More readable ability card:** the ability label uses the real available space between the icon and card's padded right edge. A measurable 15-unit preferred font shrinks only when the actual text cannot fit; a horizontal-scale fallback preserves the complete Croatian name, including longer states such as `LISAC · ŠTIT POTROŠEN`.
- **Better event readability:** lost/gained life and consumed-shield notice captions use the same measured, bounded typographic policy; card placement, notices and native Pause hit target remain unchanged.
- **Regression safety:** pure Kotlin tests validate caption fitting, invalid measurements, pictogram geometry and separation from the status label. A Python source contract enforces the real renderer wiring; Android visual emulator QA, Android 16 smoke tests and Gradle unit/lint remain release gates.
- **Backward-compatible, offline:** no change to `applicationId`, game rules, character physics, saved progress, screen navigation, advertising/tracking policy or the approved Bopi launcher icon. Unchanged GitHub distribution model: debug APK and unsigned release APK/AAB; publisher-held signing is not configured in this repository.

Manual verification of final icon legibility on compact, cutout and high-density devices remains recommended; CI visual captures are not a substitute for all physical devices.
