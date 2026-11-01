# Next Android release

## v1.0.43 — Shop native 3D reference layout (build 173)

- Match the supplied 941×1672 Shop composition more closely with its large title, original local Bopi portrait, native featured-item surface and a three-column/two-row grid of six existing offline products.
- Preserve the real six shield/magnet item indices, prices, earned balances, saved stock, atomic locally committed purchases, purchase tap gate and earned-only bonus claim.
- Display real earned coins and earned stars, not the illustrative gem/cash prices, random skins or other nonexistent products shown in the reference artwork. Keep all controls accessible and native; no screenshot-as-button and no third-party service or additional permissions.
- Update the Shop geometry's JVM regression and Python source contract; continue Android 15/16 emulator QA, Gradle lint/JVM/build and dead-code/privacy audits.
- Visual acceptance: genuine emulator screenshots must still be compared pixel-by-pixel against the original references. Rendering cannot yet be certified as 1:1.
- Publisher-only signing. Do not merge or publish without reviewed green required checks.

- Settings reference polish: give all eight existing native settings rows raised color-coded enamel pictograms and add a high-contrast gold 3D SETTINGS sign in both reference and compact scroll layouts. Preserved all real locally stored audio/haptic/accessibility/difficulty controls and legal buttons. Do not add fake cloud save, notifications, paid purchase restores or network services. Kotlin/Android 15-16 UI and screenshot QA still required.
- Responsive Shop: native ScrollView and single-column full-size purchase cards for compact/landscape/large-font viewports, preserving actual insets and at least 48dp interactive targets; the 3×2 reference camera is used only when it safely fits. Added Kotlin and source-contract safeguards.

- Level Map responsive reliability: retain original 941×1672 fantasy island map and all accessible native stage buttons for normal reference-sized portrait screens; switch to an offline native scroll layout for compact, landscape and enlarged-font devices, avoiding overlapping 48dp hit targets. The alternative displays only actual persisted world ratings, unlocked levels and saved currency, with live Previous/More worlds, disabled locked stages, and a real Play Level callback. Preserve the existing eight worlds, local progress and stage physics.

- Level Map native illustration polish: locally painted stratified floating-rock facets, Meadow forest canopy, glowing faceted Crystal Ruins crystals and Volcano Peaks lava crater. These are deterministic decorative Canvas elements behind independent native stage buttons; no stored progression, lock state, physical collision, star count or unlock path is changed. Real reference-vs-emulator pixel comparison is still required; this is not a claim of verified pixel identity.

- Fixed visually verified native Level Map label clipping from an earlier genuine 941×1672 emulator screenshot: the "LEVEL MAP" header, full two-line world names/earned star counts, and true COINS/STARS numeric balances use bounded Android auto-size, explicit line counts and reduced intrinsic padding. No custom Canvas-only text; all world selectors and stat chips remain real accessible views. Screenshot-to-reference precision after this commit still needs emulator confirmation.
