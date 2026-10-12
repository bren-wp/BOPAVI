## v0.1.50 — high-detail Bopi art and animation QA

- Build with the existing image generator and inspect the actual blue Bopi on Android API35/API36 and iOS Simulator: eyes have inner azure irises and multiple catchlights; the beak is visibly open; the pilot goggles and gold crown scarf remain legible without overlap.
- In the pilot picker, verify the fully winged Bopi portrait still renders at all sizes and that all nine existing skins remain individually selectable and priced. Do not alter saved skin indices, rewards or state on upgrade.
- In gameplay, trigger consecutive wingbeats, pause, resume and background the app. Six newly authored feather glints must move with their respective left/right wing sprites, not remain fixed on the torso; collision radii, collected coins and player position remain unchanged.
- CI must confirm Android/iOS sprite PNG equality for all nine bodies, 18 wings, nine portraits; check graphics dimensions, emulator first-flight and resume, Swift saves/physics and Android Kotlin tests.
- Record a fresh screenshot comparison with the user's premium Bopi reference; generated vector shading is still not full 3D/rendered equivalence. Repeat manual testing at reduced motion, small/large phones and a physical device before claiming production hardware readiness.

## v0.1.49 — home branding and shared illustrated footer

- Capture actual Home screenshots from Android 15 and 16 emulators and iOS simulator, including short and tall portrait devices. BOPAVI crown and lettering must appear **below** the accessible real avatar/best-score/coins/gear header, not obscured by it.
- Check four vector-sourced footer tiles: silver Profile silhouette, checklist Tasks, cyan feather Collection, gold/blue chest Shop. The icon is decorative, the tile and its accessible textual label are the only action.
- Tap every tile, verify the correct real destination and backward navigation, then inspect persisted score, pilot portrait, coins, unlocks and best results. No virtual paid gems, online logout, advertisements or placeholder views.
- In the graphics CI job confirm 128×128 transparent PNGs for all four icons and exactly byte-identical Android/iOS renderings. Review screenshot legibility, native color contrast, safe-area spacing and small/large fonts.
- The simple existing vector-based Bopi/environment is still visually behind the 3D concept references; inspect and log that separately. iOS/Android device packaging is unsigned and no physical-device test is claimed.

## v0.1.48 — idle preview, accessibility and frame scheduling

- On Android 15/16 emulator and iOS simulator, start a new flight from the pilot picker: the dark translucent DODIRNI ZA LET hint appears before the first flap. No premature gameplay HUD or Pause button.
- Leave the idle preview visible for 10 seconds. Android should not request recurring frame invalidations and iOS should not start a CADisplayLink. Verify no blank/stale first frame.
- Touch once, then inspect one flap event, one equipped-boost deduction, activated HUD/Pause, hidden idle hint and smoothly advancing scenery. Repeated touch continues flight without resetting progress.
- Test Android TalkBack activate and iOS VoiceOver activate: the accessibility click also flaps exactly once, without consuming purchased boosts again.
- Pause, open the in-pause settings panel, resume, background/foreground the app, and use reduced motion. Observe stable frame timing and no rendering loop while paused.
- Preserve original local saves and level/character IDs; compare true device/simulator screenshots against the ten concept references. Physical-device performance and 1:1 art are not yet verified.

## v0.1.47 CI failure diagnosis and regression fix

- Android 15 emulator proved BOPAVI installed/launched successfully and captured `android-home.png`, then stopped at the **outdated smoke assertion** `home must expose one accessible clickable POSTAVKE, got 2`. Both visible references show top settings gear and a separate central settings button.
- Updated the emulator QA script to demand **exactly two distinct accessible POSTAVKE actions** and separately verify active profile, achievements and earned-coin header controls. Its settings tap now selects the **lower main CTA**, so menu-navigation smoke remains deterministic while testing the new gear's presence and hitbox.
- This does not waive an app ANR or a failed navigation test; the genuine emulator gameplay tests, screenshots, Android 16 smoke and iOS build must still pass before merging.
 
## v0.1.47 — accessible interactive home header

- Check header on narrow Android 15/16 and iOS simulator: selected Bopi avatar is visible, best score and earned coins are real local values, and settings gear is reachable without scrolling. No control overlap with cutouts, hero, safe areas or navigation.
- Tap avatar: actual Profile, back Home. Tap top score: Achievements, back Home. Tap coins: offline Coin Shop, back Home. Tap settings: Settings, back Home. Repeat after selecting a different owned character and buying a skin; avatar and coin values must update when returning home.
- Verify VoiceOver/TalkBack reads all four independent actions and narrow display + larger accessibility text scales. No fake diamonds, unimplemented login or real-money items; game state survives navigation.
- Verify Android 15/16 emulator and iOS simulator/device compilation, signed production store packaging remains a separate requirement. Pixel-perfect 1:1 reference comparison not yet approved.

## v0.1.46 — Android emulator system launcher recovery

- GitHub-hosted Android API35 occasionally renders the verified system dialog `Pixel Launcher isn't responding` over an otherwise running BOPAVI app immediately after boot. Recover only the exact system alert and re-dump the live app UI before judging home navigation.
- This recovery **must not** accept any BOPAVI ANR or a missing BOPAVI process. If the next UI still lacks actual home, picker or idle flight controls, fail QA with screenshot and XML evidence.
- The picker retry branch now has a bounded attempt count so a failed tap cannot loop indefinitely.

## v0.1.46 — future-page flight-safety regression

- On Android 15/16 and iOS simulator, fresh install with frontier 1: open LEVELI and go from page 1–20 to locked page 21–40. The header must say STRANICA 2 / 52429 and ZAKLJUČANI LEVELI. Neither NASTAVI LET nor POKRENI ODABRANI LEVEL may appear on that locked page.
- Verify PRETHODNIH 20 returns to page 1, VRATI NA OTKLJUČANE LEVELE goes to the saved frontier's page, and all locked tiles refuse to launch.
- With frontier 21, page 21–40 is accessible and NASTAVI LET returns to preflight for level 21; level 22 stays locked. Show saved true stars/30 without synthesized rewards.
- On final page of 1,048,576 levels, no SLJEDEĆIH 20 is rendered; page arithmetic must remain within range. Verify VoiceOver/TalkBack can read page, lock and star labels on narrow screens.
- Existing save, purchase, pause/settings, difficulty, game physics and online-permission audits remain mandatory. No marketing picture is accepted as an emulator capture.

## v0.1.45 — locked level navigation QA

- Fresh save: on LEVELI page 1–20, tapping SLJEDEĆIH 20 opens locked page 21–40 even when frontier is 1. PRETHODNIH 20 returns to the first page.
- On fully locked pages, all tiles remain locked and cannot start gameplay, the number input is absent, and VRATI NA OTKLJUČANE LEVELE returns to the page containing the saved frontier.
- With frontier 21, tile 21 is unlocked but 22–40 are still locked. Confirm saved star counts and accessible status labels.
- At maximum 1,048,576, no next page exists. Check both Android emulators (15/16) and iOS simulator; no false 1:1 screenshot claim.

## v0.1.44 — music volume regression

