# Next Android release

## v1.0.70 — Gameplay bez donjih šiljaka i sigurnije pokretanje (build 200)

- **Gameplay:** uklonjena je dekoracija od pet zlatnih donjih šiljaka u prvom i sedmom svijetu, uključujući početni kadar prije prvog zamaha. Uklonjeni su neiskorišteni renderer i geometrija te obnovljeni regresijski testovi. Stvarni stupovi, skupljanje predmeta, viseće prepreke, radijus kolizije i granica tla ostaju nepromijenjeni.
- **Sigurnost:** razvojni QA parametri za Splash, prvi zamah i prikaz kolizija sada se prihvaćaju samo u debug izdanju. Izdana aplikacija ignorira takve vanjski zadane parametre.
- **Postavke:** smanjeni su preveliki nazivi redova, a odabir težine koristi kratke čitljive oznake EASY / NORMAL / HARD uz puni izvorni naziv u opisima pristupačnosti i iste spremljene vrijednosti.
- **QA:** novi regresijski testovi provjeravaju zabranu donjih šiljaka i produkcijsku zaštitu QA parametara. Analiza osam izvornih Android snimki od 8. 11. 2026. utvrdila je odstupanja između 23,50% i 29,03% RGB-MAE u odnosu na reference vlasnika. Izvještaj je u `docs/qa/OWNER-RGB-COMPARISON-2026-11-09.md`.
- **Transparentnost:** potpuno pixel-perfect 1:1 podudaranje, fizički uređaji, puna penetracijska provjera i odobrenje Google Playa nisu potvrđeni. Nedostaje vjerodostojna snimka zaslona Level Complete za završno numeričko mjerenje.
- **Distribucija:** Android CI proizvodi stvarne APK i AAB artefakte; release APK i Google Play AAB ostaju **nepotpisani**. Potpis izdavateljevom postojećom privatnom upload ključem potreban je prije Google Play predaje.
