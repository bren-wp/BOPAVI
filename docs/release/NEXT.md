# Next Android release

## v1.0.56 — Home and Splash <=10 percent RGB deviation target (build 186)

**Draft candidate only: NEVER publish as visually accepted until actually measured.**

The owner has supplied EXACT Home and Splash/Loading reference PNGs at
941 × 1672 pixels. The acceptance criterion is **at most 10% normalized
8-bit RGB mean absolute error on BOTH entire screens AND separately on
EVERY named region**. In numeric terms: **RGB MAE <= 25.5 / 255**.

| Last confirmed version | Total RGB MAE | Normalized error | Target |
| --- | ---: | ---: | ---: |
| Home (published v1.0.55) | 72.543 / 255 | 28.448% | <=10% |
| Splash (published v1.0.55) | 74.555 / 255 | 29.237% | <=10% |

The code and decoration tweaks in this v1.0.56 candidate are NOT YET
proven to improve these measurements. `tools/qa_two_owner_reference_screens.py`
measures original owner PNGs against unmodified Android captures and reports
full-screen and named ROI errors. `--enforce-target` fails whenever either
screen OR a single named region exceeds 10%. No claim of 1:1 equality is made.

### Home-only work

- Move original 512×512 three-layer animated pilot from (152,342,645²)
  to (100,282,660²) with reference-painted alpha starting near y=393.
- Place the original flying crown wordmark at (110,100,720×285).
- Keep real native Play, Skins, Shop, Settings and footer controls
  clickable, independently accessible and aligned; enlarge raised 3D
  lettering, golden Play arrow and pearly bevel.
- Keep live coin/star balances; account for actually VISIBLE status bars
  and physical display cutouts without displacing HUD from the top.
- Strengthen softly blurred dark lower foliage in the offline local sky.

### Splash/Loading-only work

- Move original three-layer flying pilot to (110,280,670×670).
- Widen gold/navy/cyan 3D winged loading frame to (161,1318)–(780,1422).
- Preserve honest indeterminate animation until genuine stream preparation
  reports completion, native accessibility and proper lifecycle.
- Keep local offline BOPAVI art; no rasterized whole-screen UI substitute.

### Blocking gates

- [ ] Source privacy, dead-code, release version, image generator audits
- [ ] Kotlin/JVM, Android 15 and 16 emulator tests pass
- [ ] Candidate 941×1672 unmodified Home AND Splash captures available
- [ ] Home full-screen MAE <=25.5 and **all Home ROIs** MAE <=25.5
- [ ] Splash full-screen MAE <=25.5 and **all Splash ROIs** MAE <=25.5
- [ ] Real wallet values, genuine loading state and accessible controls
- [ ] No mismatch in Bopi layers, Play/Skins/Shop/Settings, insets or touch

**Do not merge or publish v1.0.56 simply because Gradle and CI succeed.**
A successful build is not a validated 10% visual result. Published releases
remain intact and publisher upload-signing secrets never enter the repo.