- Set Glazba to 37% in Settings. Launch a level, pause, press MUZIKA twice, continue; restored value must be 37%, not default 80. Repeat after returning Home and app restart.
- Confirm ZVUK and MUZIKA buttons change ✓ / ✕ and accessibility announcement according to actual audio state. Audio and game simulation remain paused while adjusting.
- Repeat using in-pause music slider (23%), zero, 1% and 100%; confirm pre-mute volume survives. No effects on saves, coins, gameplay, or backup compatibility.
- Required: Android 15/16 emulator tests, iOS simulator/device and native release QA.

## v0.1.43 — Android QA pilot-launch hardening

- Resolved a misleading CI pass condition: a flight-confirmation tap used to count as successful if the POLETI S button merely disappeared, even if the app landed on another screen. Recheck stable on-screen button bounds after scrolling and require the exact idle GameView with its accessible flight instruction.
- Any unexpected destination now produces a failing diagnostic XML artifact instead of attempting a new IGRAJ tap on an unrelated menu.
- Validate the fix on Android API35 and API36 before publishing any new GitHub release. This does not change user-facing game data, UI or version.

## v0.1.43 navigation acceptance

- Home footer: ZADACI, KOLEKCIJA and TRGOVINA must return to HOME via the visible Back and Android system/predictive Back.
- Settings → Shop/Skins/Achievements → Back returns Settings. Profile → Skins/Achievements → Back returns Profile.
- Home → Shop → Skins → Back → Shop → Back → Home. Repeat when entering Shop from Settings. Purchase a perk or claim bonus coins and verify original Back route persists after refresh.
- Worlds → Levels → Pilot Picker: Android system Back must return through Levels and Worlds to Home. During a live flight, Android Back pauses instead of dropping the run.
- Ensure all save data, earned currency, sound and haptics remain unchanged, and run Android API35/36 plus iOS smoke.

## v0.1.42 — calm mascot flight preview

- Android API35/API36 and iOS simulator: the selected pilot gently floats in the large pre-flight hero only; scenery, portrait badge, live character prices and accessible buttons never shift with the animation.
- Turn on Reduce Motion / remove Android animator duration scale and reload character picker: the full-size pilot must remain visible, stationary and selectable. Normal motion is subtle and does not alter game physics.
- Select multiple characters in quick succession, visit levels/home and return repeatedly: check no duplicate hover animations, visible stale images, memory growth, activity retention or process crashes.
- Change application lifecycle (Home, background/foreground, screen rotation when supported) and ensure the hover stops after its view detaches. On resuming a flight, the actual game canvas and sound/pause controls retain previous semantics.
- Maintain real save formats and original offline currency; screenshot 1:1 acceptance still requires actual measured pixel comparisons.

## v0.1.41 — full-bleed status bar and safe-area QA

- Compare genuine Android 15 and 16 screenshots against v0.1.40: Home, Pilot Picker, Settings and Result should have uninterrupted background imagery up to the top, without the blue Android OS clock/status stripe.
- On notched Android hardware and emulators, the top wallet, back button and game headings must remain below camera cutout areas. Scroll insets must combine system bars and display cutout dimensions; the scene root must not be inset or double-pad the controls.
- iOS: all screens hide the status bar but UIKit safe areas must protect top notch and bottom action buttons. Test a simulator with a Dynamic Island.
- Compact Android screen: verify real large score and coins, and KOLEKCIJA tab caption, auto-shrink without clipping. Pause/settings and original controls remain tappable.
- The 3D reference artwork is not reproduced by these layout changes; do not label screenshot comparisons as 1:1 until separately verified.

## v0.1.40 — premium pilot gallery

- Inspect all nine 3-column pilot cards on Android API35/36 and iOS simulator: scenery behind portraits, title over scene, badge and state-colored pricing footer must not overlap or clip on narrow phones.
- First three cards remain Portantin, Noa, Any; older skin indices, prices, rewards and save backups must not change. Selecting owned character updates large preview before starting flight.
- Locked characters require earned coins and explicit purchase confirmation. Cancel must spend zero coins. All nine portrait layers are individually accessible through one actionable card, not separate fake controls.
- Preserve pause settings, physics and genuine store screenshots. Pixel-for-pixel fidelity to concept art is not yet verified.

## v0.1.39 — critical pause/settings regression

- Start a live flight (not the inactive preview), pause, tap POSTAVKE, adjust Glazba and Zvukovi independently, switch Vibracije, then close the settings subpanel. The canvas and exact player state (current level, distance, hearts, coins, shield and star streak) must remain unchanged until pressing NASTAVI.
- Pause music must not resume on slider updates or while the subpanel is open. Confirm volume and haptics settings remain persisted when continuing and after process restart.
- Test scroll visibility of in-pause controls with both regular phones and narrow/large-font devices, using Android accessibility and iOS VoiceOver focus navigation; no overlapping critical actions.
- Explicit PONOVO restarts and IZLAZ leaves the run; only NASTAVI resumes. Main menu POSTAVKE remains the separate full settings page. No cloud sync or sign-out is falsely shown in offline game.
- Verify Android 15/16 emulator and iOS simulator/device release builds; do not claim 1:1 screenshots unless approved pixel-diff assets exist.

## v0.1.38 UI acceptance

- On Android API35/API36 and iOS simulator confirm: world art stays inside the rounded frame; upper-left crown/number badge and lower-right arrow do not overlap the live white world name on image.
- Confirm two bottom rows show real LEVEL and collected quantity, plus real ★ rating out of 30 and per-world best score; long world titles and five+ digit values must shrink rather than clip.
- Verify all eight world cards are fully tappable, accessible as a single action, selected color feedback reflects the current save state, and the world index maps to the correct saved levels.
- Preserve responsive 2-column gallery and vertical scroll on narrow screens. Source QA must fail if text moves off artwork into a third redundant row.
- Verify pre-existing gameplay physics, per-level best star rating, pause/results, and v1-v6 offline backup continue to pass unchanged.
- High-resolution premium texture and exact 1:1 screenshot parity remain separate acceptance gates.

## v0.1.37 reference-world acceptance criteria

- Confirm eight exact gallery headings, in world-index order, are shown on both platforms. Verify Android emulator scrolls all eight and checks accessible saved scores, starred progress and actual level number.
- Verify the same generated PNG bytes for Android/iOS world0–world7; mapping is (0,1,3,5,6,4,0+new forest overlay,7) relative to existing approved scenes.
- Verify every background is visually distinct, especially world0 and world6, and preserves collision-lane contrast.
- No migration or renumbering of existing world IDs, unlocked level progress, earned 0–3 stars or long-running endless flight. Test existing v1–v6 backup restoration and per-world counters.
- Validate Android APK/AAB, Android 15/16 screenshot and navigation smoke, iOS simulator/device compilation and saves, original store asset zip.
- Full 1:1 raster comparison to reference art cannot be claimed until standalone high-resolution 3D assets are integrated and screenshot-diff approval is performed.

## v0.1.36 — real earned stars, level progression and v6 backup QA

