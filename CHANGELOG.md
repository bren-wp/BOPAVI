## v0.1.56 — Matching golden stars and a leaner iOS renderer (build 59)

- Android's existing collectible stars now use the same precise ten-vertex, five-point gold geometry and 12-pixel radius as iOS rather than depending on world-specific font glyphs. **Visibility, value, magnet, collisions, score and saved progress remain unchanged.**
- Core Graphics now draws its frequently reused rounded gameplay panels directly into the current drawing context rather than allocating a fresh `CGPath(roundedRect:)` for every HUD and obstacle rectangle.
- Both implementations preserve accessibility and reduced-motion behaviour, as well as the continuous streamed-level transitions and offline-only game model.
- Static regression tests enforce native star parity, visual position and the four quarter-circle rounded corners across narrow and full-size rectangles.
- Pixel-perfect reference art remains an uncompleted goal; neither generic 2D scenery nor simplified mascot sprites are described as photorealistic 3D.

## v0.1.55 — Royal crown collectibles & real last-gate portals (build 58)

- Render actual **coins** as reusable, bevelled gold crown medals in all eight worlds on Android Canvas and iOS Core Graphics. They no longer look like world-specific unlabelled glowing circles. Existing coin pickup flags, currency, score, magnet and save logic are unchanged.
- Mark the **actual final gate of each level** with a cyan circular magical finish portal and a golden crown, including approaching streamed gates. The existing collision geometry remains authoritative; the portal is strictly visual, not a fictional exit or instant win.
- Pulse effects respect reduced-motion settings; reused native rendering objects do not decode or allocate images inside the gameplay animation loop.
- Extend source QA for material palette parity, icon geometry, actual final-gate identification, reduced-motion, input/physics noninterference, and real screenshot release verification.
- Include a real, emulator-captured **active gameplay screenshot** as an additional versioned GitHub Release asset and in the marketing README gallery. Retain generated assets from the game itself, not user concept art.
- Android and iOS v0.1.55 build 58. **The stylised 2D game is still not pixel-perfect with the supplied high-end 3D concepts.**

## v0.1.54 — Bopi flight HUD matches reference hierarchy (build 57)

- Redesigned the real active-flight HUD on **Android Canvas and iOS Core Graphics** to use the reference's left coin wallet, a single central crown-to-flag progress gauge and a vertical rail of three blue/gold circular icons. The native, genuinely tappable pause button retains its own reserved top-right space.
- The central bar shows **actual current-level gates passed/total**, not invented metre counts from the static visual mockup. A separate slim live row shows current level and lifetime total passes, including across continuous procedural transitions.
- Circular badges display **actual earned stars, equipped shields and remaining magnet seconds**; inactive powers are subdued and no decorative circle pretends to be a purchase/activation button. Preserved accessibility state and all original touch-to-flap physics.
- No modification to world/character IDs, save schema, navigation, difficulty, collisions, pickups, rewarded ad logic or core physics. Verified regression contracts were extended for both renderers.
- Android 15 QA now saves an authentic **active-flight HUD screenshot** after confirming the game is actually flying. This evidence remains separate from concept art; production release assets still require the existing authentic screenshot QA gates.
- Android+iOS bump together to **v0.1.54 (build 57)**. High-detail 3D-feather, material shading and painted background fidelity remain below the reference; **this is not a claim of completed pixel-perfect 1:1 visual parity.**

## v0.1.53 — compact eight-world gallery and independent branded intro (build 56)

- Android and iOS world galleries now match the 2×4 composition of the supplied concept more closely: short, full-bleed scenic cards with the actual world title, genuine earned stars and saved best score overlaid directly on each world image. Kept the world badge, blue forward affordance, gold selected outline and entire card as one accessible tap target.
- Removed the previous tall, duplicated caption/score rows below every world illustration. The compact gallery improves visibility on portrait mobile displays without inventing trophy/diamond rewards or locking worlds that the actual game makes playable.
- The previously shared menu/OS launch bitmap is now **two different build-time assets** for Android and iOS. Home retains the upper wordmark and fully functional live navigation. The startup image puts the flying Bopi first and the winged BOPAVI wordmark below, closer to the separate supplied splash reference. No fake progress bar was added.
- Android emulator QA now captures a genuine `android-worlds.png` screenshot of the interactive card grid. The verified release workflow publishes it as `BOPAVI-Android-Worlds.png` next to the existing authentic emulator/simulator shots. README marketing gallery shows the real scene.
- All offline progress keys, eight world indices, nine pilot IDs, level-unlocking rules, procedural flight, physics and currency handling are preserved. Android and iOS versions advance to **0.1.53 (build 56)** with source regressions.
- **This is a measured UI/composition improvement, NOT 1:1 3D illustration or pixel matching**. The original concept still exceeds the in-app 2D art detail and should not be presented as emulator output.

## v0.1.52 — floating-cloud worlds and premium live flight HUD (build 55)

- Replaced the actual build-time background generator's opaque ground-colored rectangle with layered, biome-aware foreground cloudbanks for all eight worlds. The fantasy islands now fade into clouds rather than terminating at a flat ground stripe. Night, lava, underwater and daytime palettes are independently respected.
- Redesigned the in-flight coin, star, level, shield and magnet HUD on Android Canvas and iOS Core Graphics using common midnight-blue glass chips, luminous cyan edges and reflective highlights. The virtual gold coin uses an illustrated crown instead of a generic dot.
- HUD values remain directly bound to real earned coins, collected stars, level ordinals, shield charges and remaining magnet seconds. The separate native Pause button stays functional; no fictional diamonds, hearts or rewards were added.
- Cached the iOS crown typeface rather than constructing a font each rendered frame. No per-frame asset decoding or downloaded artwork was introduced.
- Preserved all save identifiers, nine pilots, eight world IDs, procedural stream continuity, collision geometry, player input, camera timing and offline currency. Added source regressions and updated the Android/iOS versions to **0.1.52 (build 55)**.
- High-detail 3D sprite and castle/waterfall quality and all-ten-screen exact 1:1 screenshot comparisons remain incomplete; a green build alone is not proof of pixel-identical reference artwork.

## v0.1.51 — immersive gameplay scenery, verified screenshots and public README (build 54)

- Android and iOS no longer paint an opaque fixed-color stripe over the lower 49 logical pixels of the active 480×800 game world. The complete illustrated floating-island scenery remains visible on portrait devices, with letterbox sky handling preserved. Collision, camera, flight timing and native controls are unchanged.
- Rebuilt the public-facing README with the BOPAVI logo, official icon, original pilot illustrations, eight worlds, functional game description and four navigation icon assets. Developer implementation details are in QA/docs rather than in the marketing introduction.
- Every new GitHub Release now includes five extra **real, unmodified emulator/simulator screenshots** (Android Home, pilot picker, first-flight idle preview, results and iOS Home). The README gallery links to `releases/latest/download/` assets, not the ten concept references. Publication fails if any screenshot is absent, not a real PNG or has invalid dimensions.
- Audited twelve Kotlin/Swift source files, removed the unused Android `sunset` field plus three unused imports, and added a conservative CI dead-code regression checker for private declarations and Kotlin imports. Kept all platform callbacks, sprite fallbacks and save migration handlers.
- Maintains saved progress, all nine character mappings, eight worlds, difficulty/physics, earned-coin economy, accessibility and separate Android/iOS builds. Added source tests and documented audit limitations.
- **No unsupported 1:1 claim:** the existing art is still stylized 2D and does not yet match the high-resolution 3D concept render, and physical hardware/store signing are unverified.

## v0.1.50 — Bopi expression and animated feather-depth art (build 53)

- Refined the single source-of-truth Bopi illustration: azure iris gradients, double eye catchlights, curved brows, a visibly open orange beak with mouth and tongue, glossy head crest, bronze pilot strap highlights, and the existing blue scarf with the golden crown emblem. Moved the smile toward the face center so it stays visible above the scarf. These changes bring the mascot closer to the supplied premium references without pretending to recreate the original high-detail 3D artwork.
- Removed a background-only painted shadow oval from moving Bopi flight sprite output; it remains available in the menu illustration without following the character during gameplay.
- Added six separate cyan and blue wing-feather highlights (three per wing). Replaced the brittle four-path selector in the build-time image generator with explicit left/right wing path markers, keeping every highlight attached to its real animated wing rather than painting static feathers onto the torso.
- Android and iOS still use the same generated Bopi sprite source, with nine preserved character indices, 18 independently animated wing sprites and nine full-wing menu portraits. Other pilots and level art are unchanged.
- Added regression coverage of SVG structure, gradients, body-versus-wing extraction, and original nine-pilot mappings. Version 0.1.50 / build 53.
- No changes to physics, hitboxes, procedural levels, saved progress, rewards or offline purchasing. Pixel-perfect visual matching, hardware performance and signed store publishing are not claimed.

## v0.1.49 — unobscured home logo and illustrated bottom navigation (build 52)

- Corrected the overlapping home artwork observed in the real Android emulator screenshot: the winged BOPAVI logo is now placed below the live top wallet/avatar/settings controls in the shared 900×1600 art generator.
- Replaced platform-specific decorative chess/star/diamond/box symbols with four original, stylized vector icons: Profile, Tasks, Collection and Shop. Android/iOS rasterize the same four SVGs during CI, and all controls remain independent, accessible and functional.
- Adjusted the shared Bopi illustration from the legacy bright-pink scarf/tail toward the reference's royal-blue scarf and gold crown insignia; the generated body sprites and menu art use the same source.
- Added build-time checks for byte-identical Android/iOS icon PNGs, dimension checks, source-level navigation checks, QA instructions and improved public README copy.
- Preserved the entire offline economy, all existing saved characters and levels, physics, flight control and pause lifecycle.
- The original high-resolution reference renders remain a fidelity target; matching the icon identity and removing logo overlap is measurable progress, not a claim of full 3D/pixel 1:1.

