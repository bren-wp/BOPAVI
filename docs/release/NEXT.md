# BOPAVI — next Android release

## v1.0.82 — Offline progress transaction hardening (build 212)

- Serialize skin purchases, milestone awards, consumable usage and import within the active ProgressStore.
- Commit coins, purchased skin and ownership together; return failure rather than accepting an unpersisted purchase.
- Commit level frontier and one-time earned reward together; bound the win counter.
- Make v1–v6 save import durable before success is reported, preserving historical skin IDs and save keys.
- Re-enable five overlooked QA source contracts in CI and update the outdated pause-action expectation.
- Add a regression source contract for wallet, milestone, perk and import changes.
- This release candidate does not establish pixel-perfect RGB equality, cross-provider SAF atomicity, physical-device performance certification or private signing.
