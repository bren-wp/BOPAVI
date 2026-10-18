# BOPAVI — Crash triage, evidence first (P0 / PROMPT 02)

**2026-10-18 evidence baseline:** `main` `96cb160f668b9dcc96a9840496a9dc9359acbc30`; published `v0.2.21` / Android build 126. **VERDICT: UNCONFIRMED (no device crash stack trace provided).** Do not change this to `confirmed` merely because someone reports a crash, or to `fixed` because a CI build succeeds. No root cause identified from a production-device crash.

## Evidence examined / not examined

| Evidence | Observed result | Qualification |
|---|---|---|
| Main v0.2.21 native CI | Green: https://github.com/bren-wp/BOPAVI/actions/runs/39085140513 | Runner and emulator/simulator, not user's phone |
| PROMPT 00/01 PR #148 native CI | Green: https://github.com/bren-wp/BOPAVI/actions/runs/39160343625 | Four-view source/metadata build, no traced user crash |
| Previous focused QA | docs/DEAD-CODE-AUDIT-v0.2.20.md, docs/DEAD-CODE-AUDIT-v0.2.21.md, tests/test_native_lifecycle.py | Android 16 emulator image corruption / ADB disconnect distinguished from app-process crash |
| Game sources | MainActivity/GameView/GameSimulation/Soundscape (Android); GameController/GameCanvas/BopaviCore/Soundscape (iOS) | Static lifecycle review; not proof of physical-device operation |
| User-device Android exception | **Missing** | Need `FATAL EXCEPTION`, `AndroidRuntime`, native tombstone or ANR trace, exact build/package |
| iOS device .ips / crash report | **Missing** | Need .ips plus matching app dSYM UUID, device model/build |
| Low-RAM physical device replay & process-death restore | **Not executed on hardware** | Reproduction experiment below; a background process kill is not necessarily a crash |

The build's Android game is MainActivity → GameView; iOS is GameController → GameCanvas; Pause is modal. Existing validation covers simple Home → Flight → Results → Retry but **does not** establish exhaustive fast tapping, background process reclaim, audio teardown, asset-decoding behavior or every chipset.

## Android: collect first, then reproduce

Requirements: Android platform-tools, USB-debugging authorized, **the same package/version as the reported failure**, local secure evidence folder. Only run tests on a device whose owner permits debugging and data capture. Do not post raw logs/bugreports publicly (may include identifiers and private information).

```bash
adb devices -l
adb shell getprop ro.build.fingerprint
adb shell getprop ro.product.model
adb shell dumpsys package com.brendigo.bopavi | grep -E 'versionCode=|versionName=|targetSdk='
adb shell pidof com.brendigo.bopavi || true
mkdir -p qa/crash-triage
# IMPORTANT: never mistake older crash-buffer entries for this experiment.
date -u +"start=%Y-%m-%dT%H:%M:%SZ" | tee qa/crash-triage/start-utc.txt
adb logcat -b crash -v threadtime -d > qa/crash-triage/android-crash-before.log
adb logcat -b main -b system -v threadtime -d > qa/crash-triage/android-system-before.log
# Execute reproducible runs without touching application user-data (does force-stop):
bash tests/qa_capture_android_crash.sh 20 qa/crash-triage
adb logcat -b crash -v threadtime -d > qa/crash-triage/android-crash-after.log
adb shell dumpsys meminfo com.brendigo.bopavi > qa/crash-triage/meminfo.txt
# Optional, large and sensitive: run only if user explicitly approves collection:
adb bugreport qa/crash-triage/bugreport.zip
```

Do **not** use `adb logcat -c` on a user's device as an audit prerequisite; it destroys prior evidence. Match crash timestamps/PID/package against reproduction start and before/after captures. For a native SIGSEGV, inspect tombstone/backtrace and build ID; for Java/Kotlin, inspect `FATAL EXCEPTION`, exception class and top application-owned frames. For ANR, inspect traces and CPU waits separately, never mislabel as a crash. If process vanishes under memory pressure, compare `ActivityManager` kill reason and system low-memory logs: OS reclaim is not automatically an app crash.

### Safe stress experiment (record every attempt)

```bash
# Same installed v0.2.21/build 126 before and after test; do not reset app storage.
bash tests/qa_capture_android_crash.sh 50 qa/crash-triage/replay-50
adb shell am start -W -n com.brendigo.bopavi/.MainActivity
adb shell input keyevent KEYCODE_HOME
adb shell am start -W -n com.brendigo.bopavi/.MainActivity
adb shell dumpsys meminfo com.brendigo.bopavi
```

