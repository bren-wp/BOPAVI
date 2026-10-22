# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.18 — Reliable accessible audio settings (build 148)

**Published:** 2026-10-22 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.18)

- **Settings reliability:** save music and sound-effect volumes when they are changed using Android keyboard, D-pad or accessibility actions. Previously only the touch-drag release saved a slider; a non-touch change could audibly take effect but be lost on reopening options.
- **Low-write touch interaction:** during continuous touch movement, update the audio in real time but save only the final slider value when the finger lifts. Preserve the existing 0–100 values and deduplicate repeated identical saves.
- **Compatibility:** retain `music_volume`, `effects_volume`, `music_last_audible_volume`, existing v6 JSON backup structure and local offline behavior. No migration or new permission.
- **Tests:** add Android-free JVM policy tests for touch, keyboard/accessibility and programmatic initialization; source-level checks verify both SeekBar instances use the tested policy and preserve the persisted mixer settings. Existing comprehensive Android Lint/JVM and Android 15/16 emulator QA remain release gates.
- **Unchanged:** game physics, score, collision, progression, pilots, world artwork, signing model and package identifier. F-Droid acceptance and physical TalkBack validation remain outside this release's automatic claims.

## v1.0.17 — Accessible premium buttons and stable flight announcements (build 147)

**Published:** 2026-10-22 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.17)

- **Readable primary actions:** use deep-blue text, not white, on the existing gold Home/Results/Pause gradient buttons. Each endpoint of the unchanged gold gradients now exceeds the WCAG AA 4.5:1 normal-text contrast threshold. Keep native ripples, touch targets and illustration assets intact.
- **Clearer spoken flight UI:** include the actual selected pilot, flap gesture, score, collected food and lives in the native TalkBack label. Each tap reuses the most recent HUD accessibility label instead of overwriting it with a bare pilot name.
- **Efficient updates:** compose the full HUD description only when a score, berry count or life count changes. The fast render loop still reuses the cached label; the first active tap gets a short localized fallback hint.
- **Regression protection:** check real primary-button foreground and gradient-endpoint contrast mathematically in a new CI source contract; verify HUD announcements keep control instructions across taps.
- **Safety:** physics, collision geometry, level streaming, scoring, persistent data, audio policy, pilot graphics and offline privacy behavior remain unchanged. Real-world TalkBack usability and actual battery savings still require device verification.
