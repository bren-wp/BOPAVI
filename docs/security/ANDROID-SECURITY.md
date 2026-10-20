# BOPAVI — Android security, privacy and integrity review

**Review scope:** Android-only source snapshot from BOPAVI v1.0.1 candidate.
**Method:** Android manifest/build inspection, static source audit, source
contracts and Android CI. This is not a penetration test or proof of zero bugs.

## Confirmed design decisions

- Application ID `com.brendigo.bopavi` remains unchanged; versionCode is
  monotonically incremented for upgrades. Local SharedPreferences namespace
  `bopavi_native_v4` is retained for compatibility.
- The manifest contains a single exported launcher Activity and no INTERNET
  permission. Backup is disabled; cleartext traffic is disabled.
- No analytics, advertising, billing, authentication or required backend SDK
  appears in the Android app's declared Gradle runtime dependencies.
- Legacy local JSON save-import fields remain for compatibility but must not
  be advertised as current reachable UI.
- Release AAB is unsigned in GitHub. Private signing material must never enter
  the repository or CI logs.
- Static audit preserves reflective, system-callback and dynamically named
  resources rather than deleting based on naive token counts.

## Remaining verification

- Fuzz malformed/large legacy save imports before exposing any import UI.
- Manual lifecycle, ANR, low-memory, process-death and real-device test matrix.
- Complete dependency/SBOM/license review against actual resolved Gradle graph.
- Compare release manifest and Google Play Data safety against all Play tracks.
- Inspect emulator collision images and physical-device frame pacing separately.

No production crash-free or pixel-perfect certification is asserted.
