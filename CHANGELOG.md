# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.1 — Native Android-only migration and release safeguards (build 131)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.1)

- Remove the obsolete iOS project, simulator build, Swift tests, iOS resources, and iOS-specific release packaging; preserve original BOPAVI graphics and Android audio.
- Harden offline Android asset production, Android-only CI contracts, and the tested release process without changing saved progress keys or the com.brendigo.bopavi application identity.
- Preserve deterministic flight physics and existing circle/rounded-pillar collision model pending further measured device-level tuning; run Android gameplay, emulator, and collision regression gates before publishing.
- Keep all existing public GitHub v1+ releases unchanged and prevent any iOS artifacts from future Android-only releases.

## v1.0.0 — first stable version and verified public presentation (build 130)

**Published:** 2026-10-19 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.0)

- Establish the first stable Semantic Versioning release candidate while retaining com.brendigo.bopavi, Android/iOS bundle identity, existing save data and all gameplay behavior.
- Increase both Android and iOS build numbers monotonically from 129 to 130; do not retroactively rename or replace older release binaries.
- Replace the public-facing README with a player-focused homepage, licensed project art and links to real release captures. Developer-specific status remains in dedicated project documentation.
- Add regression checks for marketing claims and stable releases. Releasing is conditional on complete native CI and authentic screenshots; unsigned GitHub outputs are not Play/App Store publication.