## v0.1.48 — first-flight accessibility and idle performance (build 51)

- Android Canvas no longer continually invalidates its static first-flight preview. The first tap immediately starts normal rendering, with reduced-motion support unchanged.
- iOS Core Graphics now presents the matching DODIRNI ZA LET in-game label. CADisplayLink runs only during active, unpaused gameplay and resets elapsed-time state across resume.
- Android TalkBack click and iOS VoiceOver activation execute the same flap path as normal touch, rather than being inert. The first-flap boost and pause reveal remain single-shot.
- Added source-level regression guards and version parity. Physics, score calculations, pilot save IDs, coin balances and level progression remain unchanged.
- Android/iOS v0.1.48 (build 51) requires green emulator/simulator/build QA before release. Exact pixel fidelity to the premium reference illustrations is not claimed.

## v0.1.47 — real illustrated home header navigation (build 50)

- Brought the Android/iOS home header closer to the supplied BOPAVI reference: the current unlocked pilot avatar is now visible, alongside separately tappable best-score and earned-coin capsules and an actual settings shortcut. All four are accessible buttons with real navigation, not flat screenshot elements.
- Avatar opens offline Profile; high score opens Achievements; earned coins open the existing offline coin shop with home return semantics; gear opens Settings. Preserved navigation on both platforms, game progress, existing pilots, levels and earned-coin economy. No unsupported gem counter, cloud accounts, payment UI or promotional ads.
- Made narrow-phone top controls 48dp/pt high with equally expanding data capsules and accessible descriptions; icons remain independent of artwork. Existing bottom four-tab navigation and hero remain functional.
- Added cross-platform source regression checks and manual emulator accessibility/layout checklist. Android+iOS v0.1.47 (build 50). Exact pixel parity with supplied reference graphics remains unverified.

## v0.1.46 — honest locked-page navigation and page status (build 49)

- Fixed a cross-platform UX bug introduced in the v0.1.45 future-page preview: locked preview-only pages still exposed **NASTAVI LET**, which launched the saved frontier rather than any level shown on screen. The Continue action is now rendered only on pages containing unlocked levels.
- Added a real current/total page indicator and saved world star summary on accessible pages; locked pages are explicitly labeled **ZAKLJUČANI LEVELI** and provide a clear return to the unlocked progression frontier.
- Preserved the 20-tile native UI, forward/backward page navigation, 1,048,576 procedural levels per world, all existing skin pricing, offline progress keys, historical star ratings, existing audio/pause lifecycle and collision physics.
- Added Android/iOS parity regression checks for CTA scope, page arithmetic, and locked preview behavior. Also fixed CI startup recovery for a verified *system Pixel Launcher* ANR before the first screen check and bounded picker retry loops; no application crashes are suppressed. Android/iOS version v0.1.46 (build 49).
- Exact screenshot-to-reference pixel parity and production store signing remain separate, unverified requirements.

## v0.1.45 — locked level preview navigation (build 48)

- Fixed Android and iOS LEVELI paging: SLJEDEĆIH 20 previously vanished for locked future pages. Now the player can preview future groups of 20 locked levels in every world.
- Every level's saved lock state, recorded stars and launch restrictions stay intact. A page with no unlocked level hides the numeric launch control and displays a clear VRATI NA OTKLJUČANE LEVELE action; NASTAVI LET still uses the current saved frontier.
- Preserved the 1,048,576-level upper bound per world and all offline saves, rewards, world identifiers and physics. Added boundary/source regression checks.
- Android and iOS v0.1.45, build 48. Exact visual 1:1 similarity still requires comparison with real device screenshots and production art.

## v0.1.44 — persistent music volume and live pause buttons (build 47)

- Fixed both platforms' **MUZIKA** pause toggle restoring an arbitrary 80% instead of the user's last custom music volume. Android and iOS now retain the last nonzero value in a backward-compatible local key and restore that exact setting after muting, including after app restart.
- The native Pause ZVUK and MUZIKA buttons now show ✓ or ✕ based on actual persisted state and refresh on interaction. Music remains paused during toggles until the user explicitly chooses NASTAVI.
- Retains every existing character, biome, earned coin, level and v6 backup schema. No new network use, fake currency or remote data. Added Android/iOS source regression checks. Version 0.1.44 build 47.
- Reference-image 1:1 visual fidelity remains unverified.

## v0.1.43 — Context-aware navigation (build 46)

- Fixed a cross-platform navigation defect: home footer ZADACI, KOLEKCIJA and TRGOVINA previously returned to Settings instead of Home. Back now follows the actual entry point, including Profile → skins/achievements and Shop → skins → Shop.
- Earned-coins purchases and bonus claims preserve the original Home/Settings return route across screen refreshes; original pricing and save keys are unchanged.
- Android system/predictive Back matches the native visible Back action on submenus rather than skipping nested world/level pages. Back during an active flight still shows Pause, and exiting Home still behaves normally.
- No changes to physics, nine original skins, eight worlds, local saves, offline privacy or branded graphics. Android and iOS v0.1.43 build 46.
- Release requires green Android 15/16 emulator QA, iOS simulator and device build, and five verified GitHub release assets. Visual 1:1 concept parity remains unverified.

## v0.1.42 — premium flying-pilot preview motion (build 45)

- Added a subtle floating motion to the chosen pre-flight bird on Android and iOS, keeping scenery, character badge, buttons and accessibility semantics stationary. This is purely presentational; gameplay motion, collision boxes, inventory and rewarded coins are unchanged.
- Respects Android disabled system animations and iOS Reduce Motion; the selected portrait remains fully visible when motion is off.
- Android ObjectAnimator is bound to View attach/detach lifecycle and canceled on removal to avoid repeat-animation leaks across menu navigation. iOS uses a CALayer animation tied to the portrait view.
- Versioned Android/iOS 0.1.42 (build 45); added source regression safeguards and QA notes. Require both Android emulator builds, iOS simulator/device, privacy/asset audit and all five checksum-verified release files.
- Exact 1:1 reference sprite and typography appearance remains unverified.

## v0.1.41 — full-bleed portrait menus and responsive safe-area controls (build 44)

- Reviewed actual v0.1.40 Android emulator captures. The device status clock/stripe was still visible above premium menus and results, unlike the full-bleed game reference.
- Unified Android status-bar-hidden mode across all portrait menus and results, matching gameplay. Preserved the bottom navigation gesture/system area and camera cutout safe insets (Android 15/16).
- Hid the iOS status bar across menu and gameplay, while retaining UIKit safe-area constraints for Dynamic Island/notch and bottom controls.
- Added automatic font size reduction for genuine high-score / currency wallets and narrow Android bottom-tab captions.
- Retained gameplay, skins, local saves, level IDs, privacy and existing native art. Android/iOS v0.1.41 build 44 with new source regression and visual QA gates.
- Existing flat illustrated sprites and backgrounds still do NOT meet the user's original premium 3D reference at 1:1.

## v0.1.40 — illustrated nine-pilot gallery (build 43)

- Rebuilt Android and iOS character cards with world-art backdrops, full existing winged portraits, overlaid live names, selection/lock badges and earned-coin state labels. Added highlighted gold chosen frame and purple premium-locked frame.
- Retained all nine character indices, earned virtual-coin costs and confirmation logic; no remote assets, purchase service or fake gameplay statistics. Responsive three-column layout uses concise text scaling.
- Kept gameplay, saves and eight worlds unchanged. Added cross-platform source guards and QA criteria. Android/iOS version 0.1.40 build 43.
- Exact 3D sprite and visual 1:1 parity remain unverified until approved production assets and device comparisons are provided.

## v0.1.39 — In-place pause settings without losing the flight (build 42)

- Fixed an actual navigation bug on **both Android and iOS**: the pause menu's Settings action previously opened the standalone settings screen and disposed the active flight. It now expands a live controls panel **inside the paused overlay**, without touching the simulation or level.
- Added persistent music and sound-effects volume sliders (0–100%) and a haptics switch, reusing the existing local settings store and original branded blue/gold native control styling. No mock controls or baked-in screenshot UI.
- Audio stays paused while changing slider values and only resumes by explicitly choosing Continue. Cancel, Retry, Exit and separate main-menu settings still use their original explicit behavior.
- Responsive pause panels remain vertically scrollable, and both platforms expose accessible controls. Source regression tests prevent reintroducing navigation away from the active canvas.
- Android and iOS v0.1.39 / build 42, existing v6 backups and physics untouched. Requires successful Android API35/API36 emulator smoke, Swift simulator/device and save tests, authentic Play images and five checksum-verified release assets.
- Remaining gap: exact character rendering, logo typography and pixel-by-pixel UI parity to the supplied concept images are not yet established.

## v0.1.38 — illustrated world-card layout and compact native typography (build 41)

