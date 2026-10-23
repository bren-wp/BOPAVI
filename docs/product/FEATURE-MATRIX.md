# BOPAVI — Current Android-only feature matrix

This file defines **actually reachable Android UI**, not features implied by
unused graphics, dormant save keys or legacy Kotlin paths. The JSON payload
is the source for the automated public-claims gate.

## Machine-readable release evidence

~~~json
{
  "schema": 1,
  "baseline_sha": "c6eb1c18e8548cfdb5bb480296b33f1239a0ea27",
  "release": "v1.0.23",
  "publication_status": "published",
  "last_published_github_release": "v1.0.23",
  "build": 153,
  "scope_decision": "pending",
  "screens": [
    "home",
    "flight",
    "pause",
    "results"
  ],
  "features": [
    {
      "id": "home",
      "status": "visible"
    },
    {
      "id": "flight",
      "status": "visible"
    },
    {
      "id": "tap_to_flap",
      "status": "visible"
    },
    {
      "id": "procedural_obstacles",
      "status": "visible"
    },
    {
      "id": "pause",
      "status": "visible"
    },
    {
      "id": "results",
      "status": "visible"
    },
    {
      "id": "score",
      "status": "visible"
    },
    {
      "id": "berries",
      "status": "visible"
    },
    {
      "id": "local_best",
      "status": "visible"
    },
    {
      "id": "retry",
      "status": "visible"
    },
    {
      "id": "offline",
      "status": "visible"
    },
    {
      "id": "no_ads",
      "status": "visible"
    },
    {
      "id": "no_account",
      "status": "visible"
    },
    {
      "id": "inline_pilot_selection",
      "status": "visible"
    },
    {
      "id": "quick_options_overlay",
      "status": "visible"
    },
    {
      "id": "local_records_overlay",
      "status": "visible"
    },
    {
      "id": "legacy_save_models",
      "status": "internal-legacy"
    },
    {
      "id": "skins_assets_nine",
      "status": "internal-legacy"
    },
    {
      "id": "world_assets_eight",
      "status": "internal-legacy"
    },
    {
      "id": "json_save_serializer",
      "status": "internal-legacy"
    },
    {
      "id": "virtual_wallet_save",
      "status": "internal-legacy"
    },
    {
      "id": "multiworld_premium_product",
      "status": "planned"
    },
    {
      "id": "skin_selection_screen",
      "status": "removed"
    },
    {
      "id": "world_selection_screen",
      "status": "removed"
    },
    {
      "id": "level_selection_screen",
      "status": "removed"
    },
    {
      "id": "shop_screen",
      "status": "removed"
    },
    {
      "id": "profile_screen",
      "status": "removed"
    },
    {
      "id": "settings_screen",
      "status": "removed"
    },
    {
      "id": "achievements_screen",
      "status": "removed"
    },
    {
      "id": "records_screen",
      "status": "removed"
    },
    {
      "id": "import_export_ui",
      "status": "removed"
    },
    {
      "id": "powerup_shop",
      "status": "removed"
    }
  ]
}
~~~

## Verified Android screen routes

| Live state | Android implementation | What players can do |
|---|---|---|
| Home | MainActivity.showHome | Choose Bopi, Sova or Lisac, open native quick-options or read-only local-records dialogs, read both offline legal links and start an endless flight |
| Flight | MainActivity.startGame, GameView, GameSimulation | Tap to flap, pass pillars and collect berries |
| Pause | MainActivity.showPremiumPause | Resume, restart or exit |
| Results | MainActivity.showResult, ProgressStore.recordRun | Read score/berries/best and retry or return Home |

The original approved Bopi art, level engine and eight generated biome asset
sources remain preserved. Sova and Lisac are new selectable animal companions
on Home, while only the first biome is presently accessible. Other stored fields are retained for backward compatibility;
they are not advertised as player-accessible options.

## Release and QA limitations

- Public release details come from CHANGELOG.md, not an unmerged PR.
- **Published baseline:** v1.0.6 (build 136). The v1.0.7 candidate is recorded only in Gradle and docs/release/NEXT.md until GitHub release verification. Do not pre-announce unreleased features as shipped.
- Android API 35/36 emulator screenshots are useful evidence, not guarantees of
  pixel-perfect reference matching, physical-device stability or Play approval.
- Product-scope decision ADR-0001 is still pending; do not restore removed UI.
