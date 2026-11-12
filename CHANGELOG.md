# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.82 — Offline progress transaction hardening (build 212)

**Published:** 2026-11-12 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.82)

- Serialize skin purchases, milestone awards, consumable usage and import within the active ProgressStore.
- Commit coins, purchased skin and ownership together; return failure rather than accepting an unpersisted purchase.
- Commit level frontier and one-time earned reward together; bound the win counter.
- Make v1–v6 save import durable before success is reported, preserving historical skin IDs and save keys.
- Re-enable five overlooked QA source contracts in CI and update the outdated pause-action expectation.
- Add a regression source contract for wallet, milestone, perk and import changes.
- This release candidate does not establish pixel-perfect RGB equality, cross-provider SAF atomicity, physical-device performance certification or private signing.

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
