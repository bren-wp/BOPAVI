# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.50 — Android 15 gameplay preflight lifecycle fix (build 180)

**Published:** 2026-11-03 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.50)

- Keep the merged Home Play, Skins, Shop and Settings compact 3D button layouts; no unrelated visual changes.
- Keep native Game Over Retry and Home controls and genuine saved statistics.
- Keep the top-right Home wallet HUD safe-inset fix.
- Fix async preflight: check actual Activity onResume/onPause instead of transient window focus, which can drop during UIAutomator and incorrectly return Home before first flap.
- Background/resume QA now retains an actual accessibility hierarchy and emulator screenshot on failure instead of suppressing or retrying a real application problem. No fake game state.

## v1.0.48 — Splash, Home, Gameplay and Game Over reference corrections (build 178)

**Published:** 2026-11-03 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.48)

- **Splash:** raised blue-and-white Loading lettering with separate navy shadow; preserve live preparations, flying Bopi, winged BOPAVI mark, rings and enamel progress bar.
- **Home:** reduce the excessive spaces between the icon and heading on Play, Skins, Shop and Settings by measuring one proportional centered cluster per button. Pin real coin/star balances to the physical safe top on tall portrait screens instead of vertically centering the wallets with the rest of the reference-stage UI. No fictional gem balance or payments.
- **Gameplay:** raise avatar and the actual three-heart HUD on reference-sized displays without altering hitboxes, lives, enemy positions, physics, collection or save behavior.
- **Game Over:** display current earned values inside locally drawn 3D outlined result text (white/navy captions; gold/bronze values), retaining real distance, coins/food and stars/score. Functional Retry/Home and the existing 3D podium remain.
