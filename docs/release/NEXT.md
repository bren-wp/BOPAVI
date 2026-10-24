# Next Android release

## v1.0.28 — Results icon fidelity and device-safe UI (build 158)

- **Result cards:** fruit counts render a native red berry pictogram (not the gold trophy colour); score keeps a separate native gold star. Pictogram type and tint are provided directly, without guessing from an emoji string.
- **Navigation accessibility:** native return-home button speaks its actual displayed caption rather than an unrelated fixed description.
- **Full-bleed Android 15/16:** Result screen reserves both system bar and display cutout insets, consistent with the Home viewport. Request an initial inset dispatch immediately after attaching the native view. Preserve full-bleed background artwork.
- **Safety:** preserve the original Bopi launcher icon, 3 pilot portraits, actual game physics, saved data, offline privacy and native Pause controls.
- **QA:** source-contract regression test for result pictogram semantics and safe inset handling, complete Android lint/unit/build, Android 15 and Android 16 emulator QA and artifact checksums.

Release artifacts are GitHub debug APK, unsigned release APK and unsigned AAB; upload signing is publisher-only.
