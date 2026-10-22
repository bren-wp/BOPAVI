# Next Android release

## v1.0.17 — Accessible premium buttons and stable flight announcements (build 147)

- **Readable primary actions:** use deep-blue text, not white, on the existing gold Home/Results/Pause gradient buttons. Each endpoint of the unchanged gold gradients now exceeds the WCAG AA 4.5:1 normal-text contrast threshold. Keep native ripples, touch targets and illustration assets intact.
- **Clearer spoken flight UI:** include the actual selected pilot, flap gesture, score, collected food and lives in the native TalkBack label. Each tap reuses the most recent HUD accessibility label instead of overwriting it with a bare pilot name.
- **Efficient updates:** compose the full HUD description only when a score, berry count or life count changes. The fast render loop still reuses the cached label; the first active tap gets a short localized fallback hint.
- **Regression protection:** check real primary-button foreground and gradient-endpoint contrast mathematically in a new CI source contract; verify HUD announcements keep control instructions across taps.
- **Safety:** physics, collision geometry, level streaming, scoring, persistent data, audio policy, pilot graphics and offline privacy behavior remain unchanged. Real-world TalkBack usability and actual battery savings still require device verification.
