# BOPAVI — Android release acceptance checklist

**Last published release evidence:** v1.0.93 (build 223).
**Application:** com.brendigo.bopavi · BRENDIGO · https://brendigo.com.

## Production UI

- [ ] Home exposes exactly four accessible controls: PLAY, SKINS, SHOP, SETTINGS.
- [ ] PLAY reaches Loading and actual playable GameView without QA-only intent extras.
- [ ] Flight, collisions, score, Pause/Resume, Retry, Home and lifecycle transitions work.
- [ ] Shop has six real local upgrades with atomic wallet/inventory updates; Skins persist.
- [ ] Settings audio, legal controls, SAF backup, language and accessibility work.
- [ ] Progress, Leaderboard, Records, Achievements and Level Map remain absent as standalone pages.
- [ ] Android 15/16 UIAutomator/emulator tests and screenshots pass at actual densities.
- [ ] Compare authentic native PNGs against original owner WebPs per distinct state, without resizing; disclose RGB differences.
- [ ] Test TalkBack, large fonts, rotation and physical Android devices before parity/stability claims.

## Data, security and distribution

- [ ] Preserve v1–v6 historical JSON formats, saved coins, skins, purchases and scores.
- [ ] Test failed, concurrent, interrupted and externally edited SAF DocumentsProvider operations on physical devices.
- [ ] Audit Kotlin, manifest, intents, R8, asset licenses and transitive dependencies.
- [ ] No INTERNET permission, ad SDKs or new account requirement.
- [ ] Run Android Lint, JVM tests, R8 APK/AAB and checksum verification for the exact commit.
- [ ] Publish no release with stale three-action or no-PLAY claims; validate release notes before upload.
- [ ] Sign production distribution privately with publisher-approved key.
- [ ] Do not rewrite or delete historical GitHub releases.
