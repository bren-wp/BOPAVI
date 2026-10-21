# Next Android release

## v1.0.15 — True music mute, audio-focus recovery and lower resource use (build 145)

- **Real 0% background-music mute:** muting the music slider pauses the soundtrack and promptly releases any held or pending Android audio-focus request. BOPAVI never plays a silent looping soundtrack or retains music focus just because the game is open.
- **Lazy audio decoding:** starting a world while music is muted no longer allocates a MediaPlayer. The local soundtrack decoder is initialized only when that active world actually needs audible music; independently enabled game sound effects continue to work.
- **Reliable unmute:** increasing volume from 0% in the same active game reuses the selected world and requests audio focus again before resuming. If focus is delayed, playback waits for a genuine gain event. Paused/backgrounded games do not restart sound merely because the slider changes.
- **Interruption safety:** delayed focus callbacks cannot revive music after the user muted it or the Activity released its focus. The existing permanent/transient loss handling and teardown remain intact.
- **Defensive mixer state:** unsupported NaN/Infinity input is normalized to silence; playable music and effects volumes remain clamped to 0–100% without changing saved user settings, the nine skins or game physics.
- **Maintainability:** extract Android-independent `MusicPlaybackPolicy.kt`, add focused Kotlin/JVM tests and a CI contract on the actual AudioManager/MediaPlayer integration. Keep the original eight offline soundtracks and sound-effect resources.
- **Distribution:** publish the existing ten-file Android release bundle: QA debug APK, privately signable unsigned release APK, unsigned Google Play AAB, four genuine screenshots, commit-pinned F-Droid submission ZIP, Play listing ZIP and SHA256SUMS.txt. No signing secrets are introduced.
- **Release gate:** require source/security/dead-code audit, Android Lint/JVM, Android 15 gameplay and Android 16 navigation emulator checks, authentic screenshots, APK/AAB signature checks, F-Droid source package and published SHA-256 integrity. Actual battery improvement and official F-Droid publication remain unverified until independently measured/approved.
