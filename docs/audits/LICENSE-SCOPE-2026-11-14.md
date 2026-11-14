# BOPAVI — MIT / artwork / brand / F-Droid license reconciliation

**Status: BLOCKED — owner/legal decision required; not a legal certification.**
Baseline reviewed: `main` commit `ad0cac26b480e57fe318e6996813a02752101d25` (2026-11-14).

## Verified baseline conflict and candidate correction (P1 for redistribution accuracy)

| Location | Actual state | Risk / required correction |
| --- | --- | --- |
| `LICENSE` | MIT permission to use, modify, publish, distribute, sublicense and sell copies of the covered Software | These granted rights cannot be overridden for the same MIT-covered material by new contradictory Terms |
| `fdroid/metadata/com.brendigo.bopavi.yml` | `License: MIT` | Must be true for ALL app-distributed code **and** assets under F-Droid's free-license requirements, not merely a Kotlin code folder |
| `docs/play/TERMS_OF_USE.md` §3–4 | **Baseline** prohibited reuse of MIT-licensed code; **candidate PR #236** now acknowledges MIT rights | Candidate needs owner/legal review before publication; no retrospective rights changes |
| `android/app/src/main/assets/legal/terms.html`, `docs/play/index.html`, and `docs/play/terms-of-use.html` | **Candidate** now shares matching MIT-sensitive terms in all versions | Cross-file parity is source-tested; public websites are not deployed by a PR |
| `docs/fdroid/README.md` | Already warns that MIT root does not prove ownership of third-party assets | Ownership, provenance and actual distributable licenses of 3D art, icons, fonts, SFX, music and reference-derived images still require an inventory |

## License boundaries included in the candidate Terms (owner/legal sign-off pending)

1. **MIT-covered source and associated materials**: the rights stated in the existing MIT `LICENSE` apply, including modification, redistribution and commercial use subject to its terms and notices. Do not retrospectively narrow an already granted MIT license.
2. **Brand identity**: copyright licensing alone does not grant trademark endorsement or a right to misrepresent another publisher as Brendigo. Explain this separately; do not prohibit legitimate references to the project.
3. **Artwork, audio, third-party resources**: audit origin and license per file, including whether the current repository-wide MIT statement already applies. An exclusion notice added in a later version cannot revoke rights granted to earlier versions. Do not assume all visual resources are proprietary OR MIT without verifying actual grants and authorship.
4. **F-Droid**: if the built APK contains material not eligible for free redistribution, either obtain permission / replace it with properly licensed resources or postpone official F-Droid distribution. Do not mislabel proprietary assets as MIT to satisfy metadata.
5. **Terms**: candidate rewrites §1, §3 and §4 consistently in Markdown, app-bundled HTML and web HTML. The existing consumer-rights clauses are retained. Confirm the accurate effective date and legal implications before actually publishing.

**Release gate:** no claim of license reconciliation, compliance or official F-Droid readiness until a file-by-file license bill of materials and reconciled approved Terms have been reviewed. This audit does not change the project's `LICENSE`, rights to earlier releases or the user's approved artwork.

**Current branch status:** A draft text correction is committed, not legally approved or publicly deployed. No statement of official F-Droid eligibility follows from this textual correction alone.
