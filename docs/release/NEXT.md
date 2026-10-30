## v1.0.39 — Native reference Loading screen (build 169)

Screen 2 of the nine owner-provided 941 × 1672 portrait references.

- Add native Canvas-based `FlightLoadingView` for the genuine flight preparation step, using the original locally bundled BOPAVI logo, selected bird and fantasy-island sky.
- Reconstruct the large center hero, decorative collectible coins, golden winged frame, cyan progress track, and accessible native loading label. Fit uniformly on tall/compact devices with a center-cropped backdrop; no external service or fake wallet.
- Only mark the track complete after deterministic level generation succeeds; do not fabricate intermediate percentages, add an arbitrary timer, or display a second OS startup splash.
- Generate levels asynchronously before entering the existing GameView, with generation-token cancellation if Back returns Home, process is destroyed or the Activity loses focus. Consume purchased run perks only after successful loading; existing save keys and flight physics are untouched.
- Add an explicit emulator-only loading preview intent for reliable screenshot and accessibility QA; the real loading view uses the same code.
- Keep Gradle 9.6.0, Android Gradle Plugin 9.5.0, target API 36; verify Android 15/16 device QA, native Lint, tests, dead-code audit, and release checks.

**Visual coverage:** Accurate structural layout based on the supplied reference, but not a certified pixel-by-pixel duplicate: the reference's flying-bird and landscape artwork are not present as independent editable production layers. Original BOPAVI imagery is used rather than rasterizing the reference as a fake screen. Only release with all CI gates green.
