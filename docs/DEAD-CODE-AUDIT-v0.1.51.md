# BOPAVI — audit izvornog koda (v0.1.51, build 54)

## Obuhvat i metoda

Pregledano je 12 ručno održavanih datoteka Androida i iOS-a: 6 Kotlin i 6 Swift datoteka. Pregled uključuje simulaciju leta, kolizije, renderiranje, proceduralne levele, navigaciju, spremanje stanja i zvuk. Skripta `tools/audit_native_dead_code.py` automatizirano provjerava lokalne reference privatnih funkcija i svojstava te Kotlin importe; koristi se u obveznom GitHub Actions auditu.

Statička provjera broja referenci samo je **konzervativni signal**. Metode koje poziva operacijski sustav, deklaracije učitane dinamički, javni API, trajna pohrana te resursi dostupni po imenu zahtijevaju ručni pregled i ne smiju se automatski brisati.

## Provjereni nalazi i izmjene

- **Android MainActivity**: uklonjen nekorišteni privatni `sunset` konstantni ton, bez ikakvog utjecaja na vidljive boje.
- **Android GameView**: uklonjeni nepotrebni `kotlin.math.abs` i `kotlin.math.max` importi.
- **Android MainActivity**: uklonjen nekorišteni `android.view.ViewGroup` import.
- **Android GameView i iOS GameCanvas**: uklonjen suvišan završni neprozirni pravokutnik koji je prekrivao donjih 49 logičkih točaka ilustriranog svijeta. Očuvane su postojeće bitmap pozadine, frame scheduler i izračun fizike.
- **README / CI**: uvedena je galerija istinskih emulator/simulator snimki kao GitHub Release resursa. CI odbija nedostajuće ili nevaljane PNG snimke, bez zamjene konceptnim ilustracijama.

## Namjerno očuvan kod

- Fallback grafički prikaz kada izvorni sprite nije dostupan, umjesto rušenja.
- Android `onTouchEvent` i `performClick`; iOS `accessibilityActivate`, `frameTick` te lifecycle metode koje poziva sustav.
- Podaci lokalnog napretka, migracije pohrane, identifikatori svih devet likova, osam svjetova i proceduralni generator.
- Postojeća kontrola potrošnje resursa: bez kontinuiranog renderiranja dok se čeka prvi zamah, keširane slike, odvojena animacija krila i ograničeno trajanje vizualnih čestica.

## Pravila prihvaćanja

Obvezne provjere: Python native source audit, Kotlin lint/JVM, Android 15/16 emulator, Swift fizika i save, iOS simulator / nepotpisani device build, generiranje identičnih umjetničkih resursa, provjera stvarnih snimki, distribucijski artefakti i SHA-256.

**Ograničenje:** ovaj audit ne predstavlja formalni dokaz odsutnosti svakog nedostižnog puta, race condition problema, curenja memorije, greške dostupne samo na fizičkom uređaju ili potpune grafičke podudarnosti 1:1. Sljedeće iteracije trebaju posebno profiliranje na stvarnim Android/iPhone uređajima i stvarne slikovne usporedbe referenci.
