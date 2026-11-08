# Next Android release

## v1.0.66 — Splash/Home reference camera and nine-screen pixel audit (build 196)

- Splash and reference-size Home now hide navigation bars with swipe-to-reveal transient system controls, following the full-bleed 941×1672 owner artwork. Compact/large-font Home remains navigation-safe.
- Splash: a sky haze and lower olive shadow are composited only over the landscape before the native mascot, logo, rings and genuine loading track; this improves color-depth alignment without inventing a loading percentage.
- Home: lightened upper sky and darkened foreground foliage under native buttons, matching the owner's blue/green depth progression.
- Home's actual wing/body actor uses an enlarged 680×680 reference square at (80,274), rather than the too-small 615×615 actor at (116,294); all three real pilot layers and gameplay semantics stay unchanged.
- Added tools/qa_owner_941_all_screens.py with SHA-256 checks for ALL NINE unmodified 941×1672 owner PNG references, no image resizing/cropping/masking, RGB-MAE and exact changed pixel counts, and focused Splash/Home regions.
- Added regression tests proving that a single one-channel, one-pixel difference cannot pass strict 0%, and connected those tests to GitHub Actions.
- Independent real native Android snapshots and working accessible UI controls remain required. Pixel equality of a flattened screenshot alone cannot pass functional acceptance.
- Baseline v1.0.63 measured RGB channel deviation: Home 24.42%, Splash 25.34%, Game Over 26.62%, Settings 26.77%, Shop 27.25%, Gameplay 28.29%, Skins 28.72%, Level Map 29.02%. Level Complete was not in that QA capture set.
- These are baseline measurements, NOT claims that v1.0.66 has achieved 0%; this release must pass native Android emulator UI flows and produce real fresh APK/AAB/screenshots before publication.
- Offline wallet, saves, navigation destinations, purchases and physics remain unchanged. Debug APK is for QA; Play AAB/release APK remain unsigned pending private publisher signing.