- Validate completion callback saves the **current level's** earned star pickups independently from the cumulative marathon HUD.
- Verify that the first ten completed levels in each world contribute at most 30 real stars to the world card; replaying a level must never lower its best rating.
- Each selectable level card must show actual ★/☆ count in the three-star row, while locked levels remain inaccessible.
- Test native v6 backup export/import preserving sparse star records; reject malformed ratings and duplicate entries before any stored progress is modified. Accept existing v1–v5 backups.
- Android emulator world-card audit must require saved level, item/score details and real earned stars. Preserve all other emulator/physics/pause/gameplay validations on Android 15 and 16 and iOS.
- Never fill unearned stars to imitate reference screenshots. Premium visuals still require separate original art and device screenshot comparisons.

## v0.1.35 — result hero and truthful record checks
- Android 15 and 16 screenshot QA must display the actual selected pilot and world on the result screen through accessible live image composition, not a hardcoded generic hero. The emulator must still reach genuine collision results and expose PONOVO.
- Both Android and iOS result screens only show NOVI REKORD when current points exceed the saved record from before the run was persisted. New-record state must be evaluated before `recordRun`, and the save operation must occur exactly once.
- Winning must play the existing level-success sound, losing the hit sound. Verify real results, reward claims and sharing remain functional.
- Complete Android/Swift platform parity, privacy/build/API 35+36/iOS simulator/device gates, one unambiguous Play screenshot artifact and unsigned AAB verification before publishing all five v0.1.35 files.

### v0.1.34 retry-safe screenshot packaging
- An Android emulator failure may upload partial visual evidence. Subsequent successful QA attempts must replace the older archive using `overwrite: true`.
- Play packaging requires exactly one current `QA-Android-visual-flow` artifact, and each screenshot must originate from real emulator capture. Never fake or fill missing screenshots with UI illustrations.

## v0.1.34 — real profile and premium Settings click-through
- On Android 15 and 16, home PROFIL opens a dedicated profile with live local pilot/player data and never opens Postavke. System Back must return to the three-button home. Emulator script verifies both transitions.
- On iOS, tapping PROFIL opens the equivalent profile with the same four stored metrics and actual Bopi portrait, not an account or mocked screenshot. Verify taps through profile, character selection, achievements, and back-to-home on simulator.
- Settings must show two interactive volume sliders, vibration, selectable three-card difficulty, followed by Additional Options with working reduced-motion and master-audio toggles. Neither offline platform may offer fake account logout, cloud sync or premium diamond purchasing.
- Complete mandatory lint, Kotlin unit, Android APK/unsigned AAB, Android API35/API36 emulator, iOS simulator/unsigned device, Swift parity and save tests before merge; main CI must publish all five assets for commit-matching v0.1.34.
- Pixel-identical reproduction of supplied high-resolution 3D mockups remains unverified; no concept render is presented as an emulator screenshot.

## v0.1.33 — mandatory versioned release after any code change
- Every future PR must declare a versionName above the last published GitHub release; Android versionCode and iOS CFBundleVersion/build must be incremented together and verified in CI.
- No production signing is permitted on GitHub: no manual signed-upload workflow, no fake keystore, no Play Console submission. Developer APK may carry the Android debug signing certificate; the release AAB intentionally has no upload signature.
- Merge after all PR jobs pass. Then require all main CI jobs, inspect public GitHub Release v0.1.33, SHA and all five artifacts before claiming issuance. An existing release tag targeting a different SHA or missing assets must fail CI, never silently skip.
- CI derives tag and store archive names from a strict Android/iOS version/build parity check. Gradle has no signing configuration; AAB JAR signatures are explicitly rejected at build and publication, with synthetic signed/unsigned regression fixtures.
- A new version tag and validated artifacts are required again for every subsequent code change; do not reuse this release tag.

## Premium world selection — native regression gate
- On Android 15/16 and iOS simulator check **SVJETOVI** uses two columns and shows eight distinct clickable world illustrations with responsive white titles and gold progress facts.
- The **LEVEL** line must reflect `streamFrontier(world)`, and the gold line must show `collectibles(world)` and `best(world)` from existing local data; screen reader labels must announce all three, not fictional stars or locked worlds.
- Scroll to all eight cards. Android emulator must confirm each world card exposes name, real saved frontier, collected items, and best score, and Back still returns to the playable home screen.
- Replay a level, return to World list and verify only earned save metrics update, without corrupting per-world progression. Visuals must not obstruct tap targets, and titles must scale on small screens.
- Run strict audit, Android APK/unsigned AAB lint and tests, Android API35/API36 emulator, iOS simulator and unsigned device/Swift parity. No GitHub production signing.
- Art remains improved 2D rendering rather than pixel-identical 3D concept; 30-star rewards and paid world gates are not fabricated.

## Premium difficulty settings — click-by-click acceptance
- On Android 15/16 and iOS simulator the **Postavke → Igrač i težina** section has horizontal Lagano / Normalno / Zahtjevno cards with actual pilot illustrations and no clipping on narrow displays.
- Tap each card: only the chosen card gains the gold border and accessible selected status; choice is persisted with original offline values 0/1/2.
- Confirm the next real flight uses the selected difficulty and the score/progression/save logic is unchanged; while inside the settings page there is no unexpected restart.
- Emulator QA must scroll to the card row, tap Lagano, check its updated accessible selection, then tap Normalno and check restored selection before returning home.
- Validate volume sliders, sound/haptics, data export/import, reduced motion and back navigation still work. TalkBack and VoiceOver expose actionable selection labels.
- Full release gate: privacy/art audit, Android lint/JVM/APK/unsigned AAB, Android 15/16 real smoke, iOS simulator/unsigned device and Swift parity. No production signing on GitHub.

## Complete character portrait parity — acceptance gates
- Android 15/16 and iOS simulator: for all nine pilots, gallery cards, selected pilot hero and native PAUZA display **both left and right wings**, not the earlier wingless torso-only asset. Portantin retains his passenger, goggles, mask and gloves; Noa/Any retain unique silhouettes.
- The same indices and owned masks are used throughout: Portantin / Noa / Any are the first visual row but ownership, coin costs and save persistence remain untouched.
- Confirm all `bopiportrait0..8` have 512×512 RGBA pixels with nonempty transparent margins and visibly differ from corresponding torso `bopi0..8`; Android/iOS PNG bytes must match exactly.
- Test first flap, separate wing animation, reduced-motion option and game collisions without using precomposed menu sprites in the active flight renderer.
- Validate long labels and full wing silhouettes do not clip in 3×3 gallery cards, preview and PAUZA on small screens.
- Run source/privacy audit, art generation, Android lint/JUnit/debug APK/unsigned AAB, Android API35/36 emulator smoke, iOS simulator/unsigned device/Swift parity before merging. Never production-sign builds on GitHub.

