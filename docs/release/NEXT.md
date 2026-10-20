# Next Android release

## v1.0.8 — Heart and berry fairness, impact clarity, polish (build 138)

- Repair the actual 3-life pickup edge case: every heart-gate with a normal berry now shows a berry while at full health instead of appearing empty.
- The game grants **exactly one reward per gate**: collecting the berry prevents the heart from reappearing after damage; collecting the heart prevents a berry from appearing immediately after life restoration.
- Restoring health now triggers the same **one-shot collection sound and haptic event** as berries, with no extra audio engine allocations.
- Losing an arcade life has a subtle warm-rose impact effect and a steady grace-period outline, distinct from the existing cyan legacy shield and respecting reduced-motion preferences.
- Correct the developer-only collision overlay to draw the currently available pickup, consistent with the exact game model.
- Add deterministic JVM tests that assert rare pickup behaviour at full/partial health and prevent a second reward when health changes. Keep original 18px collider and procedural obstacle positions unchanged.
- Maintain v1.0.7 published README/store statements until GitHub's confirmed v1.0.8 release. Keep all local save keys, legal HTML, three pilots, offline/no-ads behavior and privacy unchanged.
- Gate publication on passing Android Lint/JVM, full static/security/dead-code audits, Android 15/16 emulator QA and real release screenshots. Production signing/physical-device battery tests are not inferred.
