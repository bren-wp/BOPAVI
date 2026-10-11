# BOPAVI — Premium Reference Implementation

## Scope

The ten user-supplied reference renders on 10 October 2026 are **design targets**, not gameplay screenshots or final exportable UI components. Reimplement the screens using real Android and iOS controls and the same production art sources. **Never** paste a full reference screen as a static tappable image: counters, player stats, currency, pilot states and level availability must remain live and accessible.

### Premium visual contract
- Primary palette: deep azure `#0B3D91`, sky blue `#2DA9FF`, cyan `#00E5FF`, white `#FFFFFF`, golden orange `#FFC24D`, intense orange `#FF8A00`.
- Buttons: orange/yellow raised primary play / retry, blue raised secondary actions, blue-black panel surfaces with cyan outlines and selected gold borders.
- Character identity: winged **BOPAVI** logo and Bopi mascot shared between menu art and rendered in-game sprites.
- Background: floating islands and waterfalls rendered from the existing Android/iOS-matched eight world sources; menus dim the scene enough to maintain white-text contrast.
- Fonts: rounded, bold game UI; use installed platform fonts until an authorized distributable rounded font is integrated. Do **not** publish proprietary font assets from mockups.
- Navigation: a single visually dominant IGRAJ button, SVJETOVI and POSTAVKE, four actual shortcuts to existing Profile/settings, Achievements, Character gallery, and offline coin shop.
- Gallery: three columns of nine **real** characters, selected state and earned-coin status. Do not silently unlock a character or invent premium purchases.
- Level selector: 20 selectable levels per page, no fake unlocked levels; preserve the procedural level numbers and valid progress.
- Paused game: pause control only after first flap; continue/retry/exit must control an actual paused simulation.
- Result: real scores, earned currency, global total passed, stored leaderboard; do not generate random reward items.

## Screen-by-screen implementation targets

| Reference | Live screen | Scope |
| --- | --- | --- |
| Branding board | Shared BOPAVI SVG/asset generator | Color, winged blue/gold wordmark and hero |
| Splash / loading | Android launch artwork and iOS LaunchArt | Bopi, sky/islands, responsive logo |
| Home | Android `showHome`, iOS `showHome` | 3 hero actions, 4 functional tabs and real counters |
| Worlds | Android `showWorlds`, iOS `showWorlds` | 8 world illustrations and accurate progress |
| Levels | Android `showLevels`, iOS `showLevels` | Real unlocks, 20/page, accessible grid |
| Pilot picker | Android `showPilotPicker`, iOS `showPilotPicker` | 9 accessible portraits in 3 columns and real virtual-coin unlocks |
| Gameplay | Android `GameView`, iOS `GameCanvas` | Do not alter collision coordinates, obstacle clearance, active-vs-idle pause |
| Pause | Android game overlay, iOS game overlay | Preserve simulation and touch controls |
| Result | Android `showResult`, iOS `showResult` | Dark navy cards, actual score and rewards |
| Settings | Android `showSettings`, iOS `showSettings` | Real settings only; dark panels / cyan toggles |

## Non-negotiable fidelity limitations

A generated composite PNG is **not** sufficient to recreate a maintainable, interactive, responsive and pixel-identical application. Exact pixel matching needs approved **separate source exports** (background without baked-in text, nine character portraits, world illustrations, logo, button states, icon atlas, typography licenses) plus screenshot comparisons at defined Android/iOS device sizes. This stage implements the live interface structure and palette; final-art fidelity remains an explicit open work item.

The concept references also display `diamonds`, `plus` top-up buttons, online/cloud sync, logout and video rewards. None is shipped as a fake control: BOPAVI is an offline game with earned in-game coins, no paid economy, no account, and no cloud backend. A future redesign may add those only after specification, security review and functioning implementation.

## Acceptance / regression gates

1. Build Android APK + **unsigned** AAB and iOS simulator + unsigned device app.
2. Run Android API35/API36 end-to-end screenshots / interaction including Portantin, Noa, Any and no premature pause.
3. Run Kotlin, Swift parity tests, legacy save migration and privacy/source audits.
4. Compare real screenshots against the supplied references and record mismatches **per screen** (layout, colors, typography, alignment, visual art, accessibility, safe areas). Do not claim 1:1 until independently verified.
5. Confirm no CI or manual signing workflow runs on GitHub, no fake Play signed uploads.
6. Do not merge/redesign production assets until the latest PR-head checks pass.

## v0.1.34 verified target changes
- The PROFILE footer tile is an actual page showing the stored player, selected full-wing sprite and four genuine local metrics, not a redirect to Settings.
- Music and sound sliders now lead directly to vibration, then the existing three difficulty cards. Master sound and reduced-motion switches remain implemented under Additional Options.
- No mock logout, cloud save, paid gems, premium unlock or reward ads are introduced to simulate reference art.
- Future art work must compare actual Android/iOS screenshots with the supplied renders and document the missing raster/vector exports; identical UI pixels are not claimed by this code change.