- Reworked every Android and iOS world-gallery card using the same existing real biome artwork, with a readable **live title over the image**, a dark native contrast footer and a compact display of true saved progress.
- Matched the reference's blue/gold active selection while keeping 8 worlds in a 2-column layout. Improved tap feedback and selected-world accessibility; no interactive behavior is baked into imagery.
- Consolidated live LEVEL, collectible, earned-stars-out-of-30 and score record into two legible compact rows, with automatic text scaling for long titles and numbers on smaller devices. iOS/Android card sizing and layering are aligned.
- Kept character art, physics, seamless cross-level flight, all virtual currency, replay, offline save data, world IDs, and per-level stars unchanged.
- Added precise source regression checks guarding accessible interaction, world-card overlay and genuine source-driven metrics; Android emulator navigates all 8 cards.
- Updated Android/iOS version to 0.1.38 (build 41); release requires Android 15/16 emulators, iOS simulator and unsigned device, visual QA, authentic Play screenshots, and five version-matched release artifacts.
- Detailed reference sprite/typography 1:1 fidelity is still unverified; device screenshot pixel-diff approval requires separate authorized production art.

## v0.1.37 — Eight reference-matched world identities and biomes (build 40)

- Aligned the eight visible world names on Android and iOS to supplied reference designs: Zeleni otoci, Kristalno nebo, Lava vrhovi, Noćni dvorci, Biserno more, Nebeske kule, Čarobna šuma, Zvjezdani bezdani.
- Updated thematic collectibles, descriptive obstacles, accessibility names, README world table, active-card accent colors and emulator world-name assertions.
- Reused and remapped eight existing local decorative SVG world-scene systems to the new visual order, adding a distinct enchanted-forest overlay with mossy trees, golden mushrooms and fireflies to the seventh world. Both platforms receive pixel-identical generated PNG bytes, with no network access or UI baked into raster backgrounds.
- **Important compatibility guarantee:** save slot/world index, level generator seeds, physics, levels, unlocked progress, earned stars, scores and virtual currency were not remapped. Only descriptions and generated scenery change.
- Version 0.1.37/build 40. Production gate: Android 15/16, iOS simulator/device, Swift regression, offline privacy QA, original screenshots and all five GitHub Release artifacts.
- Standalone production 3D character renders, typography licensing and exact 1:1 pixel-diff approval remain pending; a generated scenic artwork update is not an assertion of pixel-perfect equivalence.

## v0.1.36 — Earned level stars and authentic world progress (build 39)

- Added honest **0–3 earned stars for each completed level**, collected from the actual per-level gameplay pickups rather than inferred from completion or fabricated to match concept art.
- The world gallery now shows **actual first-ten-level earned stars out of 30** for each world (not an invented global total), with real saved values reflected on both Android and iOS.
- Level grid now shows three gold/outline stars, separate lock/completion icons and spoken accessible ratings. Current/locked/completed state and level interaction remain governed by saved unlock progress.
- Preserved **continuous progression**: per-level stars reset only when the next level begins, while overall flight stars, scores, gates, and collision physics never reset during the run.
- Versioned offline backups to v6 and restored sparse best-star records with strict validation and backward compatibility for v1–v5. No network/cloud account introduced.
- Default level-number entry now selects the unlocked level in the visible page rather than confusing a page index with a level ID.
- Added Swift round-trip/corrupt-backup assertions, Android/iOS source parity checks and Android emulator world accessibility checks.
- Android v0.1.36 (build 39); iOS v0.1.36 (build 39). GitHub still produces a debug APK, unsigned AAB, simulator build and unsigned iOS device files.
- Visual fidelity caveat: supplied 3D art remains a **reference**; high-resolution separate characters, UI texture assets and pixel-by-pixel screenshot comparison remain necessary before any 1:1 claim.

## v0.1.35 — Live pilot result hero and truthful personal record (build 38)

- Replaced the generic result hero on Android and iOS with a layered composition of the **actually selected pilot portrait** over the **actually played world artwork**. This moves the result screen closer to the supplied Bopi result reference while preserving real text, accessible views, currency balances and tappable actions.
- Added a gold highlighted NOVI REKORD banner **only** when the current scored run strictly exceeds the best saved score from before recording the run. Equal scores or no-score runs never claim a record.
- Corrected result audio parity: winning uses the bundled level-success sound; failed flights use the existing hit sound. The stored result is still recorded once, without affecting physics or save compatibility.
- Added source regression assertions and a real Android emulator result-screen accessibility gate for the selected pilot/world, preventing accidental replacement with a generic fixed image or fake record label.
- Android and iOS move together to version 0.1.35, build 38. The release remains an offline game; store signing stays outside GitHub CI.
- Final 1:1 identity with the 3D reference artwork is **not** claimed. High-detail 3D character exports, typography licensing and pixel-diff approval remain outstanding.

## v0.1.34 — Premium profile, settings and retry-safe screenshot artifacts (build 37)

- Fixed a user-visible navigation defect: the PROFIL home shortcut previously opened Settings. Android and iOS now show a dedicated offline profile with the selected real pilot illustration, saved player name, personal best, earned currency, completed levels and wins.
- Profile actions navigate to actual character selection, player-name settings and earned achievements, with accessible saved-data descriptions and a working back-to-home path.
- Moved actual vibration control immediately below the music and effects sliders, matching the reference settings hierarchy. Kept master audio and reduced-motion controls functional under Additional Options; both native platforms use the same ordering and navy/cyan surface language.
- Added Android emulator click-through validation for PROFILE and Back, source parity checks, and regression assertions for settings order and real profile content.
- Resolved a real main CI release blocker: rerun after an Android emulator failure retained two same-name screenshot artifacts, and Play packaging downloaded the older incomplete ZIP. QA upload now overwrites partial prior attempts and Play packaging rejects ambiguous artifacts before reading real screenshots.
- Both native version/build values advanced together to 0.1.34 / 37. The validated unsigned AAB publishing policy from v0.1.33 remains intact.
- Fidelity note: references include cloud sync, logout, gems and video rewards, none of which is a real offline BOPAVI feature; they remain intentionally absent. Actual 3D art pixel matching is not claimed.

## v0.1.33 — Verified unsigned releases and version-safe automation (build 36)

- Fixed an actual release pipeline maintenance defect: v0.1.32 was hardcoded in CI job labels, publishing tags, artifact paths, validation and Play archive output, making the next release require synchronized manual edits in several places.
- Added `tools/release_metadata.py` to verify Android/iOS version/build parity and generate the exact tag and Play filename, reused by CI, marketing package and validation.
- Removed conditional Play keystore signing from the Gradle project. GitHub builds can never sign an AAB even when upload key environment variables are present. External publisher-controlled signing remains separate from GitHub.
- Build and publication gates now inspect AAB JAR signature material and require `jarsigner` to verify it is unsigned. An accidental signed bundle stops publication.
- Added synthetic signed/unsigned bundle regression tests and stale-version/Android/iOS build consistency tests; retained Android 15/16, iOS simulator/unsigned device and parity workflows.
- Game engine, nine pilot identifiers, saved ownership, offline progression and generated illustration sources are unchanged. This is a production pipeline and integrity fix, not a claim of 1:1 3D visual matching.

## v0.1.32 — Premium game experience and reliable release automation (build 35)
Released after the accumulated Android/iOS improvements since v0.1.31.

- Complete native UI updates to the home screen, selected pilot/gallery, results and rewards, pause, world browsing and difficulty settings; real saved numbers and accessible touch controls.
- Full-wing character portraits and richer eight-world art from shared asset-generation sources, with generated Android/iOS byte-for-byte parity.
- Accurate in-flight HUD, cumulative gates, preserved seamless-level physics and offline save behavior.
- Android build 35 / iOS build 35; Android API 36. No production-signing workflow on GitHub. Public AAB intentionally unsigned.
- Version release contract: every subsequent code change requires a new version and release; PR CI rejects versionName not exceeding the latest public release. Main CI publishes a tagged release only after complete Android/iOS QA and verifies any existing release has the exact tested commit and all five artifacts.
- Artwork quality has improved but remains stylized 2D; pixel-perfect matching to user-supplied 3D renders is not yet verified.

#### Included development milestones since the previous published release

### premium saved-progress world selection
- Updated Android and iOS eight-world cards to show **real saved frontier level, collected-item count and personal best score** for each world. Previously the cards showed an item category name but no actual earned amount.
- Retained two-column illustrated world selection, true clickable card, current-world highlight and original world order/collision/saves. Improved readability with white dynamic uppercase headings, an independent gold save-metric line, more deliberate spacing and Android illustration contrast.
- Every tile's TalkBack/VoiceOver description announces world name, unlocked status and actual progress/collection/record numbers. No invented 30-star completion counters, paywalls or world unlocks.
- Added Android UI test that opens the World gallery, swipes through all eight actual accessible cards, confirms their real metrics, returns home, and continues the existing gameplay QA. iOS source/visual semantics remain equivalent.

### premium interactive three-card difficulty selector
- Replaced Android's vertical RadioGroup and iOS's generic segmented difficulty control with **three equal-width, touch-accessible blue/gold illustrated cards** for LAGANO, NORMALNO and ZAHTJEVNO, matching the reference hierarchy.
- Each card displays an existing complete winged pilot illustration and brief Croatian challenge description; selected card gets a gold border and true persisted 0/1/2 selection. TalkBack/VoiceOver announce current selection.
- Kept the same offline difficulty physics multipliers and saved ProgressStore keys. Changes affect the **next** flight, never silently restart an active game.
- Android emulator QA actually scrolls, taps Lagano, confirms selected state, then restores Normalno and confirms persisted state. Both native source parity and settings-screen navigation remain tested.
- Still does not claim 100% exact 3D screenshot art or unimplemented cloud account/language functionality.

