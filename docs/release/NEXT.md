# Next Android release

## v1.0.67 — Skins / Game Over / Pause / Level Complete (build 197)

- Skins: native noninteractive wood-and-rope sign with real selected-pilot name, green leaves and stone/grass glowing platform. The selected, independently clickable pilot cards now have a more pronounced gold/blue 3D enamel appearance. All three playable pilots remain available, saved, and genuinely selectable.
- Skins: no fictitious fourth pilot, locked product, fake wallet, or screenshot-shaped action; the fourth illustrative reference card cannot be introduced without an actual new gameplay pilot.
- Game Over: three native data rows use cream beveled enamel within the existing wood frame; distance, food/coin, score/stars remain actual recorded run values. Retry/Home and the seated classic aviator remain unchanged.
- Pause: gold-rimmed blue dusk panel and avatar flight halo; consistent native high-contrast golden primary and blue secondary actions. Device-dependent crown emoji removed from the heading. Existing continue, replay of finite/endless mode, and exit still work as before.
- Level Complete: additional raised reward-board frame, Home-style white wings behind the main action, and native vector play/home icons; stars, coins, rating, chest receipt, next route, Home and Share remain real and unchanged.
- CI regression: first-use Android 15/16 immersive education may appear above Splash/Home after the full-bleed update. The Android smoke loop now dismisses only this verified operating-system tutorial BEFORE checking Home controls, and still requires true native accessible actions.
- Baseline measured from v1.0.65 native 941x1672 PNGs: Skins RGB MAE deviation 28.72%; Game Over 26.52%. These are baseline values, not claims of new pixel-perfect acceptance.
- Pixel-perfect 0% is a strict acceptance GOAL. No screen can be certified at 0% without a native build screenshot with zero changed RGB pixels against its corresponding owner reference and successful interactive flows.
- Release APK and AAB remain unsigned and require private Play upload signing, debug APK only for local QA; no keystores or secrets are added to repository.

## Further native screen alignment

- Skins: larger full 630×630 winged hero positioned above the illustrated stone plinth, plus two accessible round native previous/next arrows cycling only the three real playable pilots.
- Skins: real green EQUIP control adjacent to the selected hero's description, bound to persisted pilot selection instead of an inert mock purchase.
- Game Over: seated aviator occupies the larger 560×497 center composition; dynamic stats and retry/home routes remain unchanged.
- Pause: a single dialog click commit prevents duplicate continue/replay/exit navigation on rapid taps, including finite-level retry.
- Level Complete: winner is now the genuine winged 512×512 character selected by the player, rather than a static cropped portrait; star count, chest receipt and NEXT/HOME/SHARE actions are unchanged.
- Zero RGB deviation is still unverified; production builds must pass real Android capture and native interaction QA.
