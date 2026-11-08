# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.69 — Splash i Home: bolji izvorni kadar i 3D grafika (build 199)

**Published:** 2026-11-08 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.69)

- Splash: izvorni krilati BOPAVI logotip sada se prikazuje u okviru 792 × 330, na koordinatama (75,76) originalne reference 941 × 1672. Točno odgovara prirodnom omjeru izvora 780 × 325, umjesto vertikalnog izobličenja.
- Home: logotip povećan i podignut u okvir 798 × 351 na (72,72). Kontrola preklapanja uzima u obzir stvarnu alfa-siluetu, a ne prozirne rubove PNG-a.
- Home: uz standardni gornji HUD logotip se više ne smanjuje bez potrebe. Ponašanje na telefonima s izrezom kamere ostaje adaptivno.
- Home: broj stvarno osvojenih zvjezdica prikazuje zlatnu ikonu zvjezdice; ne prikazuje nepostojeću valutu dragulja ni lažne brojke s reference.
- Splash i Home: usklađeniji crveni šal (bez trokutastog klina preko prsa), približena druga naočala i zadržana animirana prava krila glavnog lika. Gameplay i fizički hitboxovi nisu mijenjani.
- Splash: referentni QA Loading sada koristi immersive prikaz bez donje Android navigacijske crte. Dostupnost sistemskih kontrola gestom je sačuvana.
- Oba zaslona: dopunjena su lokalno generirana pozadinska obilježja visokog otočnog dvorca i metalnog zračnog broda. Nema novog preuzimanja iz mreže ni lažnih kontrola.
- Izmjene su ograničene na Splash/Home, offline izradu zajedničke pozadine i njihove regresijske testove. Sve akcije ostaju stvarni, dostupni Android gumbi; Loading ne prikazuje izmišljeni postotak.
- Referenca: 941 × 1672; posljednja prethodno verificirana v1.0.68 odstupanja bila su Home 24,02% i Splash 24,66% RGB MAE. To je početna mjera, a NE novo mjerenje niti 0% podudarnosti.
- Produkcijska objava dopuštena je tek nakon uspješnog Android CI-a, stvarnih snimki i testiranja na Androidu 15/16. Play upload AAB zahtijeva potpis izdavatelja.

## v1.0.68 — Production text readability and accessible UI (build 198)

**Published:** 2026-11-08 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.68)

- Audited original 941×1672 native Android screenshots from v1.0.65 and observed specific production blockers: Settings row labels break mid-word, Achievements progress labels collide with the bottom of their cards, and Leaderboard rows/rank labels clip.
- Settings: native one-line adaptive labels, smaller decorative icon tiles, better label-to-control allocation, and three real difficulty controls in a 162dp group (at least 50dp per button after margins). The displayed setting is called Mode for legibility; saved difficulty values and TalkBack semantics remain unchanged.
- Achievements: fixed-size icon/header within each real goal card, reduced padding and progress bar height to allocate genuine vertical room for goal text, counts and status. No goal is invented or reset.
- Leaderboard: native one-line adaptive labels, smaller rank badges within the native score rows and a narrower score column to preserve player/world names. Actual local scores remain unchanged.
- The existing Android 15/16 UI flow, Settings legal links, touch target, image verification and privacy/security audits remain mandatory release gates. The prior CI run blocked the candidate before publication; the production gate must not be bypassed.
- Visual identity improves readability but absolute pixel-to-pixel equality is NOT claimed. A new 941×1672 Android screenshot is required to verify changes. Google Play upload requires signing the unsigned release AAB with publisher-held private material.
