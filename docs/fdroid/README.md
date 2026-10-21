# BOPAVI — F-Droid distribution

BOPAVI's repository has an MIT `LICENSE` and publicly available Kotlin/Android source.
F-Droid inclusion is **not automatic** and has not yet been approved or verified.

## GitHub Release contents

- `app-release-unsigned.apk` — release-mode APK **for signing**, not installable unsigned.
- `app-release.aab` — **unsigned** Android App Bundle to sign with the existing Google Play upload key.
- `app-debug.apk` — debug-signed APK for QA only, **not** an F-Droid release binary.
- `BOPAVI-FDroid-submission-v<version>.zip` — candidate F-Droid build recipe, Croatian metadata, real Android screenshots, copyright/license, instructions and exact source commit.
- `SHA256SUMS.txt` — hashes for all named downloadable distribution files.
- GitHub's automatic source ZIP/tarball points to the corresponding tagged source.

## Official F-Droid submission workflow

1. Verify all code, sound, icons, fonts and artwork may be redistributed as FOSS or under F-Droid-compatible asset licenses. The MIT root license alone does not prove ownership of externally sourced resources.
2. Download the release's **submission ZIP**, inspect `metadata/com.brendigo.bopavi.yml`, and verify its `Builds.commit` equals the immutable source commit of that release.
3. Submit a merge request adding that metadata to [fdroiddata](https://gitlab.com/fdroid/fdroiddata). F-Droid builds and signs from source and verifies FLOSS dependencies. Never submit the debug APK as a production binary.
4. Confirm `python3-pil` and `python3-cairosvg` are available in the Debian build environment, that generated images/audio build from source, and that Gradle 8.11.1 / Android API 36 build on F-Droid's infrastructure. The recipe is a candidate until `fdroid build` passes.
5. Attach the real screenshots from the ZIP under the upstream fastlane paths for metadata discovery. The F-Droid maintainers determine acceptance and the final signing method.

The recipe generated at publishing time is pinned to a **40-character Git commit SHA**, not a moving branch or arbitrary tag. The release package intentionally does **not** claim an accepted F-Droid listing.

## Private signing of Google Play AAB

Use the **existing** upload keystore already enrolled for `com.brendigo.bopavi`. Never commit it or its password:

```sh
jarsigner -keystore /private/path/upload-keystore.jks \
  -signedjar BOPAVI-google-play-upload-signed.aab app-release.aab YOUR_EXISTING_ALIAS
jarsigner -verify -verbose -certs BOPAVI-google-play-upload-signed.aab
```

Do not use a newly generated key for an existing Google Play app unless Google Play has explicitly registered it as the upload key.

## Private APK signing for direct distribution

An unsigned APK is not installable. Align **before** signing with Android build tools:

```sh
zipalign -f -p 4 app-release-unsigned.apk app-release-aligned.apk
apksigner sign --ks /private/path/your-distribution-keystore.jks \
  --out BOPAVI-release-signed.apk app-release-aligned.apk
apksigner verify --verbose --print-certs BOPAVI-release-signed.apk
```

The signed APK must use the **same app-signing certificate** as any existing APK installation it is intended to update. Google Play's upload key can differ from the app-signing key; do not assume it is suitable for a sideloaded APK update.

F-Droid documentation:
- https://f-droid.org/docs/Inclusion_Policy/
- https://f-droid.org/docs/Build_Metadata_Reference/
- https://f-droid.org/docs/Reproducible_Builds/
