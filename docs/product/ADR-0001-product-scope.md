# ADR-0001 — BOPAVI product scope

**Status: PENDING PRODUCT OWNER APPROVAL** (2026-10-18). This PR **does not** choose a product direction or restore UI. A default descriptive baseline is not approval.

## Decision A — Focused endless arcade flight
One Bopi, one endless procedural flight, score/food collection, pause, results, retry. Keep legacy saves readable, but no shop or hidden settings UI. Advantages: smaller test matrix, clearer Play description, faster stable shipping. Costs: reduced premium breadth versus historical art references.

## Decision B — Full premium product
Restore accessible selection of characters, worlds/levels, shop, profiles, settings, achievements, record screens and a well-defined save import/export flow **only after explicit approval and staged technical plan**. Advantages: broader game progression, reference parity opportunity. Costs: significant additional product, economy, localization, compatibility, accessibility and QA obligations; artwork existing in the repo is not a shipped feature.

## Required explicit approval and implementation gate

Product owner records: `Decision: A` or `Decision: B`, date, approver and scope acceptance criteria. Until then, **PENDING**. No removed screen should be implemented because this document exists. If B approved: new feature-specs and staged PRs, precise gameplay/economy/save semantics, Android/iOS parity tests, real screenshots and synchronized public metadata **before** claiming features. If A approved: remove stale internal assets only after migration audit and preserve saved records.

## Non-negotiable
Google Play statements describe the *currently shipped reachable UX*, irrespective of any future roadmap. Build/signature and Play approval remain separate independently verified steps.
