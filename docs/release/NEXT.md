# Next Android release

## v1.0.72 — Karta svjetova i Skins: izvorna kompozicija bez vizualnih lažnih prečaca (build 202)

- **Level Map:** uklonjeno je vizualno neispravno povezivanje tri lebdeća svijeta jednim golemim drvenim mostom kroz prazno nebo. Aktivne staze razina sada ostaju na pojedinom stvarnom otoku, a svi odabiri razina, zaključavanje i spremanje zvjezdica nastavljaju raditi kao prije.
- **Level Map:** trokutaste jednostavne stijene zamijenjene su slojevitom, nepravilnijom siluetom kamenih otoka s postojećim vodopadima, vegetacijom i efektima. Ovo mijenja samo prikaz, ne hitboxove i ne progresiju.
- **Skins:** prilagođena je veličina i pozicija središnjeg Bopija kako bi originalna transparentna krila i tijelo imali referentni omjer. Sva tri stvarno igriva lika, odabir i spremanje odabranog lika ostaju funkcionalni. Ne prikazuju se izmišljene kupnje/otključavanja.
- **Gameplay:** donji šiljci i šiljci prije prvog dodira ostaju uklonjeni; stvarne prepreke, mace, sakupljanje, fizika i HUD nisu zamijenjeni slikom.
- **Nastavak:** u ovoj iteraciji prioritet su Level Map i Skins jer se njihove promjene mogu napraviti bez izmišljanja stanja ili narušavanja gameplaya. Kod drugih ekrana zadržavaju se postojeće funkcionalne izmjene iz v1.0.71.
- **Usporedba:** stvarni v1.0.71 Android 15 screenshotovi 941×1672 uspoređeni su s devet referenci vlasnika bez mijenjanja veličine; RGB-MAE: Level Map 28,901%; Skins 28,431%; Gameplay ready 27,772%; Shop 27,339%; Game Over 26,584%; Settings 26,189%; Splash 24,305%; Home 23,499%. Level Complete nije izmjeren zbog nedostajuće istovjetne stvarne snimke. Detalji u `docs/qa/OWNER-V1071-941-2026-11-10.md`.
- **Verifikacija:** 0% odstupanja nije postignuto ni potvrđeno. Bez upotrebe screenshotova kao lažnih interaktivnih slojeva; dinamični brojači i gumbi ostaju stvarni. Svaka nova izmjena mora proći Android 15/16, unit i sigurnosne provjere.
- **Distribucija:** APK/AAB izlaze iz GitHub Actions. Release APK i Google Play AAB nisu potpisani. Za Google Play mora ih potpisati vlasnik postojećim upload ključem.
