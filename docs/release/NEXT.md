# Next Android release

## v1.0.13 — Premium HUD, smarter battery saving and physics parity (build 143)

- **Battery and frame pacing:** native Android OS power-saving mode now requests approximately 30 redraws per second, as does the existing Reduced motion preference, while normal motion remains synchronized to display 60/90/120Hz refresh. Battery saving is inspected during View attachment and visibility/lifecycle transitions, not through binder calls in the render loop.
- **Smooth physics:** retain 120Hz substep integration, the same -255f tap impulse, gravity, 18px body hitbox, swept obstacle collision, score rules and continuous cross-level parallax. Added deterministic tests comparing 30/60/120Hz one-second trajectories across all three difficulties.
- **Premium in-flight HUD:** lives now share a compact blue glass card aligned to the score and berries with the Croatian label **ŽIVOTI**. No wallets, development indicators or fake controls were added.
- **Truthful results:** the results hero and backdrop use the actual completed flight world's illustration and Croatian biome name instead of always showing the first world.
- **Maintainability:** the battery-aware native frame policy has been isolated in `GameFramePacing.kt` with Android-independent JVM unit tests and source audit contracts. No disruptive full rewrite or removal of compatibility code.
- **Distribution:** continue publishing the verified unsigned Google Play AAB, separately signable release APK, debug QA APK, original screenshot evidence, SHA-256 manifest and F-Droid source submission archive. Official F-Droid listing still requires fdroiddata acceptance.
- **Release gate:** Android Lint, Kotlin/JVM and Python regression tests, privacy/dead-code checks, Android 15 and 16 emulator QA, Play screenshots, verified unsigned AAB/APK, checksums and publication must pass before v1.0.13 is considered released. No measured battery or device FPS improvement is claimed.