## Eight-world premium art — native parity acceptance
- Generate all eight world PNGs with the existing Python/CairoSVG/Pillow workflow. Both Android `drawable-nodpi/world0..7.png` and iOS `Assets.xcassets/World0..7.imageset/world0..7.png` must be byte-identical for each world.
- Verify visual uniqueness: castles (green/night), cascades, crystal spires, snow, lava, sky bridges, pearl reef and ring portal. No visual counter/buttons are rasterized into game art.
- Pixel smoke on Android 15, Android 16 and iOS simulator: no stretched islands, backdrop seam, clipped landmarks, interactive-element overlaps or obstructed moving gate collision entrances.
- Confirm older devices' gameplay memory/performance unchanged (textures still 960×1600 RGB, loaded once). Check flight, infinite-level seam, coin collection, results, idle pause lifecycle and reduced-motion setting.
- Do not merge before generator/asset audit, APK + unsigned AAB, Kotlin tests, iOS simulator + unsigned device build and real emulator smoke are green. No production GitHub signing.
- The 3D character and environment reference art is still a **separate visual parity gap**, not a checked production claim.

## In-flight HUD parity — required QA
- On Android API35/API36 and iOS simulator check top row displays **actual run coins, actual collected stars, actual current level**; native pause button remains unobstructed and accessible.
- The blue progress bar uses passed/current level total gates (not fictional meters), while cumulative passed gates and screen-reader progress never reset during seamless level promotion.
- If shield/magnet is active, show the authentic current count/time. Hidden boosts must not display nonzero icons.
- Idle flight shows **no HUD and no pause button** before first real flap. Paused gameplay freezes physics and only the real custom modal renders PAUZA, not an additional overlapping painted panel.
- No duplicate iOS UIView counter; no extra per-frame UIImage decoding, scaling changes or collision geometry changes.
- Require visual screenshots, Android unit/lint/APK/unsigned AAB, API35 and API36 smoke, iOS simulator/unsigned device tests, Swift save/physics parity and privacy audit before merge.
- Visual parity with supplied high-fidelity 3D art remains **open**, even if these real UI controls pass.

## Premium character picker — regression gate
- On Android 15 and 16 and iOS simulator, verify pilot gallery first visual row is Portantin / Noa / Any and all nine cards remain accessible by real scrolling.
- Check the chosen character hero matches the actual persisted index and correct world (world art/portrait are separate layers).
- Purchase confirmation must require sufficient **earned offline** coins; on cancel, ownership and wallet are unchanged; on success, the correct old index is stored without migration.
- Primary CTA stays **below** all nine selectable cards and launches only after explicit tap; first-flap pause must not be shown on idle launch.
- Run actual end-to-end pilot picker swipe/navigation, Kotlin + Swift/save parity, Android AAB unsigned, iOS simulator and unsigned device build. No GitHub signing.

## Premium results — acceptance criteria
- Finish an actual run. All three summary cards must match the *same* GameSimulation score, coins collected and cumulative passed gates. Existing record/previous-best comparisons still run **before** persisting the completed run.
- Four reward cards show actual coins, stars, earned but unclaimed high-score bonus and persistent best score, without implying that pickups are already spendable wallet credits.
- Verify local record bonus claims only once: bonus label updates to +0, wallet balance increases by the claim exactly once, save does not re-record wins/losses on claim.
- Verify 3-/4-column layouts on small Android and iOS screens, VoiceOver/TalkBack labels, Safe Area scroll, primary Retry, bottom Leaderboard/Home and native Share with only the score & gate count, not personal names.
- Verify effects and gameplay cannot run behind the result screen, home/level state remains intact, and there are no invented paid rewards.
- Android CI: privacy, JVM tests, API35 + API36 emulator flows, APK and unsigned AAB. iOS: launch smoke, Swift parity/migration, unsigned device compilation.
- Photorealistic reference artwork remains a separate unresolved visual parity requirement; maintain an honest screenshot mismatch log.

## Premium pause — regression and interaction QA
- Confirm no PAUZA action before the first flap, on both systems and accessibility tree.
- With active flight paused: gameplay coordinates and score stop advancing; music loop remains paused even if global sound is switched back on.
- Tap Continue => closes overlay and resumes same run; Retry => new idle run of the same world/level; Exit => home; Settings => actual local settings.
- Master Sound and Music toggles affect the real audio mixer, not cosmetic labels. Controls opens accurate gesture instructions and returns to the frozen flight.
- Dialog fully scrolls and remains accessible on compact phones / Display Zoom; labels and margins must not clip behind status or navigation regions.
- Android and iOS screenshots still require pixel-diff approval against the supplied 3D rendered pause reference. No fabricated background screenshot is used.

## Premium UI iteration 2 — QA acceptance
- World card numbering 1–8 uses the actual world index; badges/arrows remain noninteractive children, while the complete card is the tap target.
- Illustrated 4-column / 5-row level grid reads persisted frontier: complete levels show ✓; next available shows the active badge; future levels remain visibly locked with a non-startable tap.
- Verify previous/next 20-level paging at boundaries 1, 21 and 360; confirm saved stream frontiers do not reset in marathon mode.
- Music/effects volume sliders are separate real playback gains, defaulting to 80/70, saved as 0–100 on device and exported in v5 backup as optional fields; imported older v5 and v1–v4 saves still work.
- Run Android API35/API36 click-path smoke, Android unit tests, iOS simulator/device build, Swift core/save parity, and privacy audit before merging. Signing must remain disabled on GitHub.
- Screenshot comparison against provided 3D reference art remains incomplete; do not claim pixel-identical artwork yet.

## Premium UI references — verification gate

- Review and track all ten supplied reference screens; run screenshot comparisons against actual Android API35/36 and iOS simulator outputs.
- Preserve existing UI hierarchy and game lifecycle smoke tests including no pause until the first flap, accessible 9-pilot gallery and playable offline progression.
- The first implementation stage is a production code redesign, **not yet verified 1:1** visually against the photorealistic 3D concept art.
- Test gesture-navigation safe areas on 360dp Android phones and compact iPhones. Follow up on real hardware before claiming production-perfect screenshots.
- Signed Google Play bundles must **never** be generated or uploaded by GitHub Actions.

## v0.1.31 Android 15 launcher ANR investigation (2026-10-10)
- Main-branch CI 38013725742 failed only the Android 15 pilot-gallery smoke; the APK/AAB build, Android 16 smoke and iOS simulator succeeded.
- The CI trace repeatedly reported the SYSTEM Pixel Launcher ANR before seeing Portantin, Noa, and Any. Preserved XML and real screenshot show the game picker rendered with a scrollable Android `ScrollView`.
- QA now terminates only the exact confirmed hung launcher process, checks BOPAVI remains running and still requires real scrolling to find all three characters. An unverified/system-other or BOPAVI ANR is never suppressed.
- GitHub signing tests/manual AAB signing are disabled. Debug APK and unsigned AAB continue to be created for download. Do not claim Play upload readiness without offline signing.

# BOPAVI — produkcijski QA

## Automatski testovi

Pokrenuti `bash tests/run_native_core_tests.sh` i automatskog pilota iz `tests/Pilot.kt`. Prvi provjerava Kotlin/Swift geometriju, duge proceduralne cikluse, migraciju spremanja, nagrade i resurse. Drugi provjerava prohodnost uz pomoć automatiziranog pilota (ne provjerava subjektivnu težinu).

## Obavezni Android/iOS fizički testovi — nisu izvedeni u ovom okruženju

