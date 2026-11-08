# Next Android release

## v1.0.68 — Production text readability and accessible UI (build 198)

- Audited original 941×1672 native Android screenshots from v1.0.65 and observed specific production blockers: Settings row labels break mid-word, Achievements progress labels collide with the bottom of their cards, and Leaderboard rows/rank labels clip.
- Settings: native one-line adaptive labels, smaller decorative icon tiles, better label-to-control allocation, and three real difficulty controls in a 162dp group (at least 50dp per button after margins). The displayed setting is called Mode for legibility; saved difficulty values and TalkBack semantics remain unchanged.
- Achievements: fixed-size icon/header within each real goal card, reduced padding and progress bar height to allocate genuine vertical room for goal text, counts and status. No goal is invented or reset.
- Leaderboard: native one-line adaptive labels, smaller rank badges within the native score rows and a narrower score column to preserve player/world names. Actual local scores remain unchanged.
- The existing Android 15/16 UI flow, Settings legal links, touch target, image verification and privacy/security audits remain mandatory release gates. The prior CI run blocked the candidate before publication; the production gate must not be bypassed.
- Visual identity improves readability but absolute pixel-to-pixel equality is NOT claimed. A new 941×1672 Android screenshot is required to verify changes. Google Play upload requires signing the unsigned release AAB with publisher-held private material.
