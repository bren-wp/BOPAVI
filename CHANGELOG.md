# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.89 — Owner-reference Home logo camera correction (build 219)

**Published:** 2026-11-16 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.89)

- Shrink and raise the actual native winged-logo ImageView based on the owner's 1080×1920 painted wordmark bounds (from approximately 840px wide in v1.0.88 toward approximately 560px). Keep the real logo vector and native ImageView; do not embed the owner screenshot in the app.
- Recolor the original six independent vector glyphs to the reference palette (B/O/P cyan, A/V/I warm gold) while preserving their contours, crown, wings and source-generated Android asset. Asset artwork changes require the full candidate build and genuine screenshot validation.
- Validate the wallet and painted logo using true 2-D non-overlap rather than an unnecessary vertical ordering rule. Preserve the four real PLAY/SKINS/SHOP/SETTINGS callbacks, original button slots and saved data.
- Add direct Kotlin/JVM camera geometry assertions; publish separate original-versus-real-Android RGB evidence across the four available viewport references. Full RGB 1:1 is **not** claimed.
- Continue in order: Home geometry and art, Skins, Shop (six real purchases), Settings, Game Over variants, Gameplay/Loading, Pause, Results and legal dialogs. Do not merge until source + Android 15/16 emulator tests succeed and real pixel measurements are reviewed.
- Obtain authentic candidate native 1080×1920 / 1080×2280 / 1080×2340 / 1080×2400 screenshots and compare against matching SHA-pinned owner originals.
- Verify visual appearance of the winged wordmark (font/palette) and sky art separately; camera-bound alignment alone cannot correct illustrated asset differences.
- Confirm accessibility, notches, font-scale, compact/landscape layouts, 48dp hitboxes, loading and transitions. Do not delete old saves or reference art.
- Physical devices, SAF reliability, per-asset rights, exhaustive security/dead-code and performance remain unverified.

## v1.0.88 — SAF recovery journal and fail-closed owner screenshot QA (build 218)

**Published:** 2026-11-15 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.88)

- Save the previous SAF document bytes in a synced, integrity-checked private recovery journal before beginning a potentially truncating provider write. Verify the completed bytes and try to restore the old backup on I/O failure; preserve a journal across process restart if recovery fails.
- Require readable SAF targets for recoverable overwrite and serialize old-document grant release behind writes. Do not modify the local v1–v6 JSON save schema or virtual wallet.
- Add JVM fault-injection tests for truncation, power-loss-style restart and stale destination; provider behavior and out-of-process writes remain to be tested on real devices.
- The owner archive **historically indexes 50 WebPs: 10 unique design states × 5 resolutions**. Four resolution sets (40 genuine, checksum-verified originals) are present. Require **at least one original per state** for the reference coverage gate; other resolution variants remain supplementary evidence. Keep the 50-entry source manifest unmodified for provenance. Record native Android PNG availability and every actual RGB difference honestly in a downloadable report, but do not allow absent optional viewport variants to fail otherwise functional CI.
- Restore a real production PLAY button (both full reference camera and compact Home) wired to `MainActivity.startGame()`. This implements the user's 2026-11-14 correction in `docs/product/ADR-0003-restore-home-play.md`.
- Fix the Android 16 Shop QA crash caused by double-escaped regex parsing of the real coin/inventory labels; add an actual visible-PLAY-to-GameView emulator test.
- **2026-11-14 Android15/16 QA regression:** the genuine XML hierarchy had all four Home actions including PLAY, but `tools/qa_android_emulator.sh` still demanded the obsolete three-action set twice. The source audit/build passed while API35/API36 smoked tests failed in run `43650096430`. Update both gates to require exactly PLAY, Skins, Shop, Settings, test a real PLAY tap through to a GameView before debug-only flags, and guard it with `tests/test_android_home_four_actions_smoke_contract.py`. This is a test-contract correction, not proof of zero RGB deviation.
- Remaining **visual audit gaps, not functional CI blockers**: 10 optional original 1440×3040 viewport variants, incomplete native Game Over B evidence, no exact pixel matches. **Release readiness blockers requiring independent review**: legal MIT/assets/Terms alignment and real SAF-provider/device tests.
- **Home original references depict no PLAY button**. The required new visible PLAY control intentionally differs from historical images. An owner-approved updated reference for the Home state is needed for a truthful 1:1 claim; it does **not** require creating five more identical design states merely because devices have different resolutions.
