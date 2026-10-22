# Next Android release

## v1.0.18 — Reliable accessible audio settings (build 148)

- **Settings reliability:** save music and sound-effect volumes when they are changed using Android keyboard, D-pad or accessibility actions. Previously only the touch-drag release saved a slider; a non-touch change could audibly take effect but be lost on reopening options.
- **Low-write touch interaction:** during continuous touch movement, update the audio in real time but save only the final slider value when the finger lifts. Preserve the existing 0–100 values and deduplicate repeated identical saves.
- **Compatibility:** retain `music_volume`, `effects_volume`, `music_last_audible_volume`, existing v6 JSON backup structure and local offline behavior. No migration or new permission.
- **Tests:** add Android-free JVM policy tests for touch, keyboard/accessibility and programmatic initialization; source-level checks verify both SeekBar instances use the tested policy and preserve the persisted mixer settings. Existing comprehensive Android Lint/JVM and Android 15/16 emulator QA remain release gates.
- **Unchanged:** game physics, score, collision, progression, pilots, world artwork, signing model and package identifier. F-Droid acceptance and physical TalkBack validation remain outside this release's automatic claims.
