# Next Android release

## v1.0.80 — Napredak i Karta razina: nativna navigacija i referentni raster (build 210)

- **Napredak:** svih osam kartica svjetova postaju prave pristupačne Android kontrole. Odabirom kartice otvara se točan svijet na Karti razina, s postojećim spremljenim novčićima, zvjezdicama, razinama i statistikama.
- **Čitljivost:** naslov, vrijednosti rezultata, broj novčića i zamaha te broj zvjezdica skaliraju se unutar postojećeg prostora bez dodavanja izmišljenih vrijednosti ili novih valuta.
- **Karta razina:** na izvornom 941×1672 prikazu ilustracija sada koristi puni referentni raster; interaktivni elementi i dalje poštuju sigurne sistemske rubove i najmanje 48dp dodirne površine.
- **Svi svjetovi:** sačuvana je izvorna paleta prve stranice Sky Meadows/Crystal Ruins/Volcano Peaks. Kasnije stranice dobivaju zasebne lokalno crtane palete za preostale svjetove, bez promjene stvarnih zaključavanja ili položaja čvorova.
- **Rotacija/DPI:** otvoreni zasloni Napredak i Karta razina ponovno se sastavljaju nakon promjene orijentacije, veličine i gustoće; trenutačna igra ne prekida se.
- **QA:** Android/JVM i stvarni emulator provjeravaju funkcionalne prijelaze Progress → World Map, uključujući promjenu veličine i stvarne nove snimke zaslona. Prošli rezultat ne smije se predstavljati kao 0 % RGB podudarnost.
- **Distribucija:** novo GitHub izdanje s APK/AAB paketima (release paketi nepotpisani), 17 stvarnih Android snimki i SHA-256 kontrolom; bez novog pull requesta.
