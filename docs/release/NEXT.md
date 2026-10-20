## v1.0.2 — Android audio focus, stability contracts and documentation cleanup (build 132)
- Safely handle Android soundtrack focus loss, interruptions, resume and resource teardown with no external audio SDK and no additional app permissions.
- Expand source and regression audits for Android audio lifecycle while retaining original gameplay physics, collision geometry, artwork and offline local save keys.
- Clean up active Android crash, QA, release, artwork and accessibility guidance; archive historically useful retired iOS/old UI evidence outside active documentation.
- Preserve all published stable GitHub v1+ releases and stage v1.0.2 as an unpublished Android-only candidate until the full CI and review gates pass.
- Extend deterministic Android generator regression coverage across world families, extreme level ordinals, animated mouth clearance and conservative hard-mode ascent budgets.
- Reject mislabeled Android release previews: a real GameView idle state, first active flight with visible pause control, and retry-to-idle are now verified before captures; image hash differences alone are insufficient.
- Centre the Results content vertically on tall screens and cap the Pause modal height to avoid oversized empty panels, retaining inset-safe scaling and native accessible controls.
- Fix active-flight screenshot QA false negative using real Android UIAutomator evidence: GameView replaces its initial 'Bopi is flying' label with the accessible live SCORE/FOOD HUD during rendering. Require actual HUD plus Pause, not the transient initial label.
