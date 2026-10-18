# BOPAVI v0.2.20 — dead code, lifecycle and security audit

## Scope
Reviewed Kotlin game engine, Android Activity and Canvas resource ownership,
SoundPool/MediaPlayer lifecycle, Swift/AVAudioPlayer, CI build and emulator
validation, release assets, and source-level navigation contracts.

## Removed
- Android MainActivity's unused `menuBackAction` callback/fallback and unused
  `small()` title helper after old submenus were deleted.
- Double Home PLAY click sound (the native action wrapper already plays one).
- Purchase sound preload in Android and iOS active runtime (Shop no longer exists).
- Unbounded Android idle/pause renderer scheduling (GPU/CPU work without a live flight).

## Retained intentionally; **not all legacy code is dead**
- Older encrypted? **No**: legacy save data is local non-sensitive preferences,
  not end-to-end encrypted. No such protection is claimed.
- Save-format v1–v6 compatibility, existing user record, language and art catalog:
  migration remains possible without clearing user data.
- Cross-world level engine, historical achievements/perk storage, older debug QA
  helpers and generated asset catalogs are isolated but retained for save/test
  compatibility. A destructive removal requires a separate versioned data
  migration and new artifact/resource contracts.

## Verified guardrails
- No Android INTERNET permission, WebView, payments or advertisement SDK is
  required by the four-view game.
- Emulator distribution uses an unsigned AAB, real gameplay screenshots and
  release publication gated on Android/iOS CI; generated assets must match
  platform parity requirements.
- The API36 image must contain verified system.img bytes; fallback is not an
  untested skip of the Android 16 smoke job.
- Reactivation after device interruption does not resume live flight physics
  without user action; blank idle previews no longer repeatedly invalidate.
- RenderThread-safe memory release: drop references on detach; do not manually
  recycle bitmap memory while a GPU display list might reference it.

## Known limitations and evidence
- GitHub CI logs alone cannot identify a user-device runtime crash without its
  stack trace and device/version steps. Do not label the emulator's SDK image
  download failure as an app crash.
- This is a focused lifecycle/source + CI audit, not exhaustive proof that
  every retained legacy symbol is reachable or every physical-device GPU fault
  is eliminated. Further Android Studio Memory Profiler and actual device logs
  are needed to validate low-memory devices.