Manually execute and video-record with screen touches visible: (1) cold launch with/without existing saves; (2) tap PLAY, first flap and rapid double-tap; (3) active pause → continue → pause → retry/exit ×20; (4) Results → Retry ×20; (5) Home↔Game scene switch; (6) Home key → sleep/wake → resume, phone call/interruption; (7) low-memory device/multiple app switches/Android background process reclaim; (8) disable/enable sound through any **actually reachable** control only (current game has no Settings screen); (9) long run with many pickups, score and save counts; (10) asset decode under cold start and repeated scene creation. Log timestamp, device and expected vs actual outcome for each attempt. Do **not** infer a failure from an ADB transport disconnect, or claim no bug because one emulator passed.

`adb shell am kill com.brendigo.bopavi` after HOME (app is backgrounded) is a **controlled process-death scenario**, **not a crash**; check that relaunch is sane and locally saved results are retained. Do not use `pm clear` without a user-approved disposable test profile, because that deletes records.

## iOS / iPadOS evidence

1. Install the **matching signed/TestFlight or development build** with its correct bundle ID/version. iOS unsigned archive from GitHub is not an installable production submission.
2. Xcode → **Window → Devices and Simulators** → select device → **View Device Logs**; collect the matching `.ips` crash/termination report. Alternatively, on device: Settings → Privacy & Security → Analytics & Improvements → Analytics Data; export the matching .ips locally.
3. Match `incident_id`, `bundleID`, `app_version`, OS/device, time, exception type, `termination` and crashed thread, and verify dSYM UUID:
```bash
xcrun dwarfdump --uuid path/to/BOPAVI.app.dSYM
# Symbolicate program counter using the correct archive dSYM and Binary Images load address:
xcrun atos -arch arm64 -o path/to/BOPAVI.app.dSYM/Contents/Resources/DWARF/BOPAVI -l 0xLOAD_ADDRESS 0xCRASH_PC
# For complete report: use Xcode Devices and Simulators > View Device Logs;
# Xcode automatically symbolicates if the exact build's dSYM is available.
```
4. Instruments → Allocations / Leaks / Time Profiler for cold launch, 20 fast retries, AVAudioPlayer teardown, repeated UIImage decoding and memory warnings. For jetsam, inspect the `JetsamEvent` / memory pressure report; do not assert an uncaught Swift exception.
5. Run the same gameplay, idle/pause, HOME/background, screen lock, memory pressure and process-death restore scenarios on real iPhone and simulator. Record observed result per build/device.

## Hypotheses — not root causes

| ID / scenario | Relevant code | Minimum evidence / regression |
|---|---|---|
| H01 cold start/scene rebuild | MainActivity.showHome; GameController.showHome | Repro timestamp, top stack; 50 cold starts, preserved saves |
| H02 fast retry/callback race | GameView.onFinished / GameController.showResult | Immediate Results → Retry ×20, one navigation transition |
| H03 Activity / UIView detach | GameView.onDetachedFromWindow / GameCanvas.stop | Heap/bitmap ownership, link count, no stale callback |
| H04 background/wake | onPause/onResume; willResignActive/didBecomeActive | No physics advance while paused; same run resumes only by user |
| H05 sound teardown | SoundPool/MediaPlayer; AVAudioPlayer | Thread-specific trace, no released-object reuse |
| H06 weak RAM/asset decode | Canvas bitmap loading / UIImage initialization | Peak PSS/jetsam, asset name/dimensions, no OOM |
| H07 process-death restore | ProgressStore; onCreate/viewDidLoad | Clean launch and persisted records, no assumed continuation of in-memory flight |
| H08 integer overflow/saved counters | recordRun, score, food, bests | Invalid or MAX-sized save migration fixtures and exact exception |
| H09 delta/double-tap/paused state | GameSimulation.step/flap + renderer timing | Deterministic 30/60/90/120 Hz tests, no step after true pause |
| H10 Android emulator/ADB failure | tools/prepare_android_emulator_image.sh; qa_android_emulator.sh | Distinguish SDK image corruption/transport from app exception |

## Triage decision and fix gate

- **Confirmed crash** requires reproducible same-build device steps **and** process-owned native/managed stack (or an equivalent traced fatal event). Document exception, offending method/line, minimal pre-fix test that fails, small targeted patch, post-fix test, Android/iOS regression and build IDs.
- **Unconfirmed** (this audit): report failed attempts and missing device traces; do not add speculative `try/catch`, ignore exceptions, reset user data, or mark user crash fixed.
- **Closed only after evidence:** no new release or Play crash-free guarantee is implied by a green emulator workflow. Use a separate incident number for a new distinct stack signature.
