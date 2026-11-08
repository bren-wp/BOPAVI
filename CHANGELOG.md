# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.68 — Production text readability and accessible UI (build 198)

**Published:** 2026-11-08 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.68)

- Audited original 941×1672 native Android screenshots from v1.0.65 and observed specific production blockers: Settings row labels break mid-word, Achievements progress labels collide with the bottom of their cards, and Leaderboard rows/rank labels clip.
- Settings: native one-line adaptive labels, smaller decorative icon tiles, better label-to-control allocation, and three real difficulty controls in a 162dp group (at least 50dp per button after margins). The displayed setting is called Mode for legibility; saved difficulty values and TalkBack semantics remain unchanged.
- Achievements: fixed-size icon/header within each real goal card, reduced padding and progress bar height to allocate genuine vertical room for goal text, counts and status. No goal is invented or reset.
- Leaderboard: native one-line adaptive labels, smaller rank badges within the native score rows and a narrower score column to preserve player/world names. Actual local scores remain unchanged.
- The existing Android 15/16 UI flow, Settings legal links, touch target, image verification and privacy/security audits remain mandatory release gates. The prior CI run blocked the candidate before publication; the production gate must not be bypassed.
- Visual identity improves readability but absolute pixel-to-pixel equality is NOT claimed. A new 941×1672 Android screenshot is required to verify changes. Google Play upload requires signing the unsigned release AAB with publisher-held private material.

## v1.0.65 — Shop i Game Over: prepoznatljivi 3D elementi (build 195)

**Published:** 2026-11-08 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.65)

- Shop: sve postojeće stvarne nadogradnje štita i magneta sada imaju različite slojevite 3D ilustracije za osnovnu, drugu i treću razinu proizvoda, umjesto ponavljanja identične ikonice.
- Shop: dodan zaseban dekorativni drveni rub pulta ispred Bopija, bez presretanja dodira ili utjecaja na kupnje.
- Game Over: novi drveno-kremasti okvir za tri retka rezultata; stvarne vrijednosti ostaju pristupačni Android tekstualni elementi.
- Game Over: osvojenim zvjezdicama pridružena je ispravna zlatna ikona zvjezdice, a ne ikona dragulja.
- Zadržane su postojeće cijene, inventar, šest stvarnih offline kupnji, RETRY/HOME navigacija i spremanje napretka bez migracije.
- Dodane su statičke provjere novih umjetničkih slojeva i razlike između stvarnih zvjezdica i neimplementiranog novčanika dragulja.
- Referentnih 941 × 1672 piksela cilj je usporedbe, ne potvrda postignutih 0 % odstupanja. Nedostaju proizvodi Extra Hearts, Coin Booster, Gem Pack i Random Skin, a Golden Avi nije zasebni kupivi skin. Njih se ne smije predstavljati kao stvarno dostupne kupnje dok ne postoje igraća logika i pohrana.
- Produkcijska objava dopuštena je samo nakon uspješnih JVM i Android CI testova te snimanja stvarnih APK ekrana. Izdani release APK i AAB nepotpisani su, a debug APK namijenjen je testiranju.
