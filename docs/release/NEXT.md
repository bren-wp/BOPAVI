# Next Android release — under review

## v1.0.88 — SAF recovery journal and fail-closed owner screenshot QA (build 218)

**CANDIDATE / NOT PUBLISHED.** Draft PR #236 requires source, unit, Android15/16 emulator, security and SAF-provider verification. Design reference coverage is measured per distinct owner design state (currently 10), not per combination of state and screen resolution. Pixel-perfect parity is reported separately and is not the functional CI merge gate; it must never be claimed without measurement.

- Save the previous SAF document bytes in a synced, integrity-checked private recovery journal before beginning a potentially truncating provider write. Verify the completed bytes and try to restore the old backup on I/O failure; preserve a journal across process restart if recovery fails.
- Require readable SAF targets for recoverable overwrite and serialize old-document grant release behind writes. Do not modify the local v1–v6 JSON save schema or virtual wallet.
- Add JVM fault-injection tests for truncation, power-loss-style restart and stale destination; provider behavior and out-of-process writes remain to be tested on real devices.
- The owner archive **historically indexes 50 WebPs: 10 unique design states × 5 resolutions**. Four resolution sets (40 genuine, checksum-verified originals) are present. Require **at least one original per state** for the reference coverage gate; other resolution variants remain supplementary evidence. Keep the 50-entry source manifest unmodified for provenance. Record native Android PNG availability and every actual RGB difference honestly in a downloadable report, but do not allow absent optional viewport variants to fail otherwise functional CI.
- Restore a real production PLAY button (both full reference camera and compact Home) wired to `MainActivity.startGame()`. This implements the user's 2026-11-14 correction in `docs/product/ADR-0003-restore-home-play.md`.
- Fix the Android 16 Shop QA crash caused by double-escaped regex parsing of the real coin/inventory labels; add an actual visible-PLAY-to-GameView emulator test.
- **2026-11-14 Android15/16 QA regression:** the genuine XML hierarchy had all four Home actions including PLAY, but `tools/qa_android_emulator.sh` still demanded the obsolete three-action set twice. The source audit/build passed while API35/API36 smoked tests failed in run `43650096430`. Update both gates to require exactly PLAY, Skins, Shop, Settings, test a real PLAY tap through to a GameView before debug-only flags, and guard it with `tests/test_android_home_four_actions_smoke_contract.py`. This is a test-contract correction, not proof of zero RGB deviation.

- Remaining **visual audit gaps, not functional CI blockers**: 10 optional original 1440×3040 viewport variants, incomplete native Game Over B evidence, no exact pixel matches. **Release readiness blockers requiring independent review**: legal MIT/assets/Terms alignment and real SAF-provider/device tests.
- **Home original references depict no PLAY button**. The required new visible PLAY control intentionally differs from historical images. An owner-approved updated reference for the Home state is needed for a truthful 1:1 claim; it does **not** require creating five more identical design states merely because devices have different resolutions.

Unsigned debug/release APK and AAB artifacts may be built for QA. Do not represent any artifact as publisher-signed or overwrite v1.0.87.
