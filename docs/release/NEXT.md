# Next Android release

## v1.0.30 — Uniform native icons, Records/Results polish and touch accessibility (build 160)

- **Results screen:** replace platform-dependent star and strawberry emoji in personal-best summary with reusable antialiased BOPAVI STAR and BERRY pictograms, preserving genuine persisted best points and food.
- **Records dialog:** show a consistent native icon beside each of five real offline statistics (best points, food, obstacles, flights and flaps). Add new native GATE, FLIGHT and WING drawings; each row has one complete, spoken TalkBack label and value rather than a decorative emoji glyph.
- **Options and Records headers:** use consistent native SETTINGS/RECORDS pictograms; avoid emoji glyphs that change with device and font.
- **Pause:** use the actual PAUSE pictogram (not a trophy icon); improve accessible resume action label to "Nastavi let".
- **Buttons and legal links:** add bounded native ripple feedback and keep explicit 48dp clickable areas, real native input targets and Croatian accessibility descriptions.
- **Native icon correctness:** correctly compose red berry opacity and green foliage alpha with Drawable alpha; preserve colour filter and original approved Bopi launcher artwork, adaptive icon mask and three pilots.
- **QA:** update icon, layout and options regression contracts, add a cross-screen native stats accessibility contract; run full Android lint/unit/gameplay/visual QA on Android 15/16, generate five authentic app screenshots and preserve signed-key policy.
- **Compatibility:** retain original saved progress keys, score logic, collision physics, package name and all existing offline privacy/security behavior.

GitHub distributes an installable debug APK plus *unsigned* release APK/AAB; production Play upload requires publisher signing. Existing public editions and their checksums remain immutable.
