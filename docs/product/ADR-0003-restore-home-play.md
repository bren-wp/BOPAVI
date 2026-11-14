# ADR-0003 — Restore a visible Play action on Home

**Decision: APPROVED by product owner in conversation, 2026-11-14.**
**Scope: BOPAVI Android v1.0.88 candidate on PR #236.**
Supersedes *only* ADR-0002's prohibition of a production Home Play
action. Do not restore removed Records, Leaderboard, Level Map, standalone
Progress or Achievements screens.

## Reason

The owner explicitly reported that the opening screen offers no Play or
equivalent action. The saved gameplay engine `MainActivity.startGame()`
already exists, but only debuggable-only QA could launch it from Home.
A shipped arcade game must expose its core playable route to ordinary
users.

## Implementation

- Reference-camera Home: display one native accessible PLAY button in
  the previously empty space between the animated Bopi artwork and the
  original three buttons. Original Shop, Skins and Settings positions
  are preserved, as is the coin HUD.
- Compact / landscape Home: add an independent native PLAY control to the
  scrolling responsive layout, with at least 48dp target size.
- Both callbacks call the *existing* endless-game loading pipeline
  `startGame()` rather than constructing a new game or altering any
  saved progress or purchases.
- Standard `com.brendigo.bopavi` package, backup schema v1–v6,
  existing wallet, skin ownership and pause/result/retry remain intact.
- A real API36 smoke test must tap the visible Home PLAY and reach a
  genuine ready GameView **without** `bopavi_qa_launch_flight` extras.

## RGB/1:1 conflict: deliberately disclosed, not hidden

The archived owner's Home WebPs depict exactly three menu buttons and
contain *no PLAY button*. A new visible PLAY control introduces a
deliberate visual delta, so 0 different RGB pixels against those historic
Home images is mathematically impossible without updating the owner
reference. **Do not call this branch's Home 1:1.**

Before strict 50/50 RGB sign-off: obtain an updated original owner Home
WebP *for every supported native resolution* showing the approved Play
placement and verify their SHA-256 against the owner source pack. Do not
forge owner reference files or overwrite the original historical ZIP.
Other nine screens remain subject to their original measured references.

## QA and release

Source+unit tests, Android15/16 emulator, accessible native bounds and
four-route navigation, real gameplay launch, SAF provider, graphics and
strict per-screen RGB must all be reported separately. Publish or merge
only after true acceptance and publisher-authorized signing.
