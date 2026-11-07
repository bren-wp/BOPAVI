# BOPAVI — Splash + Home, dvije nove reference (864×1536)

**Datum:** 2026-11-07. **Polazište:** main poslije izdanja v1.0.56.

## Opseg i podrijetlo

Ova faza obuhvaća **samo Splash/Loading i Home**. Vlasnik je dostavio
dva JPEG-a 864×1536, koja se ne pohranjuju u javni GitHub niti se koriste
kao statična aplikacijska pozadina/kontrola.

- Splash SHA-256: `18dd38645ad3b4255e08723ba1ef7a5b8eaf4702dbda90cae238e77e17524d2a`
- Home SHA-256: `358f7369690d28d63d9b14fc178e819dd6535a982bd9c110c15064bf3c5214aa`
- Stvarni Android screenshotovi: GitHub Actions run
  [42342847741](https://github.com/bren-wp/BOPAVI/actions/runs/42342847741),
  `QA-Android-visual-flow` artefakt `13561729857`
  (`android-loading-reference-size.png`, `android-home-reference.png`).

Izvor se skalira s 864×1536 na 941×1672 metodom LANCZOS **isključivo za
usporedbu**, Android snimka ostaje neizmijenjena. Pokazatelj:
MAE = srednja apsolutna RGB razlika / 255. PNG iz starih referenci 941×1672
nije automatski isto što i ova dva nova vlasnikova JPEG-a.

## Iskreno polazište prije izmjena

| Zaslon / regija | MAE (0..255) |
|---|---:|
| Splash — cijeli zaslon | **75,38** (29,56%) |
| Splash — winged logo | **88,85** |
| Splash — leteći Bopi | **77,05** |
| Splash — zlatni prstenovi | **65,33** |
| Splash — loading bar i natpis | **90,27** |
| Splash — svijet lijevo/desno | 72,77 / 72,64 |
| Home — cijeli zaslon | **69,38** (27,21%) |
| Home — winged logo | **81,68** |
| Home — leteći Bopi | **66,31** |
| Home — Play | **61,63** |
| Home — Skins i Shop | **80,27** |
| Home — Settings | **66,79** |
| Home — medalje | **78,41** |
| Home — valuta | **59,48** |

**Cilj** je najviše 25,5 RGB MAE (10%) za oba puna ekrana **i sve zasebne
regije**, pa nije dovoljno urediti samo pozadinu ili naslov.

## Kodne promjene u ovoj grani

1. **Logo BOPAVI:** oblik BOPA i Vi dobio je zaobljene individualne native
   SVG konture, izvučene iz vlasnikove vlastite grafike (približenje
   3 piksela), s pravim šupljinama i zasebnim 3D tamnonavy, koraljnim,
   zlatnim i svijetloplavim slojevima. Izbačen je neprikladni generički
   `DejaVu Sans` oblik slova; pera krila i drvena podloga posloženi su
   prema visini novih kontura. `tools/generate_images.py` zadržava
   lokalni rasterizacijski put — aplikacija koristi stvarni `logo.png`,
   ne screenshot cijelog zaslona.
2. **Splash:** perspektiva šest zlatnih šupljih prstenova, uključujući
   veliki djelomično odrezan prsten u donjem lijevom prednjem planu.
   Zadržano je stvarno preflight stanje, `Loading...` i dinamički
   cyan segment bez izmišljene postotne vrijednosti.
3. **Home:** stvarne 3D CTA kontrole imaju naglašenije tamne rubove,
   izdignute bočne stijenke i bogatije boje (Play zlatna, Skins ljubičasta,
   Shop zelena, Settings plava). Novčanici su tamnonavy, a izvorne
   funkcionalne `coins()`/`worldStars` vrijednosti ostaju
   nepromijenjene, kao i svi `onClick` i TalkBack opisi.
4. **QA:** novo `tools/qa_owner_864_splash_home.py` mjeri baš
   ove dvije reference i nativne Android snimke, bez ugradnje referenci
   u produkciju, lažiranja novčanika ili prebojavanja testova.

## Što još objektivno odstupa

Nakon zahvata potrebno je ponovno **izmjeriti nove Android snimke**;
uspješan Android CI nije automatski smanjenje MAE.

Najveći nesklad ostaju **slikarska 3D fantazijska pozadina** s tornjevima,
slapovima, plutajućim otocima, oblacima i šumskim prednjim planom te
**lik Bopija**. Postojeća lokalna pozadina računalno je pojednostavnjena,
a odvojeni 224×192 odobreni spriteovi nisu isti ilustratorski original
kao ova dva screenshotova. Slike scena nemaju odvojene čiste slojeve
bez logotipa, Bopija i gumba. Nije legitimno obećati 1:1 pomoću
pomicanja kontrola ili podmetanja spljoštene snimke. Za pravu
podudarnost trebat će izvorni višeslojni art, odnosno vjerno rekonstruirani
zasebni pozadinski i likovni slojevi; funkcionalni gumbi i spremanje
moraju ostati nativni.

## Kriterij završetka

- [ ] Gradle kompilacija, lint i jedinični testovi.
- [ ] UI testovi na stvarnom emulatoru i Android 16 navigacija.
- [ ] Ponoviti ROI mjerenja na Android captureovima posljednjeg SHA.
- [ ] Usporediti svako slovo, rub, sjenu, krilo, pozadinu i gumb.
- [ ] Ne tvrditi 1:1 dok obje cjelovite slike i sve regije ne zadovolje 10%.
- [ ] Izdati APK/AAB nakon prolaska kvalitativnih provjera i spajanja koda.

Nema generiranih slika u chatu niti promjena Gameplay/Game Over/Settings zaslona.
