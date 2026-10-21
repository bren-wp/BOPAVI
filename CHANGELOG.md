# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.13 — Premium HUD, smarter battery saving and physics parity (build 143)

**Published:** 2026-10-21 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.13)

- **Battery and frame pacing:** native Android OS power-saving mode now requests approximately 30 redraws per second, as does the existing Reduced motion preference, while normal motion remains synchronized to display 60/90/120Hz refresh. Battery saving is inspected during View attachment and visibility/lifecycle transitions, not through binder calls in the render loop.
- **Smooth physics:** retain 120Hz substep integration, the same -255f tap impulse, gravity, 18px body hitbox, swept obstacle collision, score rules and continuous cross-level parallax. Added deterministic tests comparing 30/60/120Hz one-second trajectories across all three difficulties.
- **Premium in-flight HUD:** lives now share a compact blue glass card aligned to the score and berries with the Croatian label **ŽIVOTI**. No wallets, development indicators or fake controls were added.
- **Truthful results:** the results hero and backdrop use the actual completed flight world's illustration and Croatian biome name instead of always showing the first world.
- **Maintainability:** the battery-aware native frame policy has been isolated in `GameFramePacing.kt` with Android-independent JVM unit tests and source audit contracts. No disruptive full rewrite or removal of compatibility code.
- **Distribution:** continue publishing the verified unsigned Google Play AAB, separately signable release APK, debug QA APK, original screenshot evidence, SHA-256 manifest and F-Droid source submission archive. Official F-Droid listing still requires fdroiddata acceptance.
- **Release gate:** Android Lint, Kotlin/JVM and Python regression tests, privacy/dead-code checks, Android 15 and 16 emulator QA, Play screenshots, verified unsigned AAB/APK, checksums and publication must pass before v1.0.13 is considered released. No measured battery or device FPS improvement is claimed.

## v1.0.12 — Seam allocations and adaptive frame pacing (build 142)

**Published:** 2026-10-21 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.12)

- Endless obstacle transitions reuse all five pickup-flag arrays when the next segment has the same gate count, resetting every boolean to prevent carrying berries, hearts, coins, stars or powers into the new group.
- When adjacent procedural groups have different gate counts, create correctly sized fresh arrays as before; preserve pickup rules, saved progress and the existing 18px bird hitbox.
- The active Android Canvas schedules redraws on the system's next vsync rather than an artificial 14ms delay. This allows 60/90/120Hz displays to use their real refresh rhythm; reduced-motion mode retains its roughly 30fps cap.
- Maintain existing FlightFrameClock behavior: the first flap, pause, backgrounding, QA capture and resumed flight never accumulate idle physics time.
- Add a deterministic Kotlin/JVM seam regression for all five pickup arrays and adapt the Python clock/render contracts. Keep the marathon parallax precision fixes from v1.0.11.
- Distribution now includes the existing QA debug APK, a separate **unsigned release APK** for signing, an **unsigned Google Play AAB** for the existing upload key, and a Git-commit-pinned **F-Droid submission ZIP** with Croatian metadata and real screenshots.
- All release files are listed in SHA256SUMS.txt. The MIT project license is present; official F-Droid dependency/resource licensing review and build validation remain required.
- **Release gate:** pass Android Lint, Kotlin/JVM tests, privacy/security and dead-code audits, Android 15/16 emulator checks, genuine screenshot review, APK/AAB structure and checksum verification before publishing. Device-level frame pacing and battery improvements require measurements; no percentage or Google Play availability claims are implied.
