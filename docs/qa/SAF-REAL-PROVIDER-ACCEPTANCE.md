# SAF backup — physical DocumentsProvider acceptance matrix

**Status: NOT EXECUTED ON PHYSICAL HARDWARE.** This is an explicit mandatory
test protocol for PR #236, not evidence of a successful physical test. Test
only disposable files and sacrificial game profiles; never damage an owner's
live progress or a production cloud document.

## Scope and device evidence

Test on Android API 26, 30, 33, 35, 36 as available, including at least one
real Android 15 phone and one real Android 16 phone. For each run record:
device/OS build, provider app version and authority, document URI redacted,
app commit SHA, input JSON SHA-256, original saved file SHA-256, final provider
bytes SHA-256, UI result, permission grants, provider behavior, logcat
exceptions (with account data and document contents removed).

The authoritative owner save schemas remain v1–v6 and
`com.brendigo.bopavi` is unchanged.

## Non-destructive sequence

1. Create a disposable SAF JSON document with an independently verified
   `bopavi-save` v6 backup using ACTION_CREATE_DOCUMENT, and record its
   SHA-256. Use local DocumentsUI, a removable-storage DocumentsProvider
   if available, and an explicitly opt-in cloud DocumentsProvider.
2. Trigger one valid save. Kill the app **after** the provider stream opens
   but before close with an injectable fault in a separately instrumented
   test build. A shell force-stop before opening the stream is NOT proof of
   a real partial write.
3. Relaunch the same debug build. Verify that the old file content either
   remained intact or was restored byte-for-byte from its private journal,
   with SHA-256 evidence. Successful new content is acceptable only when
   fully readable and identical to the expected backup.
4. Inject a second write/read failure during rollback; verify that the
   private recovery record remains for the next process restart, without
   reporting successful autosave.
5. Change the document externally while recovery is pending. A clearly
   unrelated external version must **not** be silently overwritten; the
   app must retain the recovery record and request manual reconciliation.
6. Test unavailable/revoked grants, provider throwing on `openOutputStream`,
   a read-only return URI, oversized original/replacement, corrupt JSON,
   and a replaced/disabled destination. Ensure no false success callback,
   no inadvertent grant revocation, no import of notification consent.
7. Restore a valid v1, v2, v3, v4, v5 and v6 fixture; compare coins, skin
   ownership, selected skin, purchased perks, achievements/stars and record
   migration with known inputs. Repeat with malformed documents; no partial
   overwrite of existing local progress.
8. Trigger rapid save, opt-out, new destination and Activity destruction in
   varying order. Verify single-writer serialization, cleanup and no UI
   callback to the destroyed Activity.

## Explicit limitations

SAF has no general portable transaction/compare-and-swap guarantee. A
provider may report writes complete before remote synchronization, and an
external editor can race after the recovery read. An empty document is
ambiguous: it may represent a truncation or a legitimate external edit. Such
cases require provider-specific evidence and possibly owner intervention.
A successful deterministic JVM fake-provider test is NOT a physical test.

**Acceptance:** report each test with exact source commit, device, provider,
observed outcome and verified SHA-256, separately from the Kotlin JVM tests.
Any loss of prior bytes, silent foreign-file overwrite, or false success
blocks PR merge/release.