### complete illustrated winged pilot portraits
- Fixed an actual visual inconsistency: character picker, selected-pilot hero and in-game pause used **torso-only** `bopi0..8` assets, while flight independently rendered both wing layers.
- The shared build-time artwork generator now composites the original body and both detached wing layers for every one of the **nine** pilots into `bopiportrait0..8` (Android) / `BopiPortrait0..8` (iOS), adding restrained soft edge diffusion for legibility over the eight rich biomes.
- All gallery cards, selected pilot previews and native pause screens on both platforms now use the full-wing portrait. Gameplay continues to use separate body/left/right textures, preserving responsive flapping, original hitboxes, reduced-motion handling and animation performance.
- Strict asset CI confirms nine unique, full alpha transparency, non-torso portraits and byte-identical Android/iOS PNGs. No network, new permission, store purchase or save-schema change.
- Still not a pixel-identical replacement for the user's detailed 3D character references; approved SVG source shapes remain unchanged.

### richer offline world illustration layers
- Expanded the one-source generated art of all **eight** worlds with biome-specific details: faceted floating cliffs, additional towers, waterfalls, crystal spires, snowy summits, lava fissures, sky bridges, night castles, coral and an astral ring portal.
- Decorative landmarks are built deterministically into shared Android/iOS static raster textures. The native 480×800 flight physics grid, moving gate collision geometry, cached parallax rendering, saved progress and offline privacy model remain unchanged.
- No fake UI is baked into scenery; buttons, currency, pause controls and pickups remain actual code-driven interactive elements.
- This strengthens depth and scene identity but is **not yet 100% pixel-identical** to the supplied 3D concept references.

### premium in-flight HUD parity
- Android Canvas and iOS Core Graphics now render the same three real glass chips for **coins gathered in the current run**, **stars collected** and **current level**, while reserving the top-right touch area for the existing native pause control.
- A cyan/gold progress bar reflects actual gates passed within the current procedural level. A distinct, permanently cumulative **PROLAZI UKUPNO** counter does not reset on seamless level boundaries. Progress is not falsely labelled in meters.
- Existing real shield and magnet power-up quantities are drawn as matching blue status chips only when active. The preflight preview remains clean until the first tap.
- Removed the duplicate iOS UIKit status pill and the duplicate painted "PAUZA" panel on Android/iOS; the functional premium pause modal remains the only action sheet.
- No new network, currency, invented interactive items or gameplay physics changes. Original artwork still differs from the supplied 3D concept images.

### premium selection-first character screen
- Both Android and iOS display the three featured, user-reference characters **Portantin, Noa, Any** in the first row, followed by the other six characters in a 3×3 accessible gallery.
- Skin indices, owned masks, offline-earned coin costs, local save migrations and character purchase confirmation are unchanged by this visual reorder.
- Selected character preview now layers its actual portrait above the chosen world's art and a darkened readable overlay with selected badge. No mockup bitmap includes fake counters or buttons.
- The primary **POLETI S…** CTA appears after the choice grid, requiring an explicit preflight confirmation as in the supplied reference. The gameplay pause/boost lifecycle is unaffected.
- Remaining 3D fidelity gap: actual game art still uses the earlier flat illustrations, not the separately rendered high-detail pilot sprites in the user-supplied concepts.

### premium result and real reward cards
- Android and iOS now render three compact summary cards (actual run score, collected coins, total passed gates) and four reward tiles (collected coins, stars, claimable score-record bonus, persisted best score) matching the structure of supplied BOPAVI result references.
- Claimable score milestones use the **existing one-time offline claim API**. Tapping claim updates the wallet and reward label immediately without rerunning the game result or crediting the same milestone twice.
- Replaced long flat footer actions with three real compact buttons for leaderboard, home, and native sharing via Android chooser or iOS activity controller. World map and offline-coin shop remain accessible.
- Native layout accommodates narrow phones using 3 equal-width results, 4 equal-width reward cells, dynamic text sizing and vertical scrolling.
- No fictitious diamonds, video rewards, premium chest inventory, backend features or GitHub signing. Actual original vector artwork is not yet pixel-identical to supplied 3D concept renders.

### premium pause overlay
- Android and iOS now use a **real scrollable premium pause modal** instead of the old system list/action sheet, including live selected flyer, cyan/gold PAUZA header, orange Continue, blue Retry and gray Exit.
- Four real shortcuts: master sound, music-volume toggle, controls help and settings. Every action is connected to the offline sound/settings system; no fake logout, cloud service or store.
- Simulation remains paused throughout the panel and any nested help dialog. Android Back/cancel resumes; explicit Continue resumes, while Retry/Exit/settings perform intentional screen transitions.
- System-level pause visibility remains off until the first actual flight flap. Gameplay physics/save files unaffected.

### premium reference alignment, iteration 2
- Android/iOS: illustrated 20-level grid with dynamic badges, real unlock/complete states, numbered world thumbnail cards and compact previous/next navigation; no fabricated star medals.
- Android/iOS: independent, working music and effects volume sliders (0–100%) with immediate mixer updates; percentages persist locally and are included as optional, backward-compatible fields in v5 backups.
- Preserves first-flap pause, existing procedural flight/collisions, 9-pilot local inventory, offline progress and unsigned GitHub AAB policy.
- The supplied photorealistic concept illustrations are not yet replicated pixel-for-pixel in the original flat vector scene art.

### premium reference-aligned interface
- New shared BOPAVI blue/gold logo and motto, rendered from SVG into Android and iOS.
- Rebuilt illustrated menu backgrounds, golden play buttons, cyan/blue secondary actions, 4 working home shortcuts, and three-column nine-pilot selection.
- Updated Android/iOS settings accents and results panels while keeping real earned currency, scores and progression.
- The exact reference images remain design targets; separately prepared production art and pixel-diff QA are still required for 1:1 fidelity.
- No new online services, sign-in, real-money purchases, or signing workflows.

## v0.1.31 — endless-flight coordinate precision (build 34)
- Android 15 emulator CI: when a verified **Pixel Launcher** system ANR overlays a healthy BOPAVI picker, terminate only the stuck launcher instead of repeatedly pressing Wait. Continue to require real scrolling and accessible Portantin, Noa, and Any cards; never dismiss an ANR originating from BOPAVI.
- Removed CI's disposable signing-key smoke job and disabled the manual GitHub signing workflow. APK debug builds and **unsigned** AAB remain available. Any future production upload signing is strictly offline.

- Android versionCode 34 / versionName 0.1.31; iOS build 34 / marketing version 0.1.31. Confirm in Play Console that 34 has not already been used before signing/uploading.
### Fixed
- Android and iOS collision positions now subtract the camera's traveled distance in double precision instead of accumulating increasingly imprecise Float world coordinates.
- Current-level and pre-rendered upcoming gates share the same precise world-coordinate frame when an endless level is promoted.

### Improved
- Extended the Kotlin parity and Android JVM regression suites with a simulated 2^25-unit marathon camera shift and quarter-pixel collision assertions.
- Strengthened the cross-platform source audit to require matching double-precision coordinate calculations.

### Changed
- The existing public Float distance remains available to Android/iOS scenery renderers; flight physics, scoring, level generation, saved progress format and UI flow remain unchanged.

### Added
- No new gameplay features in this focused regression fix.

### Security
- No new permissions, services, telemetry, third-party SDKs, or network requests.

## v0.1.30 — priprema Google Play izdanja i Android 16
- Android minSdk 26 / compileSdk 36 / targetSdk 36. Usklađenje s Google Play zahtjevom od 31.8.2026.; versionCode 31 / versionName 0.1.30, iOS build 31 / 0.1.30.
- Android 13+ moderni OnBackInvokedDispatcher (Android 16 više ne usmjerava prediktivnu gestu kroz zastarjeli onBackPressed); aktivni let pokazuje pauzu, idle prikaz vraća izbor lika, dok stariji Androidi koriste kompatibilni put.
- Manifest appCategory=game, bez INTERNET dozvole i s isključenim OS automatskim backupom. Google Play upload-key signing opcionalan i dopušten samo kroz zaseban, ručni workflow koji koristi GitHub Environment tajne; redovni CI i javni release izričito ostaju nepotpisani.
- Izvornim brand elementima generirana 512×512 Play ikona i 1024×500 promotivna grafika, zajedno s autentičnim 9:16 izrezima stvarnih Android CI screenshotova; bez izmišljenog gameplaya. Priloženi mirujući gameplay kadar nije prikaz aktivnog leta.
- Dodani API36 AAB validacija, detaljni hrvatski Google Play publishing vodič, Play listing, Data safety analiza, dvojezična politika privatnosti i ručni release checklist. Play Console objava i fizički QA ostaju obveza izdavača.

## v0.1.29 — siguran uvoz, stvarni broj prolaza i pouzdana pauza
- iOS import sada validira cijeli v5 niz streamFrontiers prije bilo kakve promjene spremljenog stanja. Ranije je neispravan niz mogao ostaviti prepisane kovanice, odabrani svijet i markere već preuzetih nagrada. Swift regresijski test uspoređuje potpunu sigurnosnu kopiju prije i nakon odbijenog uvoza.
- Završni zaslon Androida i iOS-a prikazuje totalPassed kroz sve neprekinute levele, a ne passed koji se resetira pri svakom novom levelu. Kotlin, Swift i JVM testovi dodatno potvrđuju očuvanje globalnog brojača.
- Android tipka Natrag tijekom aktivnog leta otvara postojeći izbornik pauze umjesto prekida i gubitka pokušaja. Prije prvog zamaha vraća se izbor lika bez trošenja opreme; na početnom zaslonu Natrag standardno zatvara aktivnost.
- Android emulator: stabilizirana provjera stvarno pomične galerije nakon potvrđenog Pixel Launcher ANR-a. Dopušten je samo strogo prepoznat sustavni oporavak, bez preskakanja provjere Portantina, Noe i Any ili zataškavanja pada aplikacije.
- Proširen Python audit i QA upute; Android/iOS verzija 0.1.29, build 30. Fizika, sudari, grafika likova i format spremljenih podataka nisu mijenjani.
- Potpunu vizualnu podudarnost na fizičkim uređajima treba posebno potvrditi.

