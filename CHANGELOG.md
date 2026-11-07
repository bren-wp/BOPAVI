# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.59 — Splash i Home: bolji 3D detalji i galerija svih zaslona (build 189)

**Published:** 2026-11-07 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.59)

- Nastavljeno izravno na main: isključivo Splash i Home izgled; ostale zaslone i gameplay fiziku ne mijenjamo.
- Splash/Home: osvijetljeniji plavi gradijent neba te slojeviti oblačni vrtovi, zelenilo i sitno bijelo cvijeće u donjoj trećini pozadine; svi dekorativni slojevi ostaju ispod funkcionalnog Android sučelja.
- Splash/Home: B, O, P i A ponovno su četiri odvojene 3D konture s vlastitim tamnoplavim rubom i autentičnim izrezima; prethodna spojena silueta bila je nečitljiva.
- Splash/Home: novi vektorski obris BOPA izravno je rekonstruiran iz dostavljene 864px vlasničke reference, s mekim kubnim krivuljama i očuvanim otvorima slova; slojevi reljefa ostaju odvojeni i lokalni.
- Home: COINS i GEM sada su jedna zajednički poravnata gornja HUD traka i fizički izrez zaslona ne može spustiti samo jedan novčanik preko logotipa.
- Stabilnost: za Android 16 emulator prednost ima provjerena AOSP sistemska slika, čime se uklanja nepotrebno prvo pokretanje Google usluga koje je pratilo stvarni ANR u CI-u; testni prolaz i dalje je obavezan.
- Splash: krilati BOPAVI logotip s odvojenim plavim slovima V/i, zasebno prilagođen leteći Bopi, zlatni prstenovi, izvorna traka učitavanja s istinitim indeterminiranim stanjem i bolji položaj na visokim Android zaslonima.
- Home: zaobljena bijela tipografija s tanjim 3D rubom i manjim tamnim pomakom natpisa na glavnim gumbima.
- Home: prilagođene veličine 3D natpisa i ikonica Skins, Shop i Settings; postojeći Play, ostali gumbi, stvarna spremljena stanja i pristupačnost nastavljaju raditi.
- Novi README donosi opis igre i **16 stvarnih Android snimki**: Home, Splash, Skins, Shop, Settings, Achievements, Leaderboard, Level Map, Options, Records, Privacy, Terms, Flight Preview, Flight Active, Pause, Results.
- Objavljujemo stvarne PNG datoteke iz Android 15 emulatora, a ne konceptne slike. Nema skrivenog rasta broja valuta, zamjene interaktivnih zaslona snimkom ili fingiranja uspješne provjere.
- Ugrađen je strogi test stvarnog odstupanja od referenci na 864×1536. **0% odstupanja ostaje cilj, nije dokazano.** Pozadinska ilustracija, Bopi i 3D slojevi ostaju glavni vizualni nedostaci.

## v1.0.58 — Splash and Home deeper owner-reference 3D artwork (build 188)

**Published:** 2026-11-07 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.58)

- Splash and Home only: replace a rough warm BOPA letter silhouette with six much more accurate source-contour vector paths, including independently hollow counters, preserving the existing crown, wings and native vector layers.
- Splash: relocate the gold/navy/cyan winged loading track and raised caption downward by 32 source px. The cyan highlight is now a broad 66% *moving indeterminate scanner*, **not** a fake readiness percentage. The sweep begins relative to this view's creation; only successful level preparation fills the complete track.
- Home: three bottom shortcut medallions gain reference-dark navy enamel centres and gold sidewalls. Their embossed white captions have smaller shadow offsets and accurately raised strokes.
- Original blue Bopi on Splash/Home receives independently drawn native red flying scarf, gold crown clasp and second cyan aviator lens. Existing three sprite layers and animated wings remain active. Other skins, game characters, hitboxes and gameplay stay untouched.
- Offline 900×1600 fantasy landscape now has translucent, painterly stacked cloud banks at the top, sides and distance. The still-inexact source towers and waterfalls are not replaced with a flattened screenshot.
- Baseline measured from published **v1.0.57** native Android 941×1672 screenshots against two user-owned 864×1536 JPEG originals, resampled for diagnostic comparison only: Splash **70.673/255 = 27.715%** normalized RGB MAE, Home **64.734/255 = 25.386%**. These are baseline values, **not measured results for v1.0.58** and not 1:1.
- Every major button, route, saved currency, audio control, indeterminate loading and TalkBack interaction remains truly native; no extra network permission, online purchases or fictional game scores.
