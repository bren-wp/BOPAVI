# BOPAVI — Google Play Data safety assessment (v0.2.21 / build 126)

**Status: source-based draft, publisher verification pending.** Baseline main 952d2139 (2026-10-18). Google Play Data safety is a declaration about **all applicable Play-distributed versions**, not only a single GitHub build.

| Question | Audited source | Final publisher action |
|---|---|---|
| Network/user-data collection | AndroidManifest.xml has no INTERNET permission; app/build.gradle.kts has no ad or analytics dependencies | Inspect final merged manifest, bundle, runtime SDKs and all currently distributed Play builds |
| Ads and in-app purchases | No reachable advertising, store or payment SDK in current native flow | Declare accurately in Play Console after final review |
| Accounts | Home launches offline game without sign-in UI | Check dependencies and actual installed Play build |
| Stored local data | Score, best score/food, progress preferences and retained legacy records in native ProgressStore | Describe local storage and deletion accurately; do not equate device-local records with off-device collection |
| Backup / import / export | Legacy JSON encode/decode code remains for compatibility; **no user-reachable Settings/export/import UI** in this build | Never advertise a live backup feature or claim that a system picker is currently accessible |
| Deletion | Android locally stored data can be cleared in system Settings → Apps → BOPAVI → Storage / Clear storage, or by uninstalling | Verify per-OS backups and other copies independently |
| Policy | docs/play/privacy-policy.html and PRIVACY_POLICY.md | Verify public working HTTPS URL, consistent disclosures, version and publisher identity |

## Proposed answers (not submitted)

Current Android source supports *no app-initiated collection/sharing*, advertising or monetization; this is not yet an independently inspected Play-distributed binary or Play Console questionnaire. Publisher must confirm third-party libraries, other active Play versions, crash analytics, OS/integration behavior, transfer channels and applicable categories before setting the final declarations.

## Google requirements checked 2026-10-18

- https://support.google.com/googleplay/android-developer/answer/10787469 — Data safety form and public privacy policy are required even when no data is collected; closed testing is included, internal-only testing has a narrow exemption.
- https://support.google.com/googleplay/android-developer/answer/9898842 — Store metadata must describe real functionality and images must match the app.

Do not claim uploaded or approved metadata, and do not enter forms without publisher credentials and explicit authorization.