## v0.1.28 — stabilan izbor Noe/Any i poštena potrošnja opreme
- **Ispravljen pad Android aplikacije:** zaslon ODABERI LIKA imao je sedam slika u polju, a indeks odabranog lika mogao je biti 7 ili 8 (Noa/Any). Pregled sada koristi svih devet portreta i ne pristupa polju izvan granica.
- **Štit i magnet čuvaju se do početka leta:** Android i iOS koriste previewPerks() bez trošenja zaliha na mirujućem prikazu, a consumePerks() poziva se točno jednom na prvi stvarni zamah. Odustajanje prije zamaha ne troši kupljene predmete.
- **Povratak iz pozadine:** pozivi, zaključavanje i prebacivanje aplikacije pauziraju započeti let, ali ne zamrzavaju mirujući pregled bez tipke pauze. iOS uklanja suvišnog drugog lifecycle promatrača i obnavlja zvuk na mirujućem zaslonu.
- Regresijski Python audit provjerava indeks svih devet Android portreta, sigurnu potrošnju i lifecycle. Swift save test potvrđuje da pregled ne smanjuje broj štitova ili magneta, a prvi let ih troši.
- Android versionCode 29, iOS build 29, verzija 0.1.28. Sačuvani napredak, fizika, kolizije i neprekinuti leveli ostaju nepromijenjeni. Testiranje fizičkih uređaja i dalje je potrebno.

## v0.1.27 — originalni Noa i Any tijekom stvarnog leta
- **Ispravljena konkretna regresija iz v0.1.26:** Android je za Nou i Any prikazivao Portantinova krila, dok je iOS prikazivao i Portantinovo tijelo. Tijelo, lijevo i desno krilo na obje platforme sada koriste jedinstveni validirani indeks 0–8.
- CI uspoređuje stvarne generirane Android/iOS PNG spriteove **piksel po piksel** za svih devet likova i osamnaest krila, te provjerava da krila Noe i Any nisu Portantinova.
- Source audit provjerava točno učitavanje tri spritea te sprječava ponovnu pojavu ograničenja na sedam likova.
- Android v0.1.27 (versionCode 28) i iOS v0.1.27 (build 28). Napredak, postavke, kupnje virtualnim kovanicama, fizika, sudari i neprekinuti let nisu mijenjani. Merge i izdanje dopušteni su tek nakon zelenog CI-ja.

## v0.1.26 — Noa i Any, dva premium lika
- **Noa** i **Any** dodani su kao osmi i deveti lik. Noa je mladi nebeski istraživač s električno plavim krilima i pilotskim vizirom; Any je djevojčica s ljubičasto-ružičastim krilima, zvjezdanom tijarom i čarobnom haljinom. Izvori `docs/assets/noa.svg` i `docs/assets/any.svg` su zasebne ilustracije, ne preslikane Bopijeve boje.
- Oba lika dobivaju odvojene animirane spriteove lijevog i desnog krila iz istog SVG-a na Androidu i iOS-u. Galerija prikazuje devet pravih portreta i oznake premium cijena.
- Noa košta **220**, Any **240** lokalno osvojenih kovanica. Kupnja se potvrđuje prije potrošnje; već kupljeni izgled bira se bez nove naplate. Nema trgovine stvarnim novcem.
- Sačuvan je redoslijed postojećih sedam likova te v5 JSON pohrana i bitmaska vlasništva. Dodani su izvorni audit, novi iOS testovi kupnje/obnove i CI provjera 9 raznih spriteova s 18 odvojenih krila.
- Android i iOS verzija **0.1.26**, build **27**. Nisu mijenjani fizika leta, rezultat, lokalni napredak, osam svjetova ili režim bez interneta.
- Potpuna 1:1 podudarnost svih zaslona na stvarnim uređajima tek se treba potvrditi vizualnim QA-om.

## v0.1.25 — galerija svih likova i precizan ritam prepreka
- Android i iOS više ne prikazuju samo tekstualne gumbe za Bopija, Sunny, Berry, Luna, Mint, Shadow i Portantina: izbor prije leta i Postavke koriste zajedničku galeriju od sedam ilustriranih kartica u dva stupca. Svaka kartica sadrži stvarni sprite, ime i status, a odabrani lik ima istaknut obrub. Čitači zaslona vide jednu jasnu akciju po kartici umjesto dvostrukih natpisa.
- **Potvrda trošenja kovanica**: odabir zaključanog lika s dovoljno lokalno osvojenih kovanica otvara dijalog s točnom cijenom i mogućnošću odustajanja. Tek izričitom potvrdom provodi se postojeća atomarna operacija `selectOrBuy`. Dostupni likovi i besplatni Portantin biraju se bez plaćanja i ostaju spremljeni. Nema stvarnih novčanih uplata ni zahtjeva za mrežom.
- **Kontinuitet svake zone**: sljedeći proceduralni level unaprijed se priprema kao i prije, ali uvodni razmak sada nasljeđuje stvarnu udaljenost između posljednje dvije prepreke tekuće zone. Ranijih fiksnih 242 virtualne jedinice uzrokovalo je sitan skok razmaka na višim zonama. Ni pozadina ni ukupna prijeđena udaljenost ni igrač ne resetiraju se.
- Dodani Kotlin JUnit i Kotlin/Swift parity testovi za sve osam svjetova i šest izazovnih zona, uključujući posljednji level ciklusa. Python statički audit provjerava novi model galerije, sedam stvarnih spriteova, pristupačnost i eksplicitnu potvrdu virtualne kupnje.
- Android `versionCode 26` i iOS build `26` (oba `0.1.25`). Očuvani su format sigurnosnih kopija, lokalni najbolji rezultati, vlasništvo likova, nagrade, fizika, kolizije i svih osam slobodno dostupnih svjetova.
- Za tvrdnju o potpunoj vizualnoj jednakosti svih zaslona 1:1 još je potrebna provjera na fizičkim Android/iOS uređajima.

## v0.1.24 — Portantin, izbor lika i neprekinuti leveli
- Android i iOS beskrajni let sada unaprijed generira sljedeći level i iscrtava njegove prepreke prije nego prethodni završi. Predaja između levela koristi unaprijed izračunate pozicije prepreka umjesto stvaranja novih usred ekrana; zadržani su kamera, udaljenost, fizika, bodovi, novčići i krila.
- Uklonjen je vizualni prijelazni natpis i napredak po levelu koji se vraćao na nulu. Prikazuje se **ukupan broj prolaza u tekućem letu** koji raste bez prekida.
- Novi **ODABERI LIKA** prikazuje se prije svakog leta na obje platforme. Svi likovi imaju pregled i spremaju se lokalno; boosteri se troše tek po potvrdi **POLETI S...**.
- **Portantin** — besplatan, originalni krilati čovječuljak s naočalama, zaštitnom maskicom i rukavicama koji nosi suputnika na leđima. Uređivi izvor `docs/assets/portantin.svg` generira tijelo i dva odvojena krila za Android i iOS. Dosadašnjih šest Bopijevih oblika ostaje i cijene/kupljeni izgledi se ne resetiraju.
- Uvoz nepoznate JSON kopije na Androidu i iOS-u čita najviše 550001 znak/bajt prije parsiranja, umjesto neograničenog `readText`/`Data(contentsOf:)`. Validacija formata i raspona ostaje.
- Kotlin JUnit, Kotlin/Swift parity, Android emulator odabira lika i audit izvornih resursa prošireni su za kontinuirani let, spremanje odabranog lika i zaštitu privatnosti. Obnovljen glavni README s vizualima, opisima sedam likova i uputama.
- Android 0.1.24 / versionCode 25, iOS 0.1.24 / build 25. Ovo izdanje ne predstavlja potvrdu potpune 1:1 usporedbe svih ekrana na fizičkim uređajima.

## v0.1.23 — prvi dodir započinje let, pauza više ne smeta uvodnom ekranu
- Ispravljen stvarni uzrok prijevremenog pojavljivanja gumba pauze: i Android i iOS sada ga stvaraju skrivenog i otkrivaju tek na događaju **prvog uspješnog zamaha krila**, kada simulacija prijeđe iz mirovanja u aktivan let.
- Uvodni zaslon leta više ne prikazuje HUD, status zaštite ni napredak kroz prepreke prije prvog dodira; nakon prvog zamaha svi potrebni pokazatelji postaju dostupni. Početni izbornik nema gameplay kontrole.
- Android `GameView.onFlightStarted` i iOS `GameCanvas.onFlightStarted` su jednokratne promjene stanja koje ne mijenjaju ubrzanje, kolizije, početni broj zamaha ni pravila bodovanja. Ponovljeni dodiri ne kreiraju nove gumbe pauze.
- Android ignorira zastarjele `onFinished` povratne pozive kad se igrač već nalazi izvan tog leta. iOS također provjerava trenutačni `GameCanvas` prije rezultatskog prijelaza.
- Android emulator gameplay QA sada provjerava četiri stvarna UI stanja: početni izbornik bez pauze, mirujući let bez pauze, aktivni let s pauzom i završni rezultat bez pauze. Uz to su prošireni Kotlin JUnit, Kotlin/Swift parity i Python source audit.
- Android `versionCode 24` / iOS build `24`, oba `0.1.23`. Sačuvane su lokalne postavke, spremljeni napredak, svih osam otvorenih svjetova, novčanik, rezultat i potpuno offline izvođenje.
- Priloženi referentni dizajni ostaju cilj; bez usporednih snimki svih zaslona na fizičkim uređajima nije opravdano tvrditi potpunu 1:1 podudarnost.

