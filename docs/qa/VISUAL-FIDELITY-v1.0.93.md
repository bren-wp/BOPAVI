# BOPAVI native UI fidelity — v1.0.93 candidate

This is an honest, traceable visual backlog. An Android emulator screenshot is **not** an approved original, and a passing source-contract test is **not** proof of RGB identity.

## Baseline: v1.0.92, merged main CI 43781801066 (16 November 2026)

- 40 verified original WebP viewport references across four tested resolutions.
- 36 native PNG comparisons are **different**; zero are exactly equal.
- 4 Game Over variant B native captures are unmapped/unverified.
- Representative 1080×2280 MAE (0–255): Shop 72.532437, Game Over 65.752977, Settings 62.739090, Home 62.585773, Skins 60.276248, Privacy 59.474921, Gameplay Ready 50.595225, Pause 48.789018, Active Gameplay 40.721362.
- Home reference has only three actions; the live Home intentionally retains the required fourth PLAY action. Exact match to that older source is impossible without a new owner-approved four-button reference.

## Current candidate modifications

1. **Game Over P2:** preserve reference-aligned crown and brand positions but paint crown below wordmark. Old sibling order painted an opaque 3D crown across BOPAVI logo. New source regression checks strict order.
2. **All ten owner states:** add 3×3 exact-pixel regions to existing full-frame RGB MAE and different-pixel reporting. Every region is cropped equally from the authentic original and matching native PNG; no screenshot is resized, fabricated or substituted.
3. Source and Python fixture checks must pass before interpreting a new emulator report.

## Remaining acceptance criteria

- Android 15 and 16 emulator smoke, true UIAutomator touch actions, pause/resume, save/restore and purchases.
- Native capture per 10 approved owner states and available viewport variants, no duplicate images or hidden missing states.
- Measure full RGB difference, per-third, and 3×3 region errors. Verify positions, text and 3D effect ordering against originals.
- Address every different state iteratively; a design mockup mismatch is not waived by changing fonts or disabling real controls.
- Separate physical device and signing validation before any production distribution.

CI may pass **coverage** while still reporting nonzero RGB mismatch: the acceptance standard for pixel-perfect remains zero differences only for genuinely comparable, owner-approved static scenes.