1. **Buildovi:** čist Debug i Release build, instalacija i prvo pokretanje na svim podržanim verzijama OS-a; provjera da su originalne audio datoteke upakirane.
2. **Grafika:** mali i veliki telefoni, izrezi i zaobljeni kutovi, rotacija/portret, prikazi svih svjetova, low-motion način rada.
3. **Zvuk:** start i prekid glazbe po svijetu, mute, zvuk na dodir, slušalice, primanje poziva, Bluetooth, povratak iz pozadine.
4. **Ekonomija:** nagradni level prvi put daje kovanice; ponavljanje ne daje; kupnja smanjuje stanje; pogodnost se troši točno jednom; ponovni ulazak zadržava podatke.
5. **Dugi leveli:** nastavi broj iznad prve proceduralne serije bez resetiranja naziva levela; migracija ranije verzije; otključavanje novog svijeta ne prisiljava izlazak iz trenutačnog.
6. **Stabilnost i performanse:** hladni start, više uzastopnih pokušaja, 30/60 FPS, memorija, baterija, state restore i ANR/crash logovi.
7. **Pristupačnost:** VoiceOver/TalkBack, kontrole najmanje 44 pt/48 dp, dinamički tekst, kontrast i smanjeno kretanje.

**Release gate:** ne označavati izdanje produkcijski spremnim dok Android i iOS buildovi, QA na stvarnim telefonima i crash/performance provjere ne budu stvarno prošli.


## v0.1.1 — vizualno i performansno poliranje
- Novi vizualni odziv skupljanja i aktivacije štita na obje platforme.
- Osam tematskih simbola i detalja prepreka; grafika ostaje unutar kolizijskih granica.
- iOS: Core Graphics boje i zaobljeni oblici bez stvaranja UIColor/UIBezierPath po svakom prolazu crtanja.
- Build gate: Android APK + AAB, iOS simulator + nepotpisani device build, audit resursa i privatnosti.
- Stvarni iPhone/Android uređaj, dugotrajan FPS/baterijski profil i potpisani IPA nisu potvrđeni.


## v0.1.2 — premium mobilni UX
- Android/iOS: provjeriti kartice svjetova, povratne gumbe, zaslon rezultata, zalihu pogodnosti i početni ekran.
- Grid levela renderira najviše 20 dostupnih odabira i ne prikazuje neaktivne stranice.
- Android provodi lintDebug i testDebugUnitTest prije APK/AAB.
- iOS treba proći Simulator build i nepotpisani device build prije GitHub izdanja.
- Dodatni profil FPS/memorije i uređajni smoke test nisu zamjena za uspješan CI.

## v0.1.3 — QA prema korisničkom videu
- Provjeriti frontalni dodir glavne cijevi, preklop široke završne kapice, dodir s donje i gornje strane.
- Bez kupljenog štita poraz mora nastupiti odmah; početni štit troši se jednokratno.
- Grafički i kolizijski promjer Bopija povećan je istovremeno.
- Android i iOS imaju isti kriterij sudara, uključujući bočno proširenje kapice.
- Novi home treba prikazati zaseban BOPAVI logo i veliku ilustraciju bez lažnog gumba unutar slike.
- Android splash i iOS LaunchArt prikaz bez bijele pozadine; sistemske trake u igri skrivene na Androidu.
- Stvarni uređajni smoke test i potpisani iOS IPA još se ne mogu potvrditi samo pomoću CI-ja.


## v0.1.4 — ilustrirani likovi i testovi
- Provjeriti 6 jedinstvenih prozirnih spriteova 512 × 512, bez dekodiranja unutar petlje crtanja.
- Provjeriti da štit, skupljanje i dodir kapice rade i s novom grafikom.
- Zadržati Android lint/JVM/APK/AAB i iOS Simulator/device/Swift fizikalne regresijske testove.
- Profiliranje na fizičkim telefonima i potpisani iOS IPA još nisu potvrđeni.

- Provjeriti omjere 19.5:9, 20:9, 16:9 te iPhone s Dynamic Islandom: cijela površina neba i tla, bez bijelih ili plavih pruga.


## v0.1.5 — premium vizual, titranje, stvarni launch QA
- Android: `onDraw` uvijek završava crtanje; provjera vremena ograničava samo korak fizike.
- Provjeriti 60/90/120 Hz zaslone, animaciju prve sekunde, dodir/pauzu/povratak iz pozadine, smanjene animacije 30 FPS.
- Za Android i iOS generirati 8 zasebnih 960×1600 svjetskih ilustracija. Učitati jednu pozadinu po svijetu, bez dekodiranja u petlji crtanja.
- Pokrenuti Android emulator i iOS Simulator; potvrditi pokretanje bez pada i spremiti screenshot početnog zaslona za ručni vizualni pregled.
- Izričito provjeriti home nasuprot BOPAVI referenci, kontrast zelenog gumba, pozicioniranje logotipa, pune visine zaslona.
- Testirati odabir levela, zaključavanje i spremanje napretka, sve svjetove, trenutni poraz pri sudaru bez štita.
- Buildovi i screenshotovi simulatora nisu dokaz stvarne stabilnosti, GPU profiliranja i potpune 1:1 usklađenosti na fizičkim telefonima.

- QA snimka iOS simulatora otkrila je preveliko uvećanje Bopija (landscape slika u portrait frameu) i dvostruki slogan. Prebačeno na istu portretnu kompoziciju koju koristi splash, bez drugog natpisa; provjeriti screenshotove obiju platformi nakon commita.

- Android emulator screenshot v0.1.5: potvrđen launch i početni UI; pronađen i uklonjen pretjeran tamnoplavi prazan pojas uz donje kontrole te usklađene Android sistemske trake. Ponoviti screenshot nakon zadnjeg commita.

- Android 90/120 Hz: usporediti položaj Bopija i brzinu prolaska kroz prepreke za jednako proteklo vrijeme; JUnit regresija simulira 60, 90 i 120 frejmova u sekundi.

## v0.1.7 — referentna vizualna kontrola
- Provjeriti razliku između dostavljenih konceptnih fotografija i stvarnih screenshotova: položaj logotipa, omjer Bopijeva tijela, detalji naočala, volumetrija otoka, slojevite prepreke i gumbi.
- Svaki grafički build mora potvrditi da logo PNG sadrži stvarne zasićene narančaste piksele; prethodni SVG gradijent proizvodio je presvijetlu plohu iako je izvorna paleta bila ispravna.
- Android i iOS karta svjetova mora imati četiri reda po dvije ilustrirane kartice, s preglednim nazivima i zaključanim stanjima.
- Pregledati završetak leta na svakoj platformi, prilagodbu rezultatske kartice malim ekranima, sve akcije (ponovno, oprema, mapa, početak) i čitljivost tijekom promjena orijentacije sustavnih traka.
- Obvezno: generiranje svih resursa, native audit, Android lint/JUnit/APK/AAB, Android emulator/screenshot, iOS Simulator build/launch/screenshot, Swift fizika/paritet i unsigned iPhone device build.
- Za stvarnu piksel-po-piksel jednakost potrebni su zasebni završni slojevi ilustracija i usporedni prikazi na fizičkim uređajima. Kolaži sami nisu zasebni spritesheetovi.


