# Next Android release

## v1.0.35 — Lives-only Flight, frontal pilots and harder polished obstacles (build 165)

- Remove the in-game Pause button completely and strip the score, food, power and status chips from the live flight canvas; only the centered lives and three actual heart indicators remain. Android system Back retains the accessible Continue/Retry/Exit dialog. Scoring and saved records remain functional on Results/Records.
- Improve Sova/Lisac's frontal flight by limiting full-body pitch equally for all pilots; the fox tail is recentered behind the body in original vector art while wings keep the existing flap cadence and the real 18-unit collider.
- Redraw collision-aligned obstacle lips with depth highlights and cleaner shafts, removing busy flecks without changing the actual collider. Improve difficulty after the introductory pillars via narrower/recovery openings and alternating 54–110-unit real pillar widths, while retaining the 164-unit minimum gate gap and the existing hard-mode 145-unit physical-opening floor.
- Upgrade the Android build toolchain to Android Gradle Plugin 9.5.0, Gradle 9.6.0 and JDK 17, keeping compileSdk/targetSdk 36 and minSdk 26.
- Remove retired gameplay overlay/status helpers and replace outdated source/UI contracts with lives-only and real system-Back checks. Keep original score/history/ability save data, offline privacy policy, motion-accessibility and lifecycle protections.

CI release gates must include Python source/privacy contracts, Kotlin JVM tests, R8 release build, Android 15 gameplay screenshots, Android 16 UI/navigation, SHA-256 verification and stable release artifact integrity. Pixel-identical matching to unprovided external reference screenshots and real-device Google Play approval are not claimed.
