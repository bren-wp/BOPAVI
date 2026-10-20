# BOPAVI — Current Android screens

**Published release baseline:** v1.0.1 (build 131). New candidate version
metadata belongs in Gradle and docs/release/NEXT.md until publication.

The Android entry point is MainActivity.onCreate → showHome. There are four
reachable states: Home, Flight, Pause (overlay) and Results. No shop, worlds
selector, skin picker, account page or monetization screen is reachable.

| State | Action | Genuine Android screenshot |
|---|---|---|
| Home | Start the game using IGRAJ | BOPAVI-Android-Home.png |
| Flight (preview) | First flap begins live simulation | BOPAVI-Android-Flight-Preview.png |
| Flight (active) | Fly past pillars and collect red berries | BOPAVI-Android-Flight-Active.png |
| Pause (overlay) | Continue, restart or exit | Must be separately captured and verified |
| Results | Show saved best, food and retry/home | BOPAVI-Android-Results.png |

Actual release assets: https://github.com/bren-wp/BOPAVI/releases/latest.
Emulator screenshots are real captured frames from the application, not reference
renders and not substitutes for physical-device QA. Gameplay, camera, HUD and
collision all use the original 480×800 logical coordinate system; layout
and system insets require independent validation across supported screens.
