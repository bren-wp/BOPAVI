# Next Android release

## v1.0.61 — Pending (build 191)

### Gameplay and three pilots
- First-world pillars now use carved warm sandstone, bevelled golden crown plaques, red crown banners, real anchored climbing leaves and daisies.
- Alternating metal cone caps end at the existing collision-solid edge: no visually dangerous, non-collidable protrusions and no change to the 18-unit physical Bopi/Sova/Lisac collider.
- In the first world, six curved gold rings per eligible pillar are now individually collectible in both finite and endless flight. The renderer and swept contact use the same world/viewport coordinates, and ring flags prevent duplicate rewards.
- Real finite central coin pickups remain unchanged. Additional royal rings increment true collected coins and live score rather than displaying fake, uncollectible bonus items.
- The first royal pillar enters the screen after the first genuine flap; sky-world floor spikes are removed to match the supplied pillar-based gameplay references (the separate forest biome retains its original ground decoration).
- Sky Meadows draws from the locally generated castle/waterfall background without baked gameplay controls or pilot artwork.
- During the game, Android navigation bars hide for a genuinely full-bleed viewport and return outside gameplay. Live lives are still available to accessibility even when the visual HUD uses the reference SCORE plaque.
- Active flight displays a live native SCORE plaque, actual offline saved coin balance, real collected crystals (not a purchasable gem currency) and a working on-screen Pause action. Both + controls request the real offline shop with an explicit leave-flight confirmation.
- Idle flight still hides obstacles until the actual first flap; the ready/tutorial state remains unchanged.
- Bopi, Sova and Lisac preserve independent animated wing layers, their original IDs and saved pilot selection; Sova and Lisac use a subtle forward flight perspective on their existing shaded 3D sprites.
- All active HUD controls are scaled through native safe insets across compact and tall Android layouts.
- Updated automated Kotlin/HUD geometry checks and Python source contracts; true 0% pixel difference must be assessed against a matching original reference plus actual device/emulator captures, not claimed from compilation.

### Distribution
- Publish fresh unsigned Android APK/AAB alongside a QA debug APK only after the full native release CI validates.
- Update the README gallery from real Android emulator screenshots; do not replace screenshots with fabricated renders.
