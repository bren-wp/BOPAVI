# BOPAVI — detaljne upute za Google Play (v0.1.31)

Ažurirano 10.10.2026. Repozitorij: https://github.com/bren-wp/BOPAVI. Paket: **com.brendigo.bopavi**. Android versionName **0.1.31**, versionCode **34**, minSdk **26**, compileSdk/targetSdk **36**.

## 1. Što izdanje uključuje

- Izvorni Kotlin/Android projekt koji cilja API 36, uz novu Android 13–16 obradu geste Natrag i podršku za starije uređaje.
- Automatizirane sigurnosne i funkcionalne provjere, Android APK, **nepotpisani** Android AAB, iOS simulator/device ZIP (iOS uređajni ZIP također nije potpisan za App Store).
- ZIP BOPAVI-Google-Play-listing-v0.1.31.zip: stvarna 512×512 aplikacijska ikona, promotivna grafika 1024×500 i 4 stvarne Android emulator snimke, izrezane s 1080×2400 na 1080×1920. Snimka 03 prikazuje mirujuću igru prije prvog zamaha, ne aktivan gameplay.
- GitHub Actions izrađuje **nepotpisani** AAB za preuzimanje. Potpisivanje putem GitHuba je isključeno; izdavač privatno potpisuje paket izvan GitHuba tek prije slanja na Google Play.
- Hrvatski opis trgovine, politika privatnosti, Data safety analiza i kontrolni popis.

**Izdanje nije automatska objava na Google Playu.** Prije objave potrebni su račun izdavača, upload ključ, Play App Signing, sadržajni obrasci, testeri, provjera fizičkih uređaja i odobrenje trgovine.

## 2. Play Console — kreiranje aplikacije

1. Otvori https://play.google.com/console i potvrdi da je izdavački račun verificiran. Ako je izdavač organizacija, provjeri korisnička prava i odgovornu osobu.
2. Klikni Create app / Izradi aplikaciju. Naziv: **BOPAVI**, zadani jezik: hrvatski (hr-HR), vrsta: **Game / Igra**. Status besplatno/plaćeno izdavač mora potvrditi prije prvog izdanja. Trenutačni kod nema reklama ni kupnji stvarnim novcem.
3. AAB identifikator trajno je **com.brendigo.bopavi**. Provjeri da nije zauzet ili već objavljen pod drugim računom. Nakon objave ne mijenjaj identifikator.
4. Od 31. kolovoza 2026. Google Play za nove standardne Android aplikacije i ažuriranja traži target API 36 ili više. BOPAVI v0.1.29 je API 35 i **ne smije** biti Play kandidat. Službeno: https://support.google.com/googleplay/android-developer/answer/11926878
5. Android API36 utječe na edge-to-edge, velike zaslone i predictive Back; testirati ponašanje na fizičkim uređajima: https://developer.android.com/about/versions/16/behavior-changes-16

## 3. Upload key i Play App Signing

Google Play koristi **dva različita ključa**: upload key (čuva izdavač i njime potpisuje AAB) te app-signing key (Google Play za isporuku korisnicima). Preporučuje se Google-generated app-signing key i odvojeni RSA upload key. Službena dokumentacija: https://support.google.com/googleplay/android-developer/answer/9842756

Na **vlastitom sigurnom računalu**, jednom, pokreni:

    keytool -genkeypair -v -keystore bopavi-play-upload.jks -alias bopavi-upload -keyalg RSA -keysize 4096 -validity 10000

Čuvaj datoteku, lozinke i sigurnosnu kopiju u privatnom spremištu izvan GitHub repozitorija. Nikada ih ne šalji u chat, issue, PR, commit, screenshot ili GitHub Release. Potpisani bundle bez trajnog upload ključa nije spreman za dugoročno održavanje aplikacije.

## 4. Nepotpisani AAB i privatno potpisivanje

1. GitHub Actions izrađuje `app-release.aab` bez upload ključa. GitHub signing test i ručni signing job namjerno su onemogućeni prema odluci izdavača.
2. Preuzmi nepotpisani AAB iz provjerenog GitHub Actions artefakta ili iz javnog GitHub izdanja. Provjeri izvornu verziju, SHA-256, package name i neiskorišteni `versionCode`.
3. Ako izdavač kasnije želi objaviti igru u Play Consoleu, potpisivanje izvodi **lokalno i privatno**, koristeći postojeći, odobreni upload ključ, bez slanja ključa, JKS datoteka ili lozinki na GitHub.
4. Potpis i SHA-256 provjeri lokalnim alatima prije predaje u Play Console. Nepotpisani `app-release.aab` **nije** prihvatljiv za Play upload.
5. U slučaju prethodno iskorištenog `versionCode`, pripremi novo pravo izdanje. Nemoj prepisivati tag ili mijenjati upload identitet bez potvrde u Play Consoleu.

## 5. Store listing i marketinški materijali

