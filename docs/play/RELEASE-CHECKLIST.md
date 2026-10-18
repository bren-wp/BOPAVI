# BOPAVI — Google Play / App Store release checklist

**Audited baseline:** GitHub Release v0.2.23, Android versionName 0.2.23 (build 128), versionCode 128, targetSdk 36, applicationId com.brendigo.bopavi. The latest main native CI finished green at https://github.com/bren-wp/BOPAVI/actions/runs/39173719556. No checklist item is automatically proof of a Play/App Store submission.

## Product and metadata

- [ ] Approve decision A or B in docs/product/ADR-0001-product-scope.md; while pending, market only the reachable four-state UX in FEATURE-MATRIX.md.
- [ ] Confirm final release commit, Android/iOS version parity and native CI checks; obtain actual log links for both platform builds and emulator flows.
- [ ] Verify store title, short/long descriptions, images and privacy disclosures against the **same installed build**; run python tests/test_store_claims.py.
- [ ] Use genuine Home / Flight Preview / Flight Active / Results screenshots from the correct version; preview is not active gameplay. Capture Pause separately if presented.
- [ ] Confirm no reachable skin/world/shop/Settings/import UI; do not advertise historical assets or serialization as user-facing options.
- [ ] Verify on physical supported Android devices: launch, first tap, score, one-shot berry pickup, Pause → Continue/Retry/Exit, Results → Replay/Home, back/gesture, screen sizes, audio and app interruption.
- [ ] Check crashes, ANR, battery, accessibility and RTL/language labels; confirm emulator/iOS simulator/device testing without asserting pixel-perfect reference parity.

## Google Play distribution (publisher-owned)

- [ ] Verify Play Console app, account permissions, age/target audience, IARC, Ads, app access, Data safety and privacy policy HTTPS address.
- [ ] Reconcile Data safety across **all currently distributed Play versions**, third-party SDKs and final merged manifest; closed-test tracks also require the form.
- [ ] Confirm Play App Signing and **existing** upload key certificate; make secure offline backups; never commit/share JKS or passwords.
- [ ] Sign the release AAB privately with the verified upload key and verify SHA-256, package, versionCode and certificate. GitHub's app-release.aab is UNSIGNED; app-debug.apk is not the release candidate.
- [ ] Check Play Console for used versionCodes, target API policy, device exclusions and required test access; do not invent current account entitlement.
- [ ] Upload to the authorized test track, review Pre-launch report and all metadata-policy warnings; complete any required production eligibility testing.
- [ ] Submit only after manual publisher authorization. Separately confirm Google review approval and installability. GitHub Release is not Play publication.

## iOS distribution (independent)

- [ ] Verify iOS signing/provisioning and Store Connect rights privately.
- [ ] Validate the same four-state navigation on real iPhone and supported simulators; capture authentic iOS screenshots, data disclosures and age ratings.
- [ ] Review/TestFlight/App Store approval separately; unsigned iOS device ZIP is not an App Store build.

Status: **publishing not verified**. Existing CI success does not verify upload signing, physical device behavior or Google/Apple store acceptance.
