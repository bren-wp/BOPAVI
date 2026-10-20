## v1.0.3 — Croatian UI, polished navigation and Play policy hardening (build 133)

- Use Croatian on every reachable native screen and accessibility label regardless of device locale, without erasing historical user settings or imported save keys.
- Replace the result screen's old plain Home action with a clearly styled, high-contrast, 58dp native arrow-back button and a consistent Croatian accessible description.
- Improve the Pause exit arrow; preserve game-state transitions, Back gestures, protected ongoing flight and real score persistence.
- Revalidate current Google Play API 36, truthful Croatian listing, privacy and real same-build artwork.
- Expand dead-code/resource audits and prevent orphan resources while retaining old public releases and backup migration behaviour.
- Remove unreachable 26-language UI catalog and dead helper logic from the shipped Kotlin code. Save migration and historic records stay intact.
