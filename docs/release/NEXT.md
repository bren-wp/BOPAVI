# Next Android release

## v1.0.93 — Candidate: Game Over logo occlusion and visual QA diagnostics (build 223)

**CANDIDATE / NOT PUBLISHED.** This is a development iteration for one consolidated PR after #244 was merged.

- Fix the confirmed Game Over P2 review issue: keep the original measured 3D crown coordinates but render the crown behind the native BOPAVI logo so it cannot cover brand letters.
- Add a native-view source regression test to prevent the crown from returning above the wordmark, while preserving the real Retry/Home controls and gameplay-derived statistics.
- Isolate Shop's 0.8435 horizontal camera correction: Settings and Skins now use full-width portrait reference camera so their native row titles and selectable cards cannot inherit Shop's 16:9 compression.
- Add a regression source contract for Shop versus Settings/Skins camera selection and preserve all native touch controls.
- Enhance the strict original-WebP-versus-native-PNG RGB reporter with a non-masked 3×3 per-region diagnostic grid and the worst ninth per screen to prioritize the next UI correction.
- Extend synthetic test fixtures to check exact and one-pixel-different captures without fabricated emulator evidence.
- Preserve existing coins, saves, skin ownership, offline privacy, playable physics and all approved source references.

**Not verified:** pixel-perfect 1:1 is NOT achieved. The last completed v1.0.92 main CI had zero exact native/reference matches in 36 comparable variants; four Game Over B variants lacked matching native screenshots. New v1.0.93 emulator captures, Android 15/16 tests, lint/R8, screen-by-screen review and any signed Play distribution remain pending until the exact branch CI completes and results are reviewed. Do not merge or publish this candidate as fully validated 1:1.
