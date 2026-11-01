# Next Android release

## v1.0.42 — Home wing motion and 3D control states (build 172)

- The large 941×1672 native Home now reuses its actual transparent left-wing, right-wing and body sprite resources in a lifecycle-aware `HomeWingActor`. The subtle native wing flaps stop when the window becomes invisible or is detached and respect disabled system animator motion. The central body stays fixed; gameplay physics and saved state are unchanged.
- The shared local enamel drawable used on Home, other menus and Pause now reacts to real Android pressed, selected and disabled states. Pressed buttons have reduced extrusion; selected surfaces gain a restrained reflective overlay; disabled level-map actions are visibly dimmed without becoming clickable.
- Retain native `HomeEmbossedLabel` TextView accessibility and actual wallet values. Add source regression checks for wing layer ordering, animator cancellation, Android drawable state propagation and 3D menu reuse.
- **Visual QA remaining:** actual owner reference rasters are not stored in the repository. Pixel-by-pixel matching against the original 941×1672 concept images requires the supplied originals and real emulator screenshots; code contracts cannot certify an exact visual match.
- **Release guard:** No merge, signature or publication requested. CI must pass Android 15/16 emulator navigation, Gradle unit/lint/build, asset audit and source contracts before review.

- Measured the owner's newly supplied 941×1672 Home composition against real API35 screenshot artifacts from the last green main workflow. The old three-card row plus two-card footer was inconsistent with the reference. Native layout now uses two horizontal SKINS/SHOP buttons, one wide SETTINGS button, and independent circular ACHIEVEMENTS/LEADERBOARD/SOUND targets on the bottom row. The saved coin and star wallet chips move to the top-right placement from the reference; no fake gem inventory, prices or content added. Updated Kotlin geometry and Python source regression tests. Exact artwork identity remains unverified.