## v0.1.18 — ciljani audit upravljanja kadrovima i stanja
- Pregledani Kotlin/Swift simulacijski koraci, Android Canvas vsync i iOS CADisplayLink. Ranije se za svaki kadar preko 34 ms trajanje skraćivalo i gubilo se simulacijsko vrijeme.
- Novi model u oba corea dijeli kratke zastoje na korake do 34 ms, uz granicu 100 ms nakon duljeg zastoja. CI regresijski testovi uspoređuju ekvivalentne intervale.
- iOS callbacku uklonjena je redundantna provjera pauze nakon ranog izlaska. Na kompatibilnim uređajima može raditi do 120 Hz.
- U ovom ciklusu nisu mijenjani save/migration format, lokalna ljestvica ni grafički resursi. Ovo nije cjelovit dead-code audit svakog ekrana niti test na fizičkim uređajima; oboje ostaje za dodatnu provjeru.


## v0.1.18 — Android emulator: sistemski Pixel Launcher ANR
- Produkcijski CI na merge commitu `bbcd90b2` imao je neuspješnu provjeru otvaranja Postavki. Android APK/AAB, iOS, source audit i grafika bili su zeleni; GitHub Release je propisno preskočen.
- Pročitan je log posla `113868070085` i pregledani su screenshot `android-settings-diagnostic.png` i `android-current-ui.xml` iz artefakta `QA-Android-visual-flow` workflowa `37944417518`.
- Hijerarhija prikazuje sistemski dijalog `Pixel Launcher isn't responding` s opcijama `Close app` i `Wait`, iznad aktivnog BOPAVI početnog zaslona. BOPAVI proces ostao je aktivan; automatizirani dodir u Postavke preuzela je sistemska ANR komponenta.
- QA skripta sada može prepoznati i oporaviti samo **točan** Pixel Launcher ANR pritiskom na sistemsko `Wait`; zahtijeva da BOPAVI proces i dalje radi, ograničava pokušaje i nakon svakog koraka ponovno provjerava stvarni UI.
- Ako BOPAVI padne, pojavi se drukčiji dijalog ili se ekran Postavki i dalje ne otvori, test i dalje pada i pohranjuje screenshot, hijerarhiju i logcat. Nema preskakanja provjere ni lažno pozitivnih prolazaka.

## v0.1.18 — drugi produkcijski pad i AOSP emulator
- Workflow `37946675563`, Android smoke job `113875733201`: nakon uspješnog otvaranja Postavki ponovna provjera IGRAJ nije prošla. Spremljena XML hijerarhija iz QA artefakta `11623647855` potvrđuje da je opet prikazan isključivo sistemski dijalog `Pixel Launcher isn't responding`. Postavke su se prethodno zaista otvorile, a BOPAVI proces nije prijavljen kao ugašen.
- Android emulator QA prebačen je na službenu AOSP (`target: default`) sliku Androida 35, jer igra nema Google servise niti mrežne dozvole i ne treba Googleov Pixel Launcher koji redovito podiže ANR.
- Povratak iz Postavki sada se provjerava pet puta uz provjeru BOPAVI procesa. Oporavlja se samo točno prepoznat sistemski Pixel Launcher ANR; ako dijalog potroši Android Back, povratak se ponavlja tek nakon provjere da su Postavke i dalje otvorene.
- Test mora na kraju pronaći stvarna tri dostupna početna gumba ili jasno pasti, uz screenshot/XML/logcat. Nije dopušteno prihvatiti screenshot Postavki kao dokaz ispravnog povratka na početni zaslon.
- U audit dodana provjera sintakse shell QA skripte i prisutnosti AOSP emulator konfiguracije.

### Dopuna nakon PR #24: AOSP SDK arhiva na GitHub runneru
- PR workflow `37947986426` potvrdio je uspješne Android buildove i privacy/art audit, ali emulator se nije mogao ni instalirati: `system-images;android-35;default;x86_64` vraća `Error on ZipFile unknown archive` prije pokretanja aplikacije. Nije riječ o gameplay testu niti padu BOPAVI procesa.
- Vraćen je provjereno instalabilan emulator `target: google_apis`. Važan popravak ostaje: Android Back nakon Postavki ponovno se provjerava uz ograničenu obnovu samo potvrđenog Pixel Launcher ANR, ponovno slanje Back tipke samo ako je i dalje vidljiv zaslon Postavki te strogu provjeru sva tri početna gumba.
- Nema slabljenja smoke testa i release ostaje blokiran do zelenog stvarnog Android emulator gameplay QA na posljednjem commitu.


## v0.1.19 — vizualni parallax, lighting parity i regresije
- Uspoređeni Android `GameView.kt` i iOS `GameCanvas.swift`: nova tri sloja malih plutajućih otoka iscrtavaju se preko ilustrirane pozadine, a ispod prepreka i Bopija. Nema novog dekodiranja bitmapa unutar render petlje.
- Konkretan bug: Android `glowShaders` imao je vanjski prsten neprozirne boje; iOS vanjski alpha = 0. Androidov vanjski alpha kanal sada je 0.
- Parallax dijeli raspon 696 na pločice širine 174; tri stope pomaka 0.07/0.15/0.24 jednake su na obje platforme. Smanjene animacije zadržavaju okoliš, ali ga čine statičnim. Neispravna udaljenost ne pokreće pomak.
- Kotlin JUnit + Kotlin/Swift parity testovi provjeravaju pomake, granice i reduced-motion; Python audit potvrđuje oba renderera i da su svi svjetovi dostupni. Prije mergea CI mora potvrditi Android emulator i iOS simulator.
- Kolizije, bodovanje, migracije, novčanik, lokalna ljestvica, izvoz i uvoz nisu mijenjani. Osam svjetova odmah je dostupno; pojedinačni leveli napreduju zasebno.
- Tehnički testovi ne dokazuju vizualnu identičnost 1:1 s dostavljenim kompozitnim referencama. Potrebno je testirati zasebne ekrane i fizičke uređaje.


## v0.1.20 — Bopijev input-driven zamah
- Tap gestom Kotlin i Swift postavljaju `flapPulse=0.24` s postupnim opadanjem samo tijekom valjanih simulacijskih koraka. Ponovljeni dodir ga vraća na 0,24 s; neispravan delta ne mijenja impuls.
- Na obje platforme pomak krila dobiva dodatni 17° upstroke proporcionalan `flapPulse`; nakon otprilike 240 ms preostaje osnovna animacija. Proceduralni svjetski obojeni val i sedam sitnih čestica ostaju iza Bopija i ne mijenjaju kolizijsku geometriju.
- `reducedMotion` uklanja oba nova animirana elementa, a glasnoća, vibracije, igranje bez interneta i pohrana napretka ostaju netaknuti.
- Kotlin JUnit i Swift/Kotlin parity testovi pokrivaju impuls, ponovno pokretanje, vrijeme i trajanje. Source audit provjerava renderere i reduced-motion.
- Potrebno potvrditi PR CI na Android emulatoru i iOS simulatoru, zatim main CI i sva četiri release artefakta. Bez fizičkih uređaja nije moguće tvrditi punu 1:1 podudarnost prema slikama.