## v0.1.22 — razlike među levelima i jasnija navigacija (Android + iOS)
- Kartice pojedinačnih levela sada prikazuju **Normalni ●, Izazovni ⚡, Bonus ✦ ili Elitni ♛** na temelju stvarnog `LevelEngine.create` / `BopaviCore.create` tipa levela. Vrste nisu izmišljene i ne mijenjaju generator, pravila ni razinu težine.
- Android bojom razlikuje otključane kategorije, a iOS naglašava odgovarajući tematski rub. Na obje platforme zaključani leveli prikazuju lokot; pristupačni opis razlikuje vrstu i status (dovršen/otključan/zaključan).
- Iznad mreže prikazuje se stvarna zona i raspon 20 ponuđenih levela uz legendu četiri vrste. Stranice i ručni unos levela rade s postojećim ograničenjima napretka.
- Android i iOS pauza u naslovu prikazuje aktualni level i u pozadini ispravno upućuje igrača da odabere „Nastavi let” umjesto netočne upute o samom dodirom na tipku.
- Dodani Kotlin JUnit, Kotlin i Swift parity testovi za 4 vrste, uključujući granične levele 15, 30, 60, 90, 120 i 360, te provjere UI integracije na obje platforme.
- Android `versionCode 23`, iOS `CURRENT_PROJECT_VERSION 23`; oba `0.1.22`. Format spremanja, coins, shield, magnet, najbolje bodove, kolizije, gravitaciju i sav napredak ostavljamo netaknutima; svih osam svjetova ostaje odmah dostupno.
- Referentne slike služe kao vizualne smjernice, ali potpuna potvrda 1:1 svakog ekrana na stvarnim telefonima još nije provedena.

## v0.1.21 — čitljive kartice svjetova i pošten prikaz rezultata (Android / iOS)
- iOS kartice svih osam svjetova sada razdvajaju ilustraciju (gornji dio) i dva čitljiva tekstualna reda (naziv, stvarni otključani level i vrsta predmeta), po uzoru na postojeće Android kartice. Kartica i dalje ima jednu jasnu dodirnu površinu; ilustracija i unutarnji natpisi nisu samostalni, dupli accessibility elementi.
- Android i iOS označavaju trenutačno odabrani svijet naglašenim tematskim rubom i oznakom ✓. Odabir je samo vizualna orijentacija; svih osam svjetova i dalje je dostupno odmah i ne uvode se lokoti na svjetovima.
- Rezultati sada razlikuju **NOVI REKORD!** (rezultat veći od prethodnog lokalnog rekorda), **LEVEL DOVRŠEN!** (stvarna pobjeda) i **LET ZAVRŠEN!** (ostali pokušaji). Pobjeda ima prednost pred naslovom rekorda, a nula bodova i izjednačenja nisu prikazani kao novi rekord.
- Obje platforme nakon spremanja pokušaja prikazuju ažurirani **Najbolji rezultat**, uz osvojene bodove, odigrani level i kovanice. Logika naslova uspoređuje stari rekord *prije* upisa novog, čime izbjegava lažne oznake rekorda.
- Kotlin JUnit, Kotlin/Swift parity testovi i Python audit pokrivaju izjednačenje, novi rekord, pobjedu, redoslijed spremanja i čitljive kartice.
- Android versionCode 22 / 0.1.21 i iOS build 22 / 0.1.21. Nisu mijenjani format pohrane, rezultati, mehanika leta, kolizije, virtualna valuta ni offline arhitektura.
- Referentne slike su vizualni smjer; ovo izdanje ne dokazuje potpunu podudarnost svih zaslona 1:1 na fizičkim uređajima.

## v0.1.20 — Bopi reagira na dodir, življi zamasi krila
- Android Canvas i iOS Core Graphics sada povezuju zamah Bopijevih odvojenih krila sa stvarnim ulazom igrača. Svaki dodir kratko podiže krila do dodatnih 17° i postupno vraća pokret u osnovnu petlju, bez istezanja ilustracije tijela.
- Novi 240 ms vizualni impuls generira lagani obojeni val i čestice leta usklađene s temom trenutnog svijeta. Novi dodir obnavlja impuls umjesto da čeka završetak prethodnog.
- Prilikom uključivanja smanjenih animacija dinamički zamah i čestice su isključeni. Čestice se crtaju proceduralno, bez novih bitmapa, slika ili mrežnih zahtjeva tijekom animacije.
- Kotlin JUnit, zasebni Kotlin/Swift parity testovi i source audit provjeravaju trajanje, ponavljanje, ignoriranje nevaljanog delta vremena te prisutnost smanjenih animacija na obje platforme.
- Android versionCode 21 / 0.1.20 i iOS build 21 / 0.1.20. Fizika leta, sudari, bodovanje, pohrana napretka i sigurnosne kopije nisu mijenjani. Svih osam svjetova ostaje odmah dostupno.
- CI i stvarne snimke zaslona potvrđuju samo ono što su odradili; vizualna identičnost 1:1 s referentnim kompozitima još nije potvrđena.

## v0.1.19 — živi lebdeći otoci i usklađeno osvjetljenje (Android + iOS)
- U svih osam svjetova renderiraju se tri nova dubinska sloja lebdećih otoka s tematskim bojama, prozirnošću i brzinama pomaka. Android Canvas i iOS Core Graphics crtaju ih iznad ilustrirane pozadine i iza prepreka i Bopija — nisu statični screenshotovi.
- Deterministički izračun dubinskog pomaka (0,07 / 0,15 / 0,24 udaljenosti) ponavlja se nakon 696 virtualnih jedinica. Smanjene animacije zamrzavaju okoliš, a neispravna udaljenost vraća pomak na nulu.
- Ispravljen Androidov radijalni gradijent: mekani sjaj nestaje prema rubovima kao na iOS-u, umjesto da postaje neproziran.
- Prošireni Android JUnit, Kotlin i Swift parity testovi te Python audit prikaza, osvjetljenja i otključanog odabira osam svjetova.
- Android versionCode 20 / 0.1.19, iOS build 20 / 0.1.19. Nisu mijenjani sudari, gravitacija, brzina, rezultati ni pohrana profila, kovanica i sigurnosnih kopija.
- Referentne slike daju vizualni smjer, ali cjelovita usporedba 1:1 na fizičkim uređajima još nije potvrđena.

## v0.1.18 — stabilniji let i viša frekvencija osvježavanja na iOS-u
- Android/iOS: do 100 ms proteklog vremena nakon zastoja prikaza nadoknađuje se u ograničenim simulacijskim koracima od najviše 34 ms. Prethodni kod odbacivao je vrijeme iznad 34 ms.
- Ulazni NaN, beskonačne, nulte i negativne vremenske vrijednosti ne mijenjaju simulaciju. Dulji prekidi ograničeni su na 100 ms radi sprečavanja preskakanja prepreka.
- iOS CADisplayLink koristi maksimalno osvježavanje uređaja do 120 Hz (umjesto prisilnih 60 Hz), dok smanjene animacije ostaju na 30 Hz; iz frame callbacka uklonjena je redundantna provjera pauze.
- Kotlin JUnit i Kotlin/Swift parity testovi provjeravaju nadoknadu kratkog zastoja, sigurnu granicu nakon prekida i neispravne vremenske korake.
- Android versionCode 19 / 0.1.18 i iOS build 19 / 0.1.18; CI mora proći prije mergea i objave.
- Format spremljenog napretka, kovanica, rezultata i sigurnosnih kopija nije mijenjan. Vizualna usporedba 1:1 s referencama ostaje nepotvrđena.

## v0.1.17 — tri gumba, pregledne postavke i reakcija štita
- Android i iOS: početni ekran ima točno tri jasna gumba: veliki zeleni IGRAJ te dva plava SVJETOVI i POSTAVKE.
- Unutar postavki opcije su raspoređene u četiri cjeline: Izgled i zvuk, Igrač i težina, Dodatne opcije te Podaci i privatnost. Oprema, izgled Bopija i lokalna ljestvica dostupni su izravno iz postavki, uz povratak na početni ekran.
- Pri stvarnoj potrošnji štita Android i iOS pokreću samo jedan zvučni odziv i, ako je omogućeno, jednu vibraciju. Trajanje neranjivosti ne ponavlja obavijesti svakog kadra.
- iOS automatski pauzira gameplay i zvuk pri odlasku aplikacije u pozadinu. Let se ne nastavlja samostalno po povratku.
- Dodatni regresijski audit provjerava tri gumba, izbornike, povratnu navigaciju, reakcije štita i iOS lifecycle.
- Android versionCode 18 / 0.1.17, iOS build 18 / 0.1.17; GitHub izdanje uvjetovano uspješnim Android i iOS buildovima i emulator QA.
- Zaobljeniji zeleni i plavi gumbi, jasni blokovi postavki i dvije kompaktne informacije na početnom ekranu: najbolji bodovi i osvojene kovanice.
- Trgovina prikazuje samo virtualnu valutu zarađenu igranjem; štit, magnet i Bopijeve boje dostupni su bez stvarnog novca.
- Novi bonus: za svakih novih 1.000 bodova najboljeg rezultata korisnik može jednom preuzeti dodatnu kovanicu. Iskorišteni pragovi spremaju se u kompatibilne sigurnosne kopije kako se nagrada ne bi ponavljala.
- Tekstovi su pojednostavnjeni za igrače (bez verzija formata kopije, tehničkih izraza i razvojnih napomena).
- Android emulator sada stvarno otvara Postavke i provjerava povratak na početna tri gumba; Kotlin JUnit i SwiftSaves provjeravaju jednokratno preuzimanje virtualnih kovanica.
- Izgled 1:1 prema svim referencama nije potvrđen na fizičkim uređajima.

