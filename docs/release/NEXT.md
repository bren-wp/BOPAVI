# Next Android release

## v1.0.50 — Android 15 gameplay preflight lifecycle fix (build 180)

**Candidate only, not yet published.** Post-merge hotfix following the unsuccessful 1.0.49 main-branch QA. Version 1.0.49 was not released. The published baseline is v1.0.48.

- Keep the merged Home Play, Skins, Shop and Settings compact 3D button layouts; no unrelated visual changes.
- Keep native Game Over Retry and Home controls and genuine saved statistics.
- Keep the top-right Home wallet HUD safe-inset fix.
- Fix async preflight: check actual Activity onResume/onPause instead of transient window focus, which can drop during UIAutomator and incorrectly return Home before first flap.
- Background/resume QA now retains an actual accessibility hierarchy and emulator screenshot on failure instead of suppressing or retrying a real application problem. No fake game state.

**Before release:** run the full Android 15 background/rotation gameplay suite, API 36 smoke, source/security audit, Kotlin tests, and unsigned APK/AAB build. Publishing must remain blocked unless all critical jobs pass. Pixel-exactness against original 941 × 1672 references remains unverified.
