# Next Android release

## v1.0.34 — Active-flight rotation regression QA (build 164)

- Strengthen the Android 15 rotation test from an idle-preview check to a genuine first-flap flight check, using the existing debug-only capture gate.
- Verify that score, lives, pilot ability and the current GameView accessibility state remain identical across landscape-to-portrait rotation, and that the native Pause control stays visible and tappable within display bounds.
- Verify the same process remains alive and that the ongoing flight accepts the next genuine flap after rotation. Preserve all existing Android 15/16 UI, gameplay, large-font, R8 and release-integrity gates.
- Keep original gameplay physics, 480 × 800 logical coordinates, art, sound, offline privacy model and saved preferences unchanged. This is a verification-only source change, not a claim of a newly discovered production crash fix.

This candidate must not be published unless every required CI job succeeds. Physical-device testing and Google Play Console removal of advisories are not claimed.
