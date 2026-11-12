# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.81 — Napredak i Karta razina: poravnanje nativnih elemenata (build 211)

**Published:** 2026-11-12 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.81)

- **Karta razina / HOME:** uklonjen vizualni duplikat povratne strelice koji se vidio na stvarnoj Android 15 snimci iz v1.0.80. Nativna kontrola sada u jednom Viewu crta 3D plavi okvir i vektorsku strelicu, koristeći identičnu transformaciju i jednu pristupačnu dodirnu zonu.
- **Napredak:** stvarne kartice osam svjetova koriste mjerenu visinu umjesto fiksne prazne površine 192dp, čime više sadržaja stane na zaslon; boje, star-milestone, spremljeni rezultati i navigacija ostaju stvarni.
- **Pristupačnost:** kartice Napretka dobivaju Android ripple pri dodiru. Dugi brojevi završenih razina i opisi zvjezdica ne smiju se odrezati zbog povećanog fonta.
- **Regresije:** dodana je zasebna provjera točno jedne 3D HOME kontrole na mapi te emulatorska provjera jedinstvene nativne HOME radnje; postojeći Android 15/16 testovi ostaju uključeni.
- **Paketi:** novo GitHub izdanje uključuje Android APK, nepotpisane release APK/AAB datoteke, stvarne snimke zaslona i SHA-256 kontrolne vrijednosti.
- **Ograničenje:** 0 % RGB odstupanja od vlasnikovih referenci još nije potvrđeno. Potrebna je usporedba neizmijenjenih izvornih PNG referenci s novim emulator snimkama pri jednakom stanju igre.
- **Ispravak pada Android 15:** jedinstveni HOME gumb provjerava se točnim aplikacijskim paketom i pristupačnim opisom, bez Androidovog sistemskog HOME elementa. Stvarne aplikacijske duplikacije ostaju zabranjene i izazivaju pad testa uz spremanje XML dijagnostike.
- **Napredak:** Android 15 prolazi kroz svih osam nativnih kartica i provjerava čitljivost pristupačnih naziva te dodirne površine od najmanje 48 dp. Dodatne stvarne snimke bilježe sredinu i dno popisa pri 941x1672.
- **Karta razina:** dodatne nativne snimke i navigacijski testovi pokrivaju sve tri stranice svijeta, povratak na prvu stranicu i jedinstveni HOME. Kotlin JVM test provjerava da se šest čvorova ne preklapa s ostalim čvorovima, natpisima ili šest glavnih kontrola unutar 941x1672.
- **Ograničenje:** potvrda stvarne geometrije i interaktivnosti ne dokazuje 0 % RGB odstupanja bez identičnog izvornog referentnog PNG-a i istog stanja igre.
- **Čitljivost Napretka:** stvarna snimka donjeg dijela svih osam kartica pokazala je da se opis zvjezdica približavao donjem dekorativnom 3D luku. Povećan je isključivo unutarnji donji razmak s 14dp na 32dp, uz sačuvanu automatsku visinu, podudarnu tipografiju i podatke iz ProgressStore.

## v1.0.80 — Napredak i Karta razina: nativna navigacija i referentni raster (build 210)

**Published:** 2026-11-12 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.80)

- **Napredak:** svih osam kartica svjetova postaju prave pristupačne Android kontrole. Odabirom kartice otvara se točan svijet na Karti razina, s postojećim spremljenim novčićima, zvjezdicama, razinama i statistikama.
- **Čitljivost:** naslov, vrijednosti rezultata, broj novčića i zamaha te broj zvjezdica skaliraju se unutar postojećeg prostora bez dodavanja izmišljenih vrijednosti ili novih valuta.
- **Karta razina:** na izvornom 941×1672 prikazu ilustracija sada koristi puni referentni raster; interaktivni elementi i dalje poštuju sigurne sistemske rubove i najmanje 48dp dodirne površine.
- **Svi svjetovi:** sačuvana je izvorna paleta prve stranice Sky Meadows/Crystal Ruins/Volcano Peaks. Kasnije stranice dobivaju zasebne lokalno crtane palete za preostale svjetove, bez promjene stvarnih zaključavanja ili položaja čvorova.
- **Rotacija/DPI:** otvoreni zasloni Napredak i Karta razina ponovno se sastavljaju nakon promjene orijentacije, veličine i gustoće; trenutačna igra ne prekida se.
- **QA:** Android/JVM i stvarni emulator provjeravaju funkcionalne prijelaze Progress → World Map, uključujući promjenu veličine i stvarne nove snimke zaslona. Prošli rezultat ne smije se predstavljati kao 0 % RGB podudarnost.
- **Distribucija:** novo GitHub izdanje s APK/AAB paketima (release paketi nepotpisani), 17 stvarnih Android snimki i SHA-256 kontrolom; bez novog pull requesta.
