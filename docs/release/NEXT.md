# Next Android release — v1.0.92 / build 222 (unpublished candidate)

## v1.0.92 — Screen-by-screen native owner-reference corrections (build 222)

**CANDIDATE / NOT PUBLISHED.** Dependent on #239 Home, #240 Skins and #242 Shop; do not merge or release before all exact-head QA, safety, functional and screenshot checkpoints pass. This file tracks pending work, NOT the public changelog.

- Settings: enlarge native menu text/icon while preserving all 9 original reference rows, real switches, sliders, SAF restore and two legal routes; retained compact scroll layout for inaccessible viewport sizes.
- Game Over: add missing genuine BOPAVI winged logo above the real loss/result scene while preserving painted GAME/OVER, score data, selected character and RETRY/HOME.
- Gameplay Ready: use generated local BOPAVI logo only in the genuine ready-to-tap state, not active flight, no touched hitboxes/physics.
- Pause: refine genuine modal spacing and accessibility without changing gameplay state transitions.
- Results: improve native presented result text and hit targets without inventing a fake score or winning state.
- Privacy/Terms: remove duplicate document number in the legal dialog; preserve actual full native selectable offline legal HTML, scroll, links, explicit close.
- Mandatory: compare genuine exact-head Android 15/16 screenshots against owner SHA-pinned WebPs at all four known viewports (1080×1920/2280/2340/2400), without scaling/masking/copying artwork. Include screen-state UIAutomator assertions and real-device checks where available. **Do not claim RGB=0 from a green source build.**
- Explicit constraints: owner Game Over B does not have a separate real native capture; user-provided Results and legal Terms visual references are not in current ten-state archive. Do not pretend exact match for missing states.

The published version remains v1.0.88 until explicit release acceptance. No native release artifact or signing change is authorized by this Draft.
