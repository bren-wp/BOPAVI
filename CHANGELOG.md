# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.23 — Faithful HUD counters and real Pause overlap verification (build 153)

**Published:** 2026-10-23 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.23)

- **Better legibility during long runs:** continue showing the *complete* earned score and food total. At overflow, measure the real bold font, shrink the text down to 11 logical pixels, then (only if needed) horizontally fit the full glyphs into their own card. Reset shared Paint textScaleX immediately, so later labels, hearts and power text remain normal-sized and never inherit the squeeze.
- **Life urgency without animation:** a stable brighter warning colour highlights the real `1/3` last-life status; `2/3` is amber and `3/3` remains neutral. GameView's Croatian TalkBack announcement explicitly says `Posljednji život` only in the genuine 1-of-3 state. No intrusive flashing, false hearts, altered physics or changes to the existing screenshot-approved HUD coordinates.
- **Remove misleading Pause QA:** the older Android emulator gate still assumed the pre-redesign 72-unit score row. It now finds the *actual active GameView bounds*, derives the current 86-unit HUD bottom and checks the native Pause touch rectangle against that bound, including insets and split-screen view sizes where available.
- **One source of truth:** native Pause placement derives the top-card height directly from `FlightHudLayout.lives.bottom+4f` instead of repeating a magic 86; the shared logical HUD layout remains unchanged. New JVM tests cover real Pause clearance for compact, landscape and split-screen windows, plus numerical text fitting for oversized counters and the final-life colour policy.
- **Regression protection:** added Kotlin `FlightHudReadabilityTest`, source-contract `test_hud_readability_contract.py` and updated existing layout/ability/accessibility contracts. Full privacy/dead-code audit, Android Lint/JVM, API35 gameplay/screenshot capture, API36 smoke, release AAB/APK integrity, and SHA-256 checks remain mandatory.
- **Scope:** preserve Bopi/Sova/Lisac powers, collision, motion, levels, progress storage, game images, offline privacy, app identifier and permissions. Physical-device accessibility review and F-Droid/Google Play acceptance are separate checks.

## v1.0.22 — Readable lives, balanced flight HUD and pause spacing (build 152)

**Published:** 2026-10-23 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.22)

- **Direct screenshot correction:** replace the cramped top-row cards with an aligned three-column hierarchy. Score (18–146), berries (154–276), and the wider life panel (284–462) now share a taller 16–82 logical range and consistent 8-unit gutters. The life panel receives the most horizontal room.
- **Real life indicator:** centre the three hearts at x=320, 373, 426 on their own lower line (baseline 69). Keep the actual `3/3` counter right-aligned at x=445 above them, at a fixed safe inset rather than pressed against the border or the last heart. The score, fruit, hearts and counter continue reading real GameSimulation state, without any invented values.
- **Text hierarchy:** larger common Croatian labels (BODOVI, HRANA, ŽIVOTI), numbers on an aligned lower baseline; the existing measured-width number renderer preserves long marathon scores without drawing over adjacent indicators.
- **Power and pause:** put the MOĆ heading and its colour-coded, live pilot ability on a separate 91–140 logical card, fully outside the real native pause button's right lane. Place the pause hit target only after the taller top row, updating the Android status-bar/cutout-safe overlay policy from the real HUD transform. Retain a full 56dp accessible touch target, responsive layout and existing 30fps reduced-motion mode.
- **Geometry QA:** new assertions verify identical card heights, precise gaps, visual centring of all hearts, vertical separation of the hearts from the numeric counter, increased life-panel width, and non-overlapping power/pause regions. Revalidated source contracts for the native Canvas HUD and pause layout; full Android Lint, Kotlin/JVM, Android 15 live gameplay screenshots and Android 16 emulator smoke remain mandatory.
- **No gameplay change:** keep movement physics, collision, score formulas, birds and abilities, moving pillar warnings, offline privacy, save formats, permissions and previously approved original graphics unchanged. Real-device optical inspection is still recommended; this release must not claim F-Droid or Google Play approval.
