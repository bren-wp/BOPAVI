# BOPAVI — Android / Google Play publishing

**Last published GitHub baseline:** v1.0.0 (build 130).
**Current distribution branch:** Android-only. The Kotlin app ID remains
`com.brendigo.bopavi` and the publisher is Brendigo (https://brendigo.com).

The product currently offers Home, tap-to-fly gameplay, pause/restart, results,
contact-collected berries and local personal records. Do not advertise
removed world, level, skin, shop, currency or settings screens merely because
some legacy data fields or graphics still exist.

## Android signing and Google Play requirements

1. Verify versionName/versionCode from android/app/build.gradle.kts and that
   versionCode exceeds the highest previously uploaded Play artifact.
2. Use the release AAB built from a passing main commit. GitHub publishes an
   unsigned AAB; **publisher must sign it with the existing protected upload key**.
   The debug APK is not a production signing substitute.
3. Independently verify application ID, signing certificate fingerprint,
   SHA-256, target SDK and content before Play submission.
4. Complete Play Console store listing, app content, target audience, data
   safety, IARC, ads declaration and privacy-policy URL truthfully.
5. Use only authentic screenshots captured from the same installed Android build.
   Never use a concept image as an actual in-app capture.
6. Verify install/start/gameplay/collision/pause/results on real supported
   Android devices, including screen cutouts, interrupted audio and memory.
7. Review the Play Console pre-launch report and policy messages. Submission
   and approval require the authorized account owner.

No ad, analytics, tracking, account or network backend is required by this
native offline game. Do not add extra permissions or external SDKs without
an explicit, reviewed need. GitHub Release success does not imply Play approval.
