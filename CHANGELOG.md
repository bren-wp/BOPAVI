# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.24 — Truthful heart and shield feedback (build 154)

**Published:** 2026-10-23 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.24)

- **Instant, understandable feedback:** a compact, high-contrast third-row notice appears for 1.1 seconds after a real life loss (`−1 ŽIVOT`), collected life (`+1 ŽIVOT`), or Lisac's actual one-shot pilot guard use (`ŠTIT POTROŠEN`). The source is the GameSimulation's own live lives/guard, not generic damage effects, speculative pickups or animation clocks.
- **Correct semantics and quiet rendering:** baseline 3/3 lives never produces a fake event; repeated collision frames and temporary invulnerability do not spam events. An absorbed Lisac hit does not claim a lost heart. One object is allocated per meaningful event, none during ordinary 30/60/90/120Hz frames. The notice expires on simulation time, so pausing naturally freezes it, without flashing, blinking or motion dependence.
- **Layout safety:** the notice lives in a separate left-aligned 480×800 logical region (18–275, 149–184), below the power indicator and clear of the native right-side Pause button. Existing top score/food/lives card spacing, pilot display and 56dp accessible pause hit target are unchanged. Geometry regression tests validate no overlaps.
- **Stronger Android UI checks:** the emulator Pause overlap test reads the current Kotlin life-card bottom directly instead of hardcoding an old or future 72/86-unit threshold. It measures the actual active GameView UI bounds; a source-level regression test protects the contract.
- **Testing:** new pure Kotlin tests validate the real life/guard state machine, event lifetime, once-only behavior across 30, 60 and 120Hz and invalid simulation times. Source-contract audit checks the native GameView integration. Full privacy/source audit, Gradle Lint/JVM, API35 actual gameplay and screenshot, API36 smoke, APK/AAB integrity, public checksums and release verification remain required.
- **Scope:** no changes to physics, collisions, scoring, health recovery rates, lifetimes, player powers, game art, offline behavior, saved progress, permissions or application ID. Physical-device accessibility checks, Google Play review and F-Droid publication are not implied by passing CI.

## v1.0.23 — Faithful HUD counters and real Pause overlap verification (build 153)

**Published:** 2026-10-23 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.23)

- **Better legibility during long runs:** continue showing the *complete* earned score and food total. At overflow, measure the real bold font, shrink the text down to 11 logical pixels, then (only if needed) horizontally fit the full glyphs into their own card. Reset shared Paint textScaleX immediately, so later labels, hearts and power text remain normal-sized and never inherit the squeeze.
- **Life urgency without animation:** a stable brighter warning colour highlights the real `1/3` last-life status; `2/3` is amber and `3/3` remains neutral. GameView's Croatian TalkBack announcement explicitly says `Posljednji život` only in the genuine 1-of-3 state. No intrusive flashing, false hearts, altered physics or changes to the existing screenshot-approved HUD coordinates.
- **Remove misleading Pause QA:** the older Android emulator gate still assumed the pre-redesign 72-unit score row. It now finds the *actual active GameView bounds*, derives the current 86-unit HUD bottom and checks the native Pause touch rectangle against that bound, including insets and split-screen view sizes where available.
- **One source of truth:** native Pause placement derives the top-card height directly from `FlightHudLayout.lives.bottom+4f` instead of repeating a magic 86; the shared logical HUD layout remains unchanged. New JVM tests cover real Pause clearance for compact, landscape and split-screen windows, plus numerical text fitting for oversized counters and the final-life colour policy.
- **Regression protection:** added Kotlin `FlightHudReadabilityTest`, source-contract `test_hud_readability_contract.py` and updated existing layout/ability/accessibility contracts. Full privacy/dead-code audit, Android Lint/JVM, API35 gameplay/screenshot capture, API36 smoke, release AAB/APK integrity, and SHA-256 checks remain mandatory.
- **Scope:** preserve Bopi/Sova/Lisac powers, collision, motion, levels, progress storage, game images, offline privacy, app identifier and permissions. Physical-device accessibility review and F-Droid/Google Play acceptance are separate checks.
