# BOPAVI — Google Play objava, Android

**Posljednje stvarno objavljeno GitHub izdanje:** v1.0.2 (build 132).  
**Sljedeći kandidat:** v1.0.3 (build 133), dok prođu sve provjere.  
**Identitet:** `com.brendigo.bopavi` · Brendigo · https://brendigo.com.

## Obvezne tehničke i pravilničke provjere

1. `targetSdk = 36` (Android 16). Google Play od 31. 8. 2026. zahtijeva API 36 za standardne nove mobilne aplikacije i ažuriranja. Izvor: https://support.google.com/googleplay/android-developer/answer/11926878.
2. `versionCode` mora biti veći od najvećeg broja već učitanog u **Play Console**, što se ne može zaključiti samo iz javnih GitHub izdanja. Zadržati `com.brendigo.bopavi` i kompatibilnost sa starim rezultatima.
3. GitHub proizvodi **nepotpisani AAB** i testni APK. Službeni izdavač potpisuje AAB postojećim zaštićenim Google Play upload ključem; ne objavljivati lozinke, privatne ključeve ni keystore.
4. Provjeriti konačni manifest i ovisnosti: igra nema INTERNET dopuštenje, oglase, analitiku, kupnje unutar aplikacije, račun ni udaljenu pohranu. Rezultati su lokalni. Potvrditi to na stvarnom finalnom paketu prije ispunjavanja Google Play **Data safety** obrasca.
5. Unutar aplikacije dostupna je lokalna hrvatska politika privatnosti. Play Console također zahtijeva **javno dostupan URL privatnosti**, koji treba zasebno otvoriti u anonimnom pregledniku. Sama GitHub datoteka ili naslovna stranica Brendigo nisu dokaz valjanog URL-a.
6. Opis, ikona, kratki opis i screenshotovi smiju prikazivati samo stvarnu trenutačnu igru: Početna, Let, Pauza, Rezultat i pregled privatnosti. Ne navoditi nepostojeću trgovinu, profile, otključavanje likova ili izvoz podataka.
7. Screenshotove automatski snimati s instaliranog **istog Android builda**, potvrditi aktivno stanje leta i ručno provjeriti čitljivost svih zaslona. Ne predstavljati referentne mockupove kao aplikacijske screenshotove. Izvor: https://support.google.com/googleplay/android-developer/answer/9898842.
8. Provesti Kotlin testove sudara/fizike (30/60/90/120 Hz), Android Lint, API 35/36 emulator, različite visine zaslona, TalkBack, Back, privatnost, audio-focus i scenarije s postojećim spremljenim rezultatima.
9. Završiti Google Play obrazac sadržaja aplikacije, IARC dobnu ocjenu, ciljanu dob, deklaraciju oglasa i pravila za djecu prema stvarnom proizvodu. Provjeriti pre-launch izvješće prije produkcijske objave.
10. Pregledati postojeće javne v1+ GitHub releaseove. Nijedan prethodno objavljeni tag ili artefakt ne smije biti obrisan ili zamijenjen.

**Stanje:** pripremljen izvorni kod i kontrolni popis; bez pristupa potpisnom ključu, Play Console obrascima i fizičkom uređaju nije moguće proglasiti Google Play odobrenje. U izdanje pustiti samo commit čiji su svi obvezni CI poslovi zeleni.
