# BOPAVI — Android release acceptance checklist

**Last published release evidence:** v1.0.13 (build 143).
**Application:** `com.brendigo.bopavi` · Brendigo · https://brendigo.com.

## Product and Android QA

- [ ] Confirm latest intended Gradle versionName/versionCode and no versionCode reuse.
- [ ] Review all changes in the only development PR and confirm Android-only files.
- [ ] Pass Android Lint, Kotlin/JVM physics/collision tests and stable Android builds.
- [ ] Pass emulator API 35/36 smoke and real gameplay flows, including back/pause.
- [ ] Verify near misses, true body/rock contacts, food pickup, score and save restores.
- [ ] Compare Home/Flight/Pause/Results against actual approved user references.
- [ ] Validate short/tall/wide screen layouts, cutouts, text clipping and touch targets.
- [ ] Run a physical-device regression for crashes, ANRs, battery and audio focus.
- [ ] Confirm no advertised UI feature lacks an accessible route.
- [ ] Verify genuine same-build screenshots and separate Play listing asset archive.

## Privacy, signing and release

- [ ] Confirm no INTERNET permission, tracking, ads, purchases or unnecessary SDKs.
- [ ] Verify both 48dp+ native legal footer links, complete 11-section privacy and 12-section terms text matched to the owner index.html, plus public HTTPS policy URL.
- [ ] Ensure current Croatian-only screenshots and store listing truly depict the signed candidate build.
- [ ] Preserve app ID, all local save keys and compatibility with installed builds.
- [ ] Ensure release AAB/QA artefact checksums pass; GitHub AAB remains unsigned.
- [ ] Sign privately with the approved Google Play upload key.
- [ ] Complete Data safety, privacy, content, age and target SDK declarations.
- [ ] Confirm current Play track versionCode and publisher authorization.
- [ ] Only after a **successful public stable release**, synchronize CHANGELOG
      and current release docs from the GitHub published-release inventory.
- [ ] Preserve every historical stable v1+ GitHub release without rewriting assets.