## v0.1.16 — neprekinut let i pokazatelj prolaza
- Android i iOS: novi kompaktni pokazatelj napretka trenutnog levela, sa stvarnim brojem prijeđenih prepreka, ukupnim brojem prepreka i trakom u boji odgovarajućeg svijeta.
- Pokazatelj se automatski vraća na 0 pri neprimjetnom prijelazu na sljedeći numerirani level; nema pauze, modalnog prozora ni dodatnog dodira.
- Položaj HUD-a ne preklapa gornju naslovnu traku, štit, magnet ni kratku oznaku prijelaza između levela.
- Tekst se osvježava tek kada se promijeni broj prolaza ili prepreka, bez dodatnog sastavljanja teksta u svakom prikazanom kadru.
- Pristupačni opis usklađen je na obje platforme i navodi koliko je prepreka igrač prošao.
- Novi Kotlin/Swift gameplay regresijski testovi i source audit provjeravaju reset brojača, točan omjer popunjenosti, osam paleta i kontinuitet leta.
- Android build 17 / 0.1.16; iOS build 17 / 0.1.16. Izdanje uvjetovano uspješnim Android/iOS CI-jem i emulator provjerom na main.
- Potpuna vizualna podudarnost 1:1 s referencama još nije potvrđena na fizičkim uređajima.

## v0.1.15 — ambijentalno osvjetljenje svih osam svjetova

- Android i iOS: zasebno nježno ambijentalno osvjetljenje za svih osam svjetova, s toplijim tonovima plaže/vulkana, hladnijim ledom/kristalima i diskretnim osvjetljenjem noćnih svjetova.
- Slika svijeta i Canvas/Core Graphics animacije sada se vizualno povezuju slojem radijalnog osvjetljenja koji se crta u istom koordinatnom sustavu.
- Android koristi osam unaprijed stvorenih RadialGradient shader objekata, a iOS osam predmemoriranih CGGradient objekata; tijekom crtanja ne stvaraju se nove slike ni gradijenti.
- Osvjetljenje ostaje potpuno statično kada su uključene smanjene animacije. Kolizije, brzine, otključani svjetovi i spremljeni napredak nisu promijenjeni.
- Source audit provjerava jednakost svih osam boja i centara, raspon osvjetljenja i odsutnost bitmap alokacija u render petlji.
- Android build 16 / 0.1.15 i iOS build 16 / 0.1.15. GitHub pre-release objavljuje se samo nakon zelenog CI-ja na main.
- Podudarnost 1:1 prema dostavljenim referencama i dalje zahtijeva vizualnu usporedbu na fizičkim uređajima.

## v0.1.14 — pokretne prepreke i ujednačene boje
- Pokretni stupovi i ostale dinamičke prepreke dobivaju diskretne pulsirajuće oznake unutar gornjeg i donjeg ruba; statičke prepreke ne dobivaju lažne indikatore gibanja.
- U modu smanjenih animacija oznake ostaju vidljive, ali miruju. Dinamičke oznake ne mijenjaju širinu otvora, hitboxove ni brzinu gameplaya.
- Android i iOS sada koriste identične boje svih osam svjetova za prikupljanja i oznake prepreka.
- Source regresije provjeravaju pokretne prepreke, granice četiriju oznaka, palete i odsutnost stvaranja bitmapa u petlji crtanja.
- Android versionCode 15 / v0.1.14; iOS build 15 / v0.1.14. Izdanje se objavljuje samo nakon punog zelenog CI-ja na glavnoj grani.
- Vizualno 1:1 prema referencama još nije potvrđeno na fizičkim uređajima.

## v0.1.13 — sjaj kolekcionarskih predmeta i reakcija štita
- Android/iOS: prikupljanje kovanica, zvjezdica i pogodnosti dobiva kratku animaciju deset čestica u boji pojedinog svijeta.
- Potrošnja štita pri sudaru dobiva dvanaest blagih radijalnih iskri, usklađenih s postojećim krugom zaštite.
- Povratne informacije vezane su uz stvarne `collectPulse` i `impactPulse` vrijednosti iz simulacije. Ne otvaraju izbornike i ne zaustavljaju kontinuirani let.
- Pristupačnost: za korisnike sa smanjenim animacijama dodatne čestice se ne crtaju; postojeće mirne oznake prikupljanja i udara ostaju.
- Render petlje koriste postojeći Paint/CGContext i determinističku geometriju bez novih bitmapa i objekata čestica.
- Dodane source-audit regresije za Android/iOS broj čestica, palete, trajanje i ograničenja opaciteta.
- Android build 14 / 0.1.13 i iOS build 14 / 0.1.13; izdanje se objavljuje tek nakon punog zelenog CI-ja na `main`.
- Vizualna podudarnost 1:1 prema referencama nije još potvrđena pregledom svih zaslona na fizičkim uređajima.

## v0.1.12 — živi svjetovi i animirani trag leta
- Android i iOS: osam tematski obojenih skupova svjetlucavih čestica koji se pomiču s krajolikom, uz kontinuiranu blagiju animaciju svjetline.
- Prednji slojevi svakog svijeta imaju proceduralne travke/kristalne detalje uz dodatni parallax; između numeriranih levela ostaje neprekinuti let.
- Bopi sada iza sebe ostavlja pet tankih zakrivljenih tragova pri aktivnom letu; dinamički detalji crtaju se Canvas/Core Graphics primitivima bez novih bitmapa i dekodiranja.
- U postavci smanjenih animacija gibanju okoliša zaustavlja se pomak/sway, a tragovi leta ne crtaju se.
- Dodani izvorni regresijski testovi za Android/iOS podudarnost broja čestica, pomaka i podrške za smanjene animacije.
- Android build 13 / 0.1.12, iOS build 13 / 0.1.12; GitHub pre-release uvjetovan svim zelenim CI provjerama.
- Izgled nije potvrđen kao 1:1 s dostavljenim referencama na fizičkim uređajima; rad na izvornoj ilustraciji i osvjetljenju ostaje otvoren.

## v0.1.11 — premium HUD, vibracije i provjera spremanja
- Android/iOS: zaseban prekidač „Vibracije pri igranju”. Prikupljanje predmeta aktivira taktilni signal samo kada je korisnik to omogućio.
- Android/iOS: štit i magnet imaju usklađene tamnoplave statusne oznake; prikazuju se preostali štitovi i vrijeme magneta.
- Optimiziran iOS HUD: font za statusne oznake koristi se ponovno umjesto stvaranja novog fonta svakog kadra.
- Preferencija vibracija prenosi se putem postojećih v5 sigurnosnih kopija; nedostajuća vrijednost u starim kopijama zadržava zadanu opciju.
- Ispravljene zastarjele pretpostavke testova o otključavanju svjetova; SwiftSaves sada radi u CI-ju s provjerom migracija i izvoza/uvoza vibracija.
- Regresijski izvorni audit potvrđuje HUD, postavke i oba renderera.
- Android build 12/version 0.1.11, iOS build 12/marketing 0.1.11; objava tek uz sve zelene jobove na main.

## v0.1.10 — poliranje pauze, zvuka i rezultata
- U Android/iOS pauzi moguće je odmah uključiti ili isključiti sav zvuk bez napuštanja igre; postavka se trajno pamti.
- Prikaz pauze usklađen je na obje platforme i uključuje jasnu uputu kako nastaviti igru; Android osvježava sadržaj i pri zaustavljanju.
- Završni rezultat prikazuje ime igrača i odabranu težinu.
- Izračun bodova koristi sigurno zbrajanje i ograničenje do 100 milijuna kako dugi letovi ne bi uzrokovali overflow.
- Dodani Kotlin/Swift regresijski testovi i izvorni audit.
- Android emulator QA: oporavak samo od točno prepoznatog dijaloga sustavnog Pixel Launchera, uz stvarni screenshot početnog zaslona; ANR same igre i dalje je blokirajuća greška.
- Android/iOS: pristupačno stanje lika prelazi u „Bopi leti” tek nakon stvarnog dodira. Emulator potvrđuje početak leta prije nego provjerava sudar i rezultat; izgubljeni sistemski dodiri ne stvaraju lažni gameplay pad.
- Android build 11 / 0.1.10 i iOS build 11 / 0.1.10. Izdanje je uvjetovano zelenim CI-jem na main.

