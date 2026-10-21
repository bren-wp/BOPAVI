# Next Android release

## v1.0.12 — Seam allocations and adaptive frame pacing (build 142)

- Endless obstacle transitions reuse all five pickup-flag arrays when the next segment has the same gate count, resetting every boolean to prevent carrying berries, hearts, coins, stars or powers into the new group.
- When adjacent procedural groups have different gate counts, create correctly sized fresh arrays as before; preserve pickup rules, saved progress and the existing 18px bird hitbox.
- The active Android Canvas schedules redraws on the system's next vsync rather than an artificial 14ms delay. This allows 60/90/120Hz displays to use their real refresh rhythm; reduced-motion mode retains its roughly 30fps cap.
- Maintain existing FlightFrameClock behavior: the first flap, pause, backgrounding, QA capture and resumed flight never accumulate idle physics time.
- Add a deterministic Kotlin/JVM seam regression for all five pickup arrays and adapt the Python clock/render contracts. Keep the marathon parallax precision fixes from v1.0.11.
- **Release gate:** pass Android Lint, Kotlin/JVM tests, privacy/security and dead-code audits, Android 15/16 emulator checks, genuine screenshot review, APK/AAB structure and checksum verification before publishing. Device-level frame pacing and battery improvements require measurements; no percentage or Google Play availability claims are implied.
