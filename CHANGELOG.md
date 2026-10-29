# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.35 — Lives-only Flight, frontal pilots and harder polished obstacles (build 165)

**Published:** 2026-10-29 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.35)

- Remove the in-game Pause button completely and strip the score, food, power and status chips from the live flight canvas; only the centered lives and three actual heart indicators remain. Android system Back retains the accessible Continue/Retry/Exit dialog. Scoring and saved records remain functional on Results/Records.
- Improve Sova/Lisac's frontal flight by limiting full-body pitch equally for all pilots; the fox tail is recentered behind the body in original vector art while wings keep the existing flap cadence and the real 18-unit collider.
- Redraw collision-aligned obstacle lips with depth highlights and cleaner shafts, removing busy flecks without changing the actual collider. Improve difficulty after the introductory pillars via narrower/recovery openings and alternating 54–110-unit real pillar widths, while retaining the 164-unit minimum gate gap and the existing hard-mode 145-unit physical-opening floor.
- Upgrade the Android build toolchain to Android Gradle Plugin 9.5.0, Gradle 9.6.0 and JDK 17, keeping compileSdk/targetSdk 36 and minSdk 26.
- Remove retired gameplay overlay/status helpers and replace outdated source/UI contracts with lives-only and real system-Back checks. Keep original score/history/ability save data, offline privacy policy, motion-accessibility and lifecycle protections.

## v1.0.34 — Active-flight rotation regression QA (build 164)

**Published:** 2026-10-29 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.34)

- Strengthen the Android 15 rotation test from an idle-preview check to a genuine first-flap flight check, using the existing debug-only capture gate.
- Verify that score, lives, pilot ability and the current GameView accessibility state remain identical across landscape-to-portrait rotation, and that the native Pause control stays visible and tappable within display bounds.
- Verify the same process remains alive and that the ongoing flight accepts the next genuine flap after rotation. Preserve all existing Android 15/16 UI, gameplay, large-font, R8 and release-integrity gates.
- Keep original gameplay physics, 480 × 800 logical coordinates, art, sound, offline privacy model and saved preferences unchanged. This is a verification-only source change, not a claim of a newly discovered production crash fix.
