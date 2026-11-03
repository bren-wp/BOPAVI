# Next Android release

## v1.0.48 — Splash, Home, Gameplay and Game Over reference corrections (build 178)

**Candidate only — not published.** Built on the verified and released v1.0.47 baseline.

- **Splash:** raised blue-and-white Loading lettering with separate navy shadow; preserve live preparations, flying Bopi, winged BOPAVI mark, rings and enamel progress bar.
- **Home:** reduce the excessive spaces between the icon and heading on Play, Skins, Shop and Settings by measuring one proportional centered cluster per button. Pin real coin/star balances to the physical safe top on tall portrait screens instead of vertically centering the wallets with the rest of the reference-stage UI. No fictional gem balance or payments.
- **Gameplay:** raise avatar and the actual three-heart HUD on reference-sized displays without altering hitboxes, lives, enemy positions, physics, collection or save behavior.
- **Game Over:** display current earned values inside locally drawn 3D outlined result text (white/navy captions; gold/bronze values), retaining real distance, coins/food and stars/score. Functional Retry/Home and the existing 3D podium remain.

**Required acceptance before merge / release:** passing Android CI, debug/release APK and AAB compilation, no stale import warnings, responsive and cutout checks, native touch and accessibility tests, and emulator captures of the actual four screens at 941×1672 compared with the owner PNGs using `tools/compare_owner_ui.py` and `tools/qa_three_owner_references.py`.

**Remaining artwork fidelity:** The existing happy/portrait Bopi is not the distinct sad seated hero shown in the Game Over reference. Complex fantasy background elements are also not identical to the owner concept. Do not claim fully pixel-perfect 1:1 without original standalone assets and actual device captures.