## v0.1.9 — težine, lokalni rezultati i čišćenje koda
- Tri težine (Opušteno, Standardno, Izazovno) na Androidu i iOS-u mijenjaju brzinu i gravitaciju bez prekida levela; standardni način zadržava staru fiziku.
- Lokalna top-10 ljestvica prikazuje samo stvarne rezultate s imenima igrača, svijetom, težinom i brojem prolaza. Nema izmišljenih igrača ni mrežne povezanosti.
- Težina i ljestvica uključene su u kompatibilne sigurnosne kopije podataka uz provjeru raspona pri uvozu.
- Uklonjene mrtve grane zaključavanja svjetova, neupotrebljavani rating helper i zastarjela logika otključavanja svijeta nakon 30 levela.
- Android/iOS gameplay parity testovi i provjere izvornog koda dopunjeni su za nove načine rada.
- Izdanje se priprema za objavu tek nakon zelenog GitHub Actions CI-ja; potpisani App Store IPA nije uključen.
## v0.1.8 — Integracija slike i igre, otključani svjetovi
- Svih osam svjetova dostupno odmah, uz očuvani individualni napredak.
- Lokalni profil igrača s izvozom i uvozom spremljenog imena, autorske informacije i regresijske provjere.
- Dorade CI-ja za stabilnije čekanje rezultatskog ekrana i Android/iOS kontrole fizičkih prepreka.
- Izdanje se objavljuje isključivo nakon zelenog CI-ja za main, s Android i iOS artefaktima.
- Podudarnost 1:1 s referencama nije potvrđena; potrebno je testiranje na fizičkim uređajima.

## Razvoj v0.1.8 — optimizacija fizike i regresijski testovi (neobjavljeno)
- Android: uklonjena privremena lista iz izračuna pulsirajućih prepreka u aktivnoj petlji simulacije, uz očuvanu geometriju i pravila kolizije.
- Dodani JUnit regresijski scenariji za 120 Hz pomične prepreke i sprečavanje višestrukog trošenja štita tijekom istog kontakta.
- iOS: uklonjen konflikt dvaju fiksnih Auto Layout ograničenja brojača kovanica na početnom ekranu; dodan regresijski audit početnog zaslona.
- Promjene zahtijevaju uspješan GitHub Actions CI prije spajanja; vizualna podudarnost referencama nije potvrđena.

## v0.1.7 — Vizualna dorada prema referencama
- Ispravljen BOPAVI logotip: zasićena narančasta slova, tamnoplavi 3D rubovi i drveni podnaslov; dodan QA test stvarnih narančastih piksela u izvezenom PNG-u.
- Bogatija ilustracija Bopija: odsjaji pilot-naočala, sjenčanje lica, svjetliji trbuh i obrazi, uz postojeća odvojeno animirana krila.
- Splash/početni ekran: lebdeći otoci, cvijeće, oblačni slojevi, slapovi i udaljeni dvorci; kompaktniji HUD kovanica gore desno i sjajniji zeleni gumb IGRAJ.
- Android i iOS: dvije kolone ilustriranih kartica za svih osam svjetova, s čitljivim stanjem otključavanja.
- Gameplay: dodatni detalji kamena, pukotine, mahovina i rubno osvjetljenje tematskih prepreka.
- Zaslon rezultata: tematska ilustrirana pozadina, velika kartica rezultata i jasne kontrole umjesto generičke tekstualne stranice.
- Očuvani su numerirani kontinuirani leveli, offline napredak, osam zasebnih zvučnih tema i pogodnosti isključivo za osvojene kovanice. Nema plaćanja stvarnim novcem.
- Android/iOS QA i produkcijski grafički audit obvezni su prije izdanja. Vizualna 1:1 podudarnost i testiranje na svim fizičkim uređajima nisu potvrđeni.

## v0.1.6 — Krila i neprekinuti let (2026-10-08)
- Iz originalnog BOPAVI SVG-a nastaje šest prozirnih trupova i dvanaest odvojenih krila za stvarnu animaciju mahanja na Androidu i iOS-u.
- Smanjene animacije zadržavaju nepomična krila; geometrija sudara je nepromijenjena.
- Prelazak između numeriranih levela više ne vraća globalnu udaljenost na nulu; nova prepreka odmah se pojavljuje u vidljivom području.
- Nenametljiv natpis novog levela ne prekida let i ne traži dodatni dodir.
- Nagrađivanje kovanicama, kupnja pogodnosti kovanicama, 8 zasebnih zvučnih tema i offline spremanje ostaju.
- Novi Android/JUnit i iOS/Swift regresijski testovi za prijelaze, uz postojeći QA grafike i uređajnih simulacija.
- Vizualna 1:1 jednakost referencama i stabilnost na svim fizičkim uređajima nisu potvrđene.

## v0.1.5 — Nebeski redizajn (2026-10-08)

- **Titranje na Androidu:** uklonjeno rano prekidanje `onDraw()`; svaki zatraženi kadar sada se iscrta do kraja, a pomak fizike koristi stvarno trajanje kadra (60/90/120 Hz), bez pragova koji su uzrokovali trzanje na 90 Hz.
- **Android 15 / sistemske trake:** stabilizirane margine izbornika i uklonjena nepotrebna preraspodjela prikaza igre pri promjenama insets.
- **Početni ekran:** portretna ilustracija s logotipom i zelenim IGRAJ; uklonjeni uvećani izrez Bopija, duplicirani slogan i tamni prazni pojas u dnu. Android sistemske trake imaju diskretnu tamnoplavu pozadinu.
- **Svjetovi:** osam različitih pozadina s tematskim krajolikom, lebdećim otocima i posebnim elementima; renderiraju se iz zajedničkog izvora bez mrežnog pristupa.
- **Karte svjetova:** pregled s ilustriranim prikazima okruženja; svijet ima vlastite boje i nagrade.
- **Leveli:** puni pregled 4×5 uključuje završene, dostupne i zaključane stavke umjesto gotovo praznog popisa.
- **Uređajni QA:** automatizirano pokretanje Android/iOS simulatora i snimke početnih zaslona kao release gate.
- Igra ostaje offline, bez oglasa i kupnji stvarnim novcem. Napredak i kovanice ostaju spremljeni.
- Produkcijska 1:1 podudarnost i rad na svim fizičkim telefonima nisu još dokazani; referentni kolaži nisu izvorne pojedinačne 2D grafike.

## v0.1.4 — Bopi oživljava (2026-10-08)
- Šest prozirnih ilustriranih likova, izvedenih iz postojećeg BOPAVI brendinga.
- Android i iOS koriste jednaku ilustraciju Bopija, uz boje izgleda otključanih kovanicama.
- Spriteovi se generiraju prije mobilnih buildova i učitavaju jednom po prikazu.
- Animirani nagib, trag leta i efekti nagrada ostaju; postojeći geometrijski prikaz je fallback.
- Nebo i tlo sada vizualno pokrivaju cijeli zaslon visokih i širokih mobitela, bez razvlačenja svijeta ili hitboxa.
- Dodatne provjere zaštite od lažnog sudara u otvorenom prolazu.
- Nema kupnji za novac, oglasa ni prikaza ukupnog broja levela.
- Završna potvrda izgleda i rada na svim fizičkim uređajima još je potrebna.

## v0.1.3 — Pravi dodir (2026-10-08)
- Popravljen dodir Bopija sa stvarno nacrtanim završnim kapicama stupova (Android i iOS).
- Sudar bez štita završava pokušaj u istom koraku simulacije; štit štiti točno jedan udarac.
- Automatizirani regresijski testovi kolizije dodani za Kotlin/JUnit i Swift.
- Novi ilustrirani početni ekran, rasterizirani logotip i splash prikaz.
- Android gameplay skriva sistemske trake, izbornici zadržavaju kontrastne tamne trake.
- Redizajnirani lebdeći otoci, čitljiviji gameplay bez nepotrebnog natpisa na podlozi.
- Grafika se generira u CI-ju i uključuje u Android/iOS build artefakte.
- Ne tvrdimo da je završena provjera stabilnosti na svim fizičkim telefonima.

## v0.1.2 — Premium let (2026-10-08)
- Android i iOS: premium stilsko ujednačavanje izbornika, gradijenata, tipografije, gumba i kartica svjetova.
- Početni ekran ostaje usredotočen na Bopija, kovanice i jedan gumb IGRAJ.
- Odabir otključanih levela sada koristi kompaktnu mrežu 4 × 5 s navigacijom bez beskorisnih gumba.
- Bolja čitljivost HUD-a na manjim iPhoneima te prilagođeni tekstovi u postavkama.
- Android JUnit testovi generatora i fizike prolaza, uz lint i build gate.
- Bez oglasa i kupnji stvarnim novcem; po svjetu različiti zvukovi i blago.
- Potpisivanje i testiranje na fizičkim telefonima još su potrebni.

## v0.1.1 — Uglancan let (2026-10-08)
- Android/iOS: dodatne animacije Bopija pri skupljanju blaga i udarcu u štit.
- Nove dekoracije prepreka za livade, vulkan, noć i svemir; kolizije koriste originalne granice prolaza.
- iOS: različiti simboli blaga po svijetu, manje stvaranja UIKit objekata pri crtanju.
- Bez kupnji za stvarni novac; napredak, zvukovi i jednostavan početni ekran sačuvani.
- Potvrda produkcijske stabilnosti zahtijeva testiranje na stvarnim uređajima.

# BOPAVI — povijest izdanja

## v0.1.0 — Prvi GitHub projekt
- Dvije zasebne mobilne aplikacije: Android (Kotlin) i iOS (Swift).
- Osam svjetova s vlastitim grafikama, zvukom i predmetima za skupljanje.
- Proceduralni leveli s trajnim nastavkom napredovanja.
- Animacije Bopija, pomične prepreke, više načina promjene izazova.
- Pogodnosti za osvojene kovanice, bez kupnji za novac.
- Zajednički generator i testiranje jednakosti fizike.
- Ispravljen tip brojila zamaha u Android spremanju te uključena iOS audio implementacija u build.
- Novi marketing README s logotipom, ilustracijom i opisima svjetova.
