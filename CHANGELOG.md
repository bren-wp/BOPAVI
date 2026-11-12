# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.83 — Safe backup lifecycle and stale-destination guards (build 213)

**Published:** 2026-11-12 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.83)

- Prevent background backup/restore tasks from scheduling onto a stopped executor or calling a destroyed Activity.
- Invalidate queued auto-save and manual-save jobs when the user changes or disables the selected SAF document.
- Persist destination removal before revoking provider access; release obsolete grants after successfully changing destinations.
- Preserve work already writing to an external provider during Activity shutdown instead of interrupting a potentially non-atomic document write.
- Add pure Kotlin/JVM regression tests for revision invalidation and closure, and a CI source contract for the backup lifecycle.
- Keep offline operation, old v1–v6 save compatibility, the current launcher and 941×1672 screen artwork.
- Limitations: a provider's SAF write still cannot be claimed atomically recoverable; external failure-injection and real Android-device tests remain outstanding.

## v1.0.82 — Offline progress transaction hardening (build 212)

**Published:** 2026-11-12 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.82)

- Serialize skin purchases, milestone awards, consumable usage and import within the active ProgressStore.
- Commit coins, purchased skin and ownership together; return failure rather than accepting an unpersisted purchase.
- Commit level frontier and one-time earned reward together; bound the win counter.
- Make v1–v6 save import durable before success is reported, preserving historical skin IDs and save keys.
- Re-enable five overlooked QA source contracts in CI and update the outdated pause-action expectation.
- Add a regression source contract for wallet, milestone, perk and import changes.
- This release candidate does not establish pixel-perfect RGB equality, cross-provider SAF atomicity, physical-device performance certification or private signing.
