# BOPAVI — Android release acceptance checklist

**Last published release evidence:** v1.0.85 (build 215).
**Application:** com.brendigo.bopavi · BRENDIGO · https://brendigo.com.

## Production UI

- [ ] Home has exactly three accessible controls: Shop, Skins, Settings.
- [ ] No links, buttons, gestures or standalone screens for Progress, Leaderboard, Records, Achievements, Level Map or Play.
- [ ] Shop virtual purchases and Skins ownership stay transactional and offline.
- [ ] Settings privacy, legal controls, SAF backup, audio and accessibility work.
- [ ] Run Android 15/16 emulator UI and image checks at multiple densities.
- [ ] Check physical Android devices and TalkBack before claiming production parity.

## Data, security and distribution

- [ ] Preserve historical v1–v6 local save keys and virtual balances.
- [ ] No INTERNET permission, ad SDKs or new account requirement.
- [ ] Confirm 11 original emulator PNG captures, no removed-screen assets.
- [ ] Check Gradle Android Lint, JVM tests, R8 APK/AAB, SHA-256 manifests.
- [ ] Sign production distribution privately with publisher-approved key.
- [ ] Check Play Console policy, accessibility, data safety and physical-device stability.
- [ ] Never rewrite or delete older stable GitHub releases.
