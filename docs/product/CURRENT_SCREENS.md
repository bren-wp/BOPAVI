# BOPAVI — reachable screens (v0.2.21 / build 126)

Native launch entry points: Android MainActivity.onCreate() → showHome(); iOS BopaviApp → GameController.viewDidLoad() → showHome(). Four reachable UX states; Pause is a modal overlay, not a separate standalone menu.

| State | Entry | Actions / destination | Screenshot evidence |
|---|---|---|---|
| Home | Cold launch; Results → POČETNA; Pause → IZLAZ | IGRAJ → Flight (idle preview) | v0.2.21 release: BOPAVI-Android-Home.png |
| Flight (idle / active) | Home → IGRAJ; retry | Tap/flap activates flight; active Pause button → Pause; collision/end → Results | BOPAVI-Android-Flight-Preview.png; BOPAVI-Android-Flight-Active.png |
| Pause modal | Pause button during active flight; back handling | NASTAVI → same Flight; PONOVO → new Flight; IZLAZ → Home; interruption can pause simulation without opening separate menu | No dedicated v0.2.21 release screenshot; capture and verify separately |
| Results | onFinished after actual run | PONOVO → Flight; POČETNA → Home; actual score, collected berries and stored best records | BOPAVI-Android-Results.png |

## Reachability proofs

- Android: android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt contains showHome / startGame / showPremiumPause / showResult and initial showHome.
- iOS: ios/BOPAVI/GameController.swift contains the parallel four handlers; ios/BOPAVI/BopaviApp.swift launches GameController.
- Existing source tests: tests/test_native_files.py and tests/test_reference_navigation.py check that removed showWorlds, showLevels, showShop, showSettings and other menu routes are absent.
- Runtime fixed to original Bopi and first world. An art asset or a legacy save field does **not** make a screen reachable.
- Release screenshots: https://github.com/bren-wp/BOPAVI/releases/tag/v0.2.21 (build 126). Android screenshots are emulator captures, not proof of physical-device parity. iOS Home capture also exists; other iOS flow screenshots must be captured and reviewed separately.

## Paths requiring next manual QA

Cold launch → IGRAJ → first flap → berry pickup → pause/continue → collision → results → replay/home, on both native platforms. Verify device back/gesture, interruption pause, notch/safe areas, 60/90/120 Hz and no scrolling or inert controls. An interactive test on a simulator/device is still needed for each flow; these checks must not be represented as completed solely from source inspection.
