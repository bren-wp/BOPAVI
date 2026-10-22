# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.19 — Three pilot powers, faster flights and tougher pillars (build 149)

**Published:** 2026-10-22 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.19)

- **Three distinct playable abilities** (free, offline and automatic): Bopi's *Brzi let* increases horizontal world movement by 6%; Sova's *Mirna krila* reduces gravity by 8% for more precise altitude control; Lisac's *Zaštitni štit* absorbs the first collision once per endless run without consuming a heart. All use the same 18px hitbox and familiar single-tap wing impulse.
- **Real mechanics, not cosmetics:** MainActivity passes the chosen Home pilot to GameSimulation. Simulation physics and actual pillar collision decide the results, not animations. Lisac's guard is spent exactly once, carries no saved currency, and never recharges automatically. The legacy default CLASSIC policy retains compatibility for existing finite/test callers.
- **Faster, harder, fairer worlds:** normal world speed starts at 148 instead of 140 logical units/second (and is capped at 228 instead of 212), Bopi's extra boost is additive through the physics multiplier. After the first two introductory gates, later mouths narrow deterministically but never below the original 164px base corridor; active moving pillars grow slightly more demanding yet remain capped at 22px displacement. The established hardest opening stays at least 145px and the original 18px body collider remains unchanged. Renderer and collision share the same Gate and Opening values.
- **Home and all active screens:** native pilot cards announce the power through their Croatian accessibility descriptions. The selection heading shows the selected pilot's ability without adding a new row or shrinking legal tap targets. The flight HUD gets a slim, readable power indicator below its three real counters and left of the pause button; Lisac's shield visibly changes from ready to consumed. Pause and Results expose the selected ability without changing their native actions or button geometry.
- **Accessibility and performance:** updated live flight TalkBack description includes the ability and its one-time status, with updates only when counters/lives/guard state change. Rendering reuses static labels and existing Paint/Canvas primitives, without allocating bitmaps on each frame. Reduced-motion and power-save options continue to work.
- **Regression gates:** new Kotlin/JVM tests verify the three real skills, one-use collision shield, default compatibility, faster deterministic generation, corridor floors and bounded moving pillars. Expanded source and Android 15 emulator accessibility checks; existing Android Lint, JVM, native screenshots, API35/API36, source/privacy/dead-code and signed/unsigned release validation still gate merges.
- **Scope:** no paid items, tracking, Internet permission, new SDK, unsafe shortcut collisions, imported-save migration or changes to the approved character/world artwork. Device-specific feel, manual TalkBack validation and F-Droid acceptance remain subject to separate checks.

## v1.0.18 — Reliable accessible audio settings (build 148)

**Published:** 2026-10-22 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.18)

- **Settings reliability:** save music and sound-effect volumes when they are changed using Android keyboard, D-pad or accessibility actions. Previously only the touch-drag release saved a slider; a non-touch change could audibly take effect but be lost on reopening options.
- **Low-write touch interaction:** during continuous touch movement, update the audio in real time but save only the final slider value when the finger lifts. Preserve the existing 0–100 values and deduplicate repeated identical saves.
- **Compatibility:** retain `music_volume`, `effects_volume`, `music_last_audible_volume`, existing v6 JSON backup structure and local offline behavior. No migration or new permission.
- **Tests:** add Android-free JVM policy tests for touch, keyboard/accessibility and programmatic initialization; source-level checks verify both SeekBar instances use the tested policy and preserve the persisted mixer settings. Existing comprehensive Android Lint/JVM and Android 15/16 emulator QA remain release gates.
- **Unchanged:** game physics, score, collision, progression, pilots, world artwork, signing model and package identifier. F-Droid acceptance and physical TalkBack validation remain outside this release's automatic claims.
