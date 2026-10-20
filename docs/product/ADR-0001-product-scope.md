# ADR-0001 — BOPAVI Android product scope

**Status: PENDING PRODUCT OWNER APPROVAL.** This document does not
silently restore removed features or imply a new approval.

## Current executable baseline

Native Android has one Bopi, a continuous flight, scoring and
contact-collected berries, Home, Pause and Results with Retry/Home.
The package is `com.brendigo.bopavi`; offline local data and old
save-key compatibility are mandatory. No shop, worlds selector, skins
picker, monetization or settings menu is currently reachable.

## Decision A — Focused endless arcade

Keep the small visible feature set, optimize graphics, collisions,
stability, accessibility, battery and Google Play honesty. Preserve
historical source art / save fields unless audited safe to remove.

## Decision B — Expanded premium game

Restore more player-facing features only on explicit owner approval,
after feature scope, design references, save migration and QA acceptance
are specified. Historical prototypes and generated art do not
authorize a change to current player-visible features.

## Approval gate

Record `Decision: A` or `Decision: B`, date and owner approval
separately. Until approval, follow the **existing reachable UX** and
do not reintroduce previously removed functions. All public copy must
describe current installed Android behaviour.
