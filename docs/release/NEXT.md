# Next Android release

## v1.0.77 — Settings 1:1 native controls, backup and notifications (build 207)

**Status:** Draft PR #228; in QA. Not published or verified as pixel-identical.

- **Settings reference:** nine real owner-ordered controls: Music, Sound Effects, Notifications, Language, Controls, Vibration, Cloud Save, Privacy Policy, Terms of Service, plus the bottom Restore Purchases button. The large screen uses the reference's 941×1672 geometry, while compact/large-font layouts scroll natively. Existing Sound, Reduced Motion and Difficulty remain available on the scrolling layout and through Controls.
- **Notifications:** optional daily local 18:00 reminder with an Android 13+ runtime permission, safely off by default, without a push service.
- **Language:** locally stored English/Croatian setting choice for the Settings interface. This does **not** imply all gameplay copy is translated.
- **Cloud Save:** optional Android Storage Access Framework (SAF) backup to a provider/document the player explicitly selects (including locally stored or third-party cloud documents); automatic saves while the app is active, and validation of imported save data. No Brendigo account, backend or hidden sync service.
- **Restore Purchases:** explicit confirmation and restore of locally earned/owned items from a compatible BOPAVI JSON backup. This is **not** Google Play Billing or in-app purchase restoration; no real-money products are claimed.
- **Privacy:** bundled/offline and public legal documents updated to describe the opt-in storage-provider access, included save contents and optional reminders.
- **Shop and Skins:** native offline purchase and character-selection behavior retained; decorative assets still need significant owner screenshot alignment.
- **QA acceptance:** static source, legal, security, Android lint, JVM, unsigned APK/AAB, native Android 15/16 smoke, font-scale and screen-size checks. Strictly compare genuine 941×1672 screenshots against the original owner images per region.
- **Visual honesty:** 0% RGB deviation is not claimed. The last proven Android16 baseline before this version was Shop 23.3593% and Skins 32.1376% normalized RGB MAE. The Settings metrics must be measured from the new functional nine-row layout.
- **Distribution:** debug APK for QA. Release APK and Google Play AAB are unsigned until publisher signing, and must not be promoted to stable distribution before the checks pass.
