# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.51 — Honest splash progress and seated Game Over Bopi (build 181)

**Published:** 2026-11-06 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.51)

- Splash/Loading: during asynchronous level preparation, replace the 61–68% apparent prefilled progress bar with an indeterminate cyan scan; fill the blue enamel track only on the real preparation completion event. Keep the native offline artwork and loading accessibility state.
- Game Over: use a newly drawn local, noninteractive **sad seated classic Bopi** illustration instead of the flying portrait for the original blue aviator. Preserve the currently selected alternate-pilot portraits until dedicated approved seated images exist.
- Home and Game Over: place decorative wordmarks, character art, podiums and 3D reference controls on the original full 941×1672 grid when safe. Keep a real Android cutout/navigation-bar fallback for native touch targets on smaller or inset-heavy devices.
- Keep real Retry/Home click listeners and accessibility targets, true run stats, save data, gameplay physics, collision geometry, music and existing world progression unchanged.
- Add deterministic JVM tests for loading scan bounds, movement and completion, and source assertions for the seated Bopi route.

## v1.0.50 — Android 15 gameplay preflight lifecycle fix (build 180)

**Published:** 2026-11-03 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.50)

- Keep the merged Home Play, Skins, Shop and Settings compact 3D button layouts; no unrelated visual changes.
- Keep native Game Over Retry and Home controls and genuine saved statistics.
- Keep the top-right Home wallet HUD safe-inset fix.
- Fix async preflight: check actual Activity onResume/onPause instead of transient window focus, which can drop during UIAutomator and incorrectly return Home before first flap.
- Background/resume QA now retains an actual accessibility hierarchy and emulator screenshot on failure instead of suppressing or retrying a real application problem. No fake game state.
