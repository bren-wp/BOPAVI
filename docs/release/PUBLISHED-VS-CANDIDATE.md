# BOPAVI — Published releases versus development candidates

- Published versions: `CHANGELOG.md` is generated from GitHub's public, stable release API; it must not be populated from the candidate draft.
- Candidate work: `docs/release/NEXT.md` records only the proposed next Android version, required tests, and unresolved blockers; it does **not** prove publication.
- Store-facing active documentation: `docs/product/FEATURE-MATRIX.md`, `docs/product/CURRENT_SCREENS.md`, `docs/play/STORE-LISTING-hr-HR.md`, and `docs/play/RELEASE-CHECKLIST.md` describe the last **actually published** production navigation.
- From **v1.0.88**, PLAY is a fourth production Home action, wired to real gameplay. Historical v1.0.84–v1.0.87 three-button documentation is not current.
- Release gate: before `gh release create`, verify that both active copy and generated release notes do not state or imply that Home cannot launch gameplay. Treat candidate/unpublished labels in release notes as a hard failure.
- Published v1.0.88 has an immutable historical release-note body with the erroneous phrase “CANDIDATE / NOT PUBLISHED”. Its GitHub `published_at` is 2026-11-15T01:30:00Z. We do not rewrite the release; the corrected active documentation and changelog reflect its real publication status.
- CI success, screenshot existence and reference counts do not certify pixel-perfect RGB, signed distribution, physical SAF-provider recovery or asset ownership. Each requires separate evidence.

No historical tags, released binaries, user data, signing keys, or original owner references are modified by this document.
