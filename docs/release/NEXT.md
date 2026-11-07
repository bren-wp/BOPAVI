# Next Android release

## v1.0.57 — Splash and Home source-contour 3D fidelity (build 187)

**Pending QA / not yet published.** Scope limited to the two original 864×1536 owner Splash and Home references.

- Replace regular DejaVu BOPAVI title with owner-derived irregular native SVG wordmark contours, including separated blue i-dot, crown, wings, layered golden/cyan enamel and wood backing.
- Restore six hollow metallic gold rings in Splash's source perspective, including a partly cropped lower-left foreground ring. Maintain genuine Android stream preparation, accessible Loading label and indeterminate animation without fake completion.
- Deepen Home gold Play, purple Skins, green Shop and blue Settings buttons with authentic raised dark-navy sidewalls. Keep independent working clickable controls, real balance values, and three bottom shortcuts.
- Add owner 864×1536 versus native emulator 941×1672 comparison tooling and tests. Reference JPEGs are not bundled in the app or committed to this public repository.
- Baseline before changes: Splash 29.56% normalized RGB deviation and Home 27.21%. These numbers are **not** proof that the new revision is pixel-identical or within 10%; new on-device measurements and separately approved background/character layers are still required.
- Preserve other screens and gameplay physics. No new network permissions, purchases or fictional currencies.

**Acceptance:** source audit, Gradle/Kotlin tests, Android 15/16 emulator captures and navigation must pass. Compare both full screenshots and each ROI after the new render. Release APK/AAB remain unsigned for distribution; private signing is required for Google Play.
