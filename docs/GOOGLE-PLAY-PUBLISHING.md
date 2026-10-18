# BOPAVI — Google Play publishing guide (v0.2.21 / build 126)

Verified source on 2026-10-18: Android applicationId com.brendigo.bopavi, minSdk 26, targetSdk/compileSdk 36, versionName 0.2.21, versionCode 126. Latest tag https://github.com/bren-wp/BOPAVI/releases/tag/v0.2.21. GitHub main CI: https://github.com/bren-wp/BOPAVI/actions/runs/39085140513 (success). These facts **do not** prove Play Console approval or real-device crash freedom.

## Actual product for metadata

Accessible Android/iOS navigation: Home → one-button endless Flight → Pause (Continue / Retry / Exit) or Results (Retry / Home). One original Bopi, first biome, score, contact-collected red berries and local best records. There are **no navigable** character/world/level pickers, shop, profile, settings screen or user-accessible JSON import/export. Some old graphics, saved counters and backup code remain internally for compatibility. See docs/product/FEATURE-MATRIX.md and docs/product/CURRENT_SCREENS.md.

Use **only** docs/play/STORE-LISTING-hr-HR.md for the current Croatian Play listing. Keep screenshots truthful. No feature may be inferred from source assets alone.

## Required publisher steps

1. In https://play.google.com/console check authorized ownership, app ID, country availability, audience, category and currently uploaded versions.
2. Confirm current Google Play target API and developer policies (https://support.google.com/googleplay/android-developer/answer/11926878). Current code targets API 36; that does not waive any other compatibility or form requirement.
3. Obtain the **unsigned** release AAB from the matching GitHub release; the public APK is a debug build. Validate the matching version, release SHA, artifact hash and the full dependency/merged manifest.
4. On a trusted local machine sign with the **preexisting publisher-verified upload key** and check signing certificate and SHA-256. Do not create/replace a Play signing identity casually. Never put keystores, passwords or signed artifacts in public GitHub.
5. Upload the signed bundle to the chosen Play test track, verify there is no reused versionCode, complete app access, Ads, Content rating, Target audience, Data safety, and a working public HTTPS privacy-policy URL. Complete account-specific testing if required before applying for production.
6. Prepare real store icon (512×512), feature graphic (1024×500) and actual same-build screenshots. For v0.2.21 the release provides Android Home, Flight Preview, Flight Active and Results; preview is not active flight. No dedicated Pause screenshot is in the release manifest. Validate images on the real device/Play-delivered artifact before using them; do not use legacy premium screens.
7. Test first launch, hitboxes, contact-only berry collection, Pause/Results actions, orientation, inset/layout, audio, offline startup, crashes/ANRs and stored best records on supported devices. Read Pre-launch reports and fix blockers.
8. Submit and await independent Google review; verify the actual Play listing and install after approval. Neither GitHub Actions nor the public unsigned bundle is evidence of approval.

## Privacy and disclosure

Current AndroidManifest.xml has no INTERNET permission, and the build lists no analytics/ad SDK. Local score, berry record, preferences and compatible legacy save data are present; old export/import serialization in the code **does not imply a visible export/import button**. Confirm actual final Play package, all versions distributed, all libraries and any external data flows before declaring no collection or sharing. See docs/play/GOOGLE-PLAY-DATA-SAFETY.md. A public privacy policy and complete Data safety form are required even if no data is collected (except Google’s specific internal-only testing exemption).

Rules: https://support.google.com/googleplay/android-developer/answer/9898842 ; https://support.google.com/googleplay/android-developer/answer/10787469 ; https://support.google.com/googleplay/android-developer/answer/15191715.

See docs/play/RELEASE-CHECKLIST.md for evidence-based sign-off. No Play Console edits, certificates or signing keys are handled in this repository audit.