1. Preuzmi iz GitHub Releasea ZIP **BOPAVI-Google-Play-listing-v0.1.31.zip** i raspakiraj ga.
2. U Play Console idi na **Grow users → Store presence → Main store listing**. Unesi hrvatski opis iz dokumenta **docs/play/STORE-LISTING-hr-HR.md**.
3. Prenesi store icon 512×512 RGB i feature graphic 1024×500 RGB. Slike su izvedene iz postojećih izvornika aplikacije; nema izmišljenih screenshotova.
4. Prenesi valjane **stvarne** Android snimke iz ZIP-a. Provjeri da 9:16 izrez ne skriva gumb, da nema sistemskih dijaloga, pogrešnih imena ni obavijesti. Za kvalitetnije predstavljene igre preporučuju se najmanje **3 stvarna screenshota aktivnog gameplaya** veličine 1080×1920; postojeći ZIP je početni skup, a prikaz mirujućeg leta **nije** stvarni aktivni gameplay.
5. Dodatne snimke iz aktivne igre pribavi sa stvarnog Android telefona ili legitimnog Android emulatora s **istom verzijom** i bez umjetničkog precrtavanja sučelja.
6. Pravila o 1024×500 grafici, 512×512 ikoni i snimkama: https://support.google.com/googleplay/android-developer/answer/9866151

## 6. Javna politika privatnosti i obrasci

1. Pripremljena dvojezična javna stranica je **docs/play/privacy-policy.html**. Mora biti dostupna preko stvarnog, javnog HTTPS URL-a bez prijave. Primjer konfiguracije: GitHub repo **Settings → Pages → Deploy from a branch → main /docs**, pa provjeri je li URL **https://bren-wp.github.io/BOPAVI/play/privacy-policy.html** stvarno aktivan. Adresa je samo očekivana nakon aktivacije, ne potvrda da je već objavljena. Može se postaviti i na provjerenu vlastitu domenu.
2. U **App content** popuni Privacy policy URL, Data safety, Ads, App access, Content rating, Target audience & content i sva dodatna pitanja koje konzola prikaže. Izdavač mora potvrditi dobne skupine; ako se cilja djecu, potrebno je provjeriti posebna Families pravila.
3. Trenutačno BOPAVI nema INTERNET dozvolu, analitiku, račune, oglase ni prave kupnje. Nadimak igrača, napredak, kovanice i postavke čuvaju se lokalno. Ručni JSON export koristeći odabrani pružatelj dokumenata ne znači da BOPAVI šalje podatke na svoj poslužitelj. Detaljno: **docs/play/GOOGLE-PLAY-DATA-SAFETY.md**.
4. **Čak i aplikacije bez prikupljanja podataka moraju imati javni privacy policy i popuniti Data safety obrazac.** Potvrdi tvrdnje s konačnim potpisanim bundleom: https://support.google.com/googleplay/android-developer/answer/10787469

## 7. Interni test, zatvoreni test i produkcija

1. Prenesi signed AAB u **Internal testing**; aktiviraj Play App Signing, po potrebi odaberi Googleov app-signing ključ. Testiraj baš Playom isporučenu verziju.
2. Fizički QA: Android 8/12/13/15/16; 60/90/120 Hz, male i velike zaslone, izreze, edge-to-edge, novi Back, odabir svih 9 likova, svih 8 svjetova, neprekinuti level, zvuk, prekide, pozadinu, nagrade, magnet, štit, JSON import/export i offline rad. Provjeri crash/ANR i Play Pre-launch report.
3. **Ako je primjenjiv novi osobni račun kreiran nakon 13.11.2023.**, potreban je Closed testing s najmanje **12 kontinuirano prijavljenih testera tijekom 14 dana** prije zahtjeva za pristup produkciji. Ne može se zamijeniti GitHub CI testovima. https://support.google.com/googleplay/android-developer/answer/14151465
4. Riješi sva blokirajuća upozorenja i dopuni podatke, tek tada odaberi **Production → Create new release → Review release**. Po potrebi postepena distribucija.
5. Pričekaj Play odobrenje, a zatim ručno potvrdi javnu stranicu, instalaciju i rad paketa s Google Play. GitHub Release **nije** dokaz objave u trgovini.

## 8. Kvarovi — brza dijagnostika

| Poruka / stanje | Postupak |
|---|---|
| Unsigned app | Pogrešan AAB. Izradi i prenesi artifact iz ručnog signing workflowa. |
| Wrong signing key | Provjeri izvorni .jks i Play App Signing upload certifikat; ne izmišljaj drugi ključ. |
| versionCode already used | U novom izdanju povećaj versionCode; broj 34 mora biti slobodan i potvrđen u Play Consoleu. |
| Target SDK | Provjeri da je cilj 36 i uploadana v0.1.31, ne stara v0.1.29. |
| Privacy Policy URL invalid | Stranica mora biti anonimno dostupna HTTPS lokaciji. |
| Images rejected | Provjeri 512×512, 1024×500, minimalno 2 stvarna screenshota i najveći dopušteni omjer. |
| Closed testing gate | Dovrši 12/14-dnevni test ako pravilo vrijedi za račun. |
| Signing job failure | Provjeri GitHub Environment, četiri tajne, alias i base64; nikad ne ispisuj njihovu vrijednost. |

## 9. Završni kontrolni popis

Vidi **docs/play/RELEASE-CHECKLIST.md**. Ne označavati aplikaciju kao odobrenu ili objavljenu bez dokaza iz stvarnog Play Console računa.
