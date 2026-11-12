# BOPAVI — next Android release

## v1.0.83 — Safe backup lifecycle and stale-destination guards (build 213)

- Prevent background backup/restore tasks from scheduling onto a stopped executor or calling a destroyed Activity.
- Invalidate queued auto-save and manual-save jobs when the user changes or disables the selected SAF document.
- Persist destination removal before revoking provider access; release obsolete grants after successfully changing destinations.
- Preserve work already writing to an external provider during Activity shutdown instead of interrupting a potentially non-atomic document write.
- Add pure Kotlin/JVM regression tests for revision invalidation and closure, and a CI source contract for the backup lifecycle.
- Keep offline operation, old v1–v6 save compatibility, the current launcher and 941×1672 screen artwork.
- Limitations: a provider's SAF write still cannot be claimed atomically recoverable; external failure-injection and real Android-device tests remain outstanding.