## v0.1.21 — kartice svjetova i autentični lokalni rekord
- Izvorna iOS kartica imala je naslov/natpis položen preko ilustracije bez sigurnog kontrasta. UI je preuređen u odvojeni gornji preview (137 pt) i dva natpisa na tamnoplavoj podlozi; Android već koristi tu podjelu.
- Obje platforme jasno ističu trenutačno odabrani svijet i zadržavaju zasebnu pristupačnu oznaku za cijelu karticu; svih 8 svjetova otključano je odmah.
- `ResultHeadline` na Kotlinu/Swiftu koristi `score`, `previousBest` i `won`: nula/izjednačenje nije novi rekord; pobjeda ima prednost; novi rekord mora biti strogo veći od ranijeg. UI dohvati prijašnji rekord prije `recordRun`; nakon spremanja prikazuje ažurirani osobni najbolji rezultat.
- JUnit i Swift/Kotlin parity testovi provjeravaju iste ulaze; source audit provjerava strukturu kartica, redoslijed snimanja i dostupnost osam svjetova.
- CI treba dokazati Android lint/JUnit/build, emulator gameplay QA, iOS simulator/Swift/save/device build prije mergea, zatim iste poslove na main i 4 release artefakta. Bez fizičkih uređaja nije potvrđena vizualna identičnost 1:1.


## v0.1.22 — oznake levela i pauza
- Izbornik koristi `LevelEngine.create(world,n).type` i `BopaviCore.create(world,n).type` za svaki vidljiv level, te usklađene oznake `LevelKind.name/icon` na Kotlinu i Swiftu. Simulacija igre, generator i pohrana ostaju nepromijenjeni.
- U svim svjetovima svi leveli imaju deterministički tip: 1 = Normalni, 15 = Izazovni, 30 = Bonus, 60 = Elitni, 75 = Izazovni, 90 = Bonus, 120/360 = Elitni. Zaključani leveli nisu automatski otvoreni; svih osam svjetova jest.
- Mreže prikazuju zonu i raspon stranice, ikonice i pristupačne nazive koji uključuju status; odabrani/napredni level se razlikuje vizualno bez pretvaranja stvarno zaključanih levela u otključane.
- Pauza u oba sučelja pokazuje stvarni aktivni level i tekst `ODABERI NASTAVI LET`; akcije zvuka, povratka i nastavka ostaju kao prije.
- Kotlin JUnit i Kotlin/Swift parity provjere ispituju granične nivoe i naziv/ikonu; Python audit potvrđuje integraciju. Nakon zelenog PR CI-ja treba provjeriti isto na main i 4 stvarna GitHub Release artefakta. Screenshotovi s emulatora/simulatora nisu dokaz 1:1 za svaki fizički telefon.


## v0.1.23 — regresija: pauza samo tijekom stvarnog leta
- Analizom `startGame` na obje platforme utvrđeno je da se pauza dosad stvarala vidljiva čim korisnik odabere `IGRAJ`, čak i kada simulacija čeka prvi `flap` (`active=false`).
- Sada je Android `pause.visibility=INVISIBLE`, a iOS `pause.isHidden=true`, sve do prvog `inactive→active` prijelaza unutar simulacije; iOS početni HUD je sakriven, Android/iOS rendereri odgađaju HUD do aktivnog leta. Na početnom izborniku nema kontrole pauze.
- Na odlasku na drugi zaslon prethodna gameplay hijerarhija se zamjenjuje ili uklanja. Android/iOS rezultatski callback se prihvaća samo za trenutno aktivan `GameView`/`GameCanvas`, što sprječava povrat zastarjelog rezultata.
- Android emulator QA više ne traži pauzu kao dokaz da je `IGRAJ` otvorio igru: traži stvarni `Dodirni za let Bopija` view i izričito odbija pauzu prije prvog zamaha. Nakon zamaha mora se pojaviti vidljiva pauza (ako nije već stigao završni rezultat); nakon završetka mora nestati. Početni izbornik i dalje mora izložiti točno tri pristupačna gumba bez pauze ili statusa leta.
- Kotlin JUnit + Kotlin/Swift parity testovi osiguravaju da `active` i fizika ostaju u mirovanju prije prvog dodira. Python source audit provjerava oba callbacka, ispravan početni visibility, aktivni HUD, zaštitu callbackova i rigorozni emulator skript.
- Merge i izdanje dopušteni su tek nakon zelenog PR CI-ja, zelenog novog CI-ja na main te stvarne potvrde četiri artefakta; ovo nije zamjena za usporedni screenshot QA svih zaslona s referentnim renderima.


## v0.1.24 — neprekinuti let, Portantin i zaštita sigurnosnih kopija
- **Seamless:** Simulacije unaprijed izračunavaju `nextLevel` i njegov `nextOrigin` iz fiksnog razmaka posljednje prepreke. Oba renderera crtaju dolazne prepreke prije završetka trenutnog levela, zadržavajući njihovu točnu poziciju nakon promjene broja levela. Stara metoda ubacivanja prve nove prepreke na x=300 tek nakon završetka uklonjena je.
- **Neprekidni HUD:** statični prijelazni overlay se ne iscrtava, a ukupan broj prolaza `totalPassed` monotono raste umjesto resetiranja po levelu. Test mora usporediti koordinatu prve dolazne prepreke prije i nakon granice.
- **Likovi:** šest ranijih Bopijevih boja + besplatan Portantin (`skin_index=6`), izbor prije `startGame` na Androidu i iOS-u, pregled spriteova i potvrda početka. Ostaje podržan postojeći v5 JSON save i kupljene boje, čak i kad novi kod učita stare kopije.
- **Grafika:** jedan uređivi SVG Portantina u `docs/assets/portantin.svg`, s dvije izdvojive grupe krila; isti generirani 512 px prozirni spriteovi na obje platforme. Generator ruši build ako grafički slojevi nedostaju.
- **Sigurnost:** nijedan import ne smije neograničeno učitavati datoteku od vanjskog pružatelja; granica 550000 na obje platforme provjerava se i prije i nakon čitanja. Nema novih mrežnih dozvola ni telemetrije.
- **Quality gate:** Android lint/JUnit/APK/AAB, emulator stvarni tijek IGRAJ→ODABERI LIKA→POLETI i sudar, Swift simulator/physics/save/device build te source/privacy audit. Nakon svih zelenih PR poslova slijedi merge, puni main CI i provjera četiri GitHub Release artefakta. Fizički 1:1 screenshot audit još nije automatiziran.


