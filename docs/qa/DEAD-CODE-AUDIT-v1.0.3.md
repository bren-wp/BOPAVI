# BOPAVI — Android dead-code, stability and privacy audit (v1.0.3 kandidat)

**Datum:** 20. 10. 2026. **Opseg:** jedina nativna Android aplikacija, Kotlin, Canvas, fizičke kolizije, asset generatori, lokalne preference, GitHub Actions i metadata za Play.

## Provjereno i uklonjeno

- Iz produkcijskog `LevelEngine.kt` uklonjen je neaktivan opsežan katalog prijevoda na 26 jezika, `ResultHeadline` i `LevelKind` (pomoćne klase starih uklonjenih izbornika).
- Nakon provjere stvarnog poziva u `GameView.drawParallaxIslands` klasa `ParallaxScenery` **zadržana je** i vraćen je njezin Kotlin regresijski test; riječ je o aktivnoj logici, a ne mrtvom kodu.
- Iz `GameView` i `MainActivity` uklonjeni su prevoditeljski pozivi. Sve dostupne komande, HUD i pristupačni opisi postali su izravno hrvatski.
- Ne brišu se stari bitmapi ni datoteke bez potpunog inventara generatora jer artwork kontrolira trenutni renderer i potpisani reproducibilni build.

## Namjerno očuvano — nije sigurno slijepo brisati

- `ProgressStore`: `exportJson`, `importJson`, povijesni ključevi, virtualne kovanice/perkovi i svjetski rekordi služe kompatibilnosti starih spremljenih podataka, unatoč uklonjenim izbornicima.
- `GameSimulation`: podrška za svjetove, proceduralne razine i deterministička fizika potrebna je regresijskim testovima i postojećem zapisu napretka.
- V1 i V6 JSON save payload, originalni Bopi art, šest starih svjetova u resursima (ukupno osam) i stabilne GitHub objave.
- `QA` opcije aktivne su samo u debug buildu te ne daju mogućnost korisniku mijenjati produkcijski sudar.

## Provjere

- `python tools/audit_native_dead_code.py`: privatni simboli, importi, audio inventar, reference obvezne grafike, bez iOS projekta/Swifta, bez INTERNET dopuštenja i bez vraćanja neaktivnog UI kataloga.
- Novi izvori regresija: `tests/test_hr_ui_contract.py`, `tests/test_physics_visual_contract.py`, `tests/test_in_app_privacy.py`.
- `gradle :app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease`; stvarna provedba provjerava se u CI izvješću odgovarajućeg commita.
- Emulatorski Android 15/16: stvarni GameView, aktivni HUD, rezultat, povratak, kompaktna i visoka rezolucija te provjera da korisnički tok ne preskače korake.

**Ograničenja:** statički audit nije dokaz da nema koda koji koristi refleksija ili migracija. Bez fizičkog Android uređaja i potvrde stvarnih korisničkih referenci nije moguće tvrditi 100% stabilnost, identičnost 1:1, izostanak ANR-ova ni odobrenje Play Storea.
