# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.73 — Odabir junaka / Karta razina (build 203)

**Published:** 2026-11-10 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.73)

- Skins: četiri jednako raspoređene, stvarne kartice umjesto prethodne tri, uz izvorni raspored vlasnikove reference 941×1672. Postojeći Bopi, Sova i Lisac ostaju sa starim ID-jevima, podacima o spremanju i pravim sposobnostima.
- Star Pilot: četvrti junak koristi **postojeći Orion** (cosmetic skin #8), čiji se pravi sprite generira u projektu. Prikazuje se zaključan dok taj skin nije kupljen prikupljenim novčićima. Otključavanje koristi postojeću cijenu (240), provjeru stanja, **zasebnu potvrdu korisnika** i trajno spremanje odabira. Nema in-app naplate, izmišljene gem valute ili prečaca preko spremljenog stanja.
- Skins: redizajnirane su kartice u četiri stupca, izbornik ima native touch kontrole te je uklonjen netočan dodatni natpis FREE • UNLOCKED ispod info panela.
- Level Map: vizualne korekcije natpisa i proceduralnog scenografskog sloja usklađuju oblik/platforme s referencom bez lažnog otključavanja ili promjene brojača zvjezdica.
- Testovi: provjeriti jednostruku kupnju, odbijanje nedovoljnih novčića, save ID kompatibilnost, oba smjera navigacije, četvrti sprite na Home/Flight/Result i rezolucije s urezima zaslona.
- Vizualno: cilj 1:1, ali ne izjavljuje se 0% prije stvarnih screenshotova i mjerenja. Baseline v1.0.72: Level Map 28,781% / Skins 27,885% RGB-MAE na originalnim 941×1672 kadrovima.
- Distribucija: objaviti tek nakon Android 15/16, testova i uspješne GitHub Actions verifikacije. APK za release i AAB su nepotpisani; Play ažuriranje treba postojeći upload ključ.

## v1.0.72 — Karta svjetova i Skins: izvorna kompozicija bez vizualnih lažnih prečaca (build 202)

**Published:** 2026-11-09 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.72)

- **Level Map:** uklonjeno je vizualno neispravno povezivanje tri lebdeća svijeta jednim golemim drvenim mostom kroz prazno nebo. Aktivne staze razina sada ostaju na pojedinom stvarnom otoku, a svi odabiri razina, zaključavanje i spremanje zvjezdica nastavljaju raditi kao prije.
- **Level Map:** trokutaste jednostavne stijene zamijenjene su slojevitom, nepravilnijom siluetom kamenih otoka s postojećim vodopadima, vegetacijom i efektima. Ovo mijenja samo prikaz, ne hitboxove i ne progresiju.
- **Skins:** prilagođena je veličina i pozicija središnjeg Bopija kako bi originalna transparentna krila i tijelo imali referentni omjer. Sva tri stvarno igriva lika, odabir i spremanje odabranog lika ostaju funkcionalni. Ne prikazuju se izmišljene kupnje/otključavanja.
- **Gameplay:** donji šiljci i šiljci prije prvog dodira ostaju uklonjeni; stvarne prepreke, mace, sakupljanje, fizika i HUD nisu zamijenjeni slikom.
- **Nastavak:** u ovoj iteraciji prioritet su Level Map i Skins jer se njihove promjene mogu napraviti bez izmišljanja stanja ili narušavanja gameplaya. Kod drugih ekrana zadržavaju se postojeće funkcionalne izmjene iz v1.0.71.
- **Usporedba:** stvarni v1.0.71 Android 15 screenshotovi 941×1672 uspoređeni su s devet referenci vlasnika bez mijenjanja veličine; RGB-MAE: Level Map 28,901%; Skins 28,431%; Gameplay ready 27,772%; Shop 27,339%; Game Over 26,584%; Settings 26,189%; Splash 24,305%; Home 23,499%. Level Complete nije izmjeren zbog nedostajuće istovjetne stvarne snimke. Detalji u `docs/qa/OWNER-V1071-941-2026-11-10.md`.
- **Verifikacija:** 0% odstupanja nije postignuto ni potvrđeno. Bez upotrebe screenshotova kao lažnih interaktivnih slojeva; dinamični brojači i gumbi ostaju stvarni. Svaka nova izmjena mora proći Android 15/16, unit i sigurnosne provjere.
- **Distribucija:** APK/AAB izlaze iz GitHub Actions. Release APK i Google Play AAB nisu potpisani. Za Google Play mora ih potpisati vlasnik postojećim upload ključem.