## v0.1.25 — zona bez skoka i ilustrirani odabir lika
- Kotlin i Swift `prepareNext` izračunavaju ulazni razmak na temelju `lastGate.x - previousGate.x`; samo sintetski level s jednom preprekom zadržava sigurnu zadanu udaljenost od 242. Položaj unaprijed nacrtanih prepreka ne mijenja se prilikom promocije sljedećeg levela. Globalna udaljenost, prolazi, scena i kamera ostaju kontinuirani.
- Kotlin JUnit i Kotlin/Swift parity provjeravaju sva osam svijeta te levele 2, 62, 122, 182, 242, 302 i 360, uključujući šest izazovnih zona i završetak ciklusa. Testovi uspoređuju razmak stvarnog prethodnog niza s prvim stupom unaprijed pripremljenog sljedećeg levela.
- Obje platforme koriste galeriju svih sedam stvarnih likova s dva stupca i čitljivim natpisima; Android koristi `R.drawable.bopi0..6`, iOS `Bopi0..6`. Portantin ostaje dostupan odmah. Preskakanje pre-flight odabira nije dodano.
- Potvrda virtualne kupnje je obvezna: novi zaključani lik ne smije smanjiti broj osvojenih kovanica na prvi dodir. Dijalog mora navesti točnu cijenu i omogućiti odustajanje. Već otključani likovi ne naplaćuju se ponovno; preostale kovanice i skin izbor spremaju se lokalno.
- Python audit provjerava prisutnost ilustracija, odvojenu pristupačnost kartica, uvjetovano trošenje i obje galerije. Prije spajanja i objave obvezni su svi Android/iOS CI koraci, Android emulator smoke QA, iOS simulator/Swift/backup testovi i potvrda sva četiri stvarna artefakta. Nije potvrđena apsolutna pixel-perfect jednakost 1:1 na fizičkim uređajima.

## v0.1.26 — Noa i Any
- Provjeriti da se Noa i Any nalaze u izboru lika prije svakog leta te među svih devet kartica u Postavkama Androida i iOS-a.
- Potvrditi svaki portret i odvojeno gibanje lijevog/desnog krila pri dodiru, uključujući smanjene animacije i mali ekran.
- Otključati Nou za 220 te Any za 240 kovanica, odbiti potvrdu bez trošenja i ponovno izabrati otključani lik bez druge naplate.
- Provjeriti da v3 i v5 JSON kopije ne gube postojeće likove, saldo ni spremljeni izbor te da v5 pohranjuje nove premium likove.
- CI mora potvrditi svih devet nepraznih, međusobno različitih RGBA tijela 512×512 te osamnaest zasebnih krila i ispravne Android/iOS pakete.

## v0.1.27 — stvarna grafika premium likova
- Za lik Noa (indeks 7) i Any (indeks 8) provjeriti da render tijekom igre pokazuje izvorno tijelo i OBA izvorna krila, a ne dijelove Portantina (indeks 6).
- CI mora provjeriti identične 512×512 RGBA resurse između Android drawable-nodpi i iOS imageset za devet tijela i osamnaest krila.
- Vizualno pregledati zamah oba krila na oba sustava, aktivno/smanjeno gibanje i povratak s pauze; napredak i otključavanja ostaju nepromijenjeni.

## v0.1.28 — regresija premium likova, kupljene opreme i povratka iz pozadine
- Android: odabrati Nou i Any i otvoriti zaslon **ODABERI LIKA**; provjeriti stvarne ilustracije bez pada i pravilno pokretanje leta.
- Android/iOS: kupiti štit i magnet, otvoriti mirujući let, zaključati uređaj, vratiti se i odustati bez zamaha; broj kupljenih predmeta mora ostati nepromijenjen. Pri prvom stvarnom zamahu svaka prisutna pogodnost troši se jednom.
- Započeti let, premjestiti aplikaciju u pozadinu i vratiti se: let mora ostati pauziran dok korisnik izričito ne odabere nastavak. Mirujući pregled mora primati dodire bez nevidljive pauze.
- Automatski CI obuhvaća Kotlin i Swift testove, Python audit, Android emulator, iOS simulator i oba builda. Testovi na fizičkim uređajima i tržišno potpisani paketi nisu potvrđeni.

## v0.1.29 — sigurnost kopija i kontrola aktivnog leta
- iOS: pripremiti v5 JSON sigurnosnu kopiju s neispravnim osmom stavkom streamFrontiers. Uvoz se mora odbiti, a cijeli prethodni spremljeni napredak, kovanice, postavke i marker već preuzetih nagrada moraju ostati nepromijenjeni.
- Android/iOS: dovršiti barem jedan level u neprekinutom letu, potom završiti pokušaj. Zaslon rezultata mora prikazati ukupan broj prolaza iz svih levela u tom pokušaju.
- Android: u aktivnom letu pritisnuti tipku ili gestu Natrag — mora se otvoriti izbornik pauze, a odabirom Nastavi let isti pokušaj mora nastaviti. U mirujućem letu Natrag vraća izbor lika i ne troši opremu; na početnom zaslonu izlazi iz aplikacije.
- CI mora potvrditi Android/JVM, Swift save i physics testove, izvorni audit, Android emulator i iOS simulator/build. Testiranje fizičkih uređaja i potpisane trgovinske instalacije i dalje su zasebni uvjeti.
- Android CI: Pixel Launcher ANR smije se oporaviti samo nakon provjere naslova, sistemskog ID-a gumba Wait i koordinata; zatim treba ponovno ispisati stvarni UI te pronaći sve premium kartice. Ako ScrollView ostane nedostupan ili BOPAVI padne, test mora pasti.

## v0.1.31 — marathon coordinate precision, Android/iOS parity
- Android and iOS obstacle screen positions are computed in Double before converting to Float, preserving collisions and pre-rendered next-level gates far beyond 2^25 world pixels.
- Run Kotlin/JVM unit tests, Kotlin/Swift generator parity, Android 35/36 emulator smoke and iOS simulator launch. Verify no reset of bird position, score, progress, scenery or camera distance at a world boundary.
- Verify `versionCode 34` is unused in Play Console before uploading any signed AAB; do not use disposable CI signing credentials.
- The AAB structure verifier fails when any unreviewed `.so` library is included. Reassess native library alignment before enabling a dependency that adds one.
- Inspect device GPU animation and background parallax in long sessions: display distance remains Float for visuals; this cycle specifically corrects collision coordinates.

## v0.1.30 — Google Play / Android 16
- Provjeriti manifest i bundle: com.brendigo.bopavi, minSdk26, targetSdk36, versionCode31. Android edge-to-edge, sigurni inseti, statusne/navigacijske trake, prediktivna gesta Natrag na Androidu 13-16; aktivni let mora se pauzirati, ne napustiti.
- Javno izdanje ima nepotpisani AAB. Samo zaštićen, ručno pokrenut Google Play signing workflow može proizvoditi potpisani AAB s privatnim upload ključem, bez objave tajni.
- Provjeriti stvarnu Play 512x512 ikonu, 1024x500 grafiku te originalne emulator screenshotove izrezane na 1080x1920 bez novih/nacrtanih sučelja. Screenshot mirujuće igre nije aktivni gameplay.
- Potvrditi Play Console Data safety, javni HTTPS privacy policy, ciljani uzrast i sadržaj, stvarni test na fizičkom telefonu i Play pre-launch report. Za primjenjivi novi osobni račun 12 testera tijekom 14 dana.
- Pratiti detaljni checklist: docs/play/RELEASE-CHECKLIST.md.
