#!/usr/bin/env python3
"""Update only latest two actually published stable BOPAVI releases."""
import argparse
import json
import re
from pathlib import Path
from release_metadata import release_metadata
STABLE=re.compile(r"v([1-9]\d*)\.(\d+)\.(\d+)")
HEADER=re.compile(r"(?m)^## v\d+\.\d+\.\d+\s+[—-]\s+[^\n]+$")
ACTIVE=("docs/product/CURRENT_SCREENS.md","docs/play/STORE-LISTING-hr-HR.md","docs/play/RELEASE-CHECKLIST.md")
def decode_pages(raw):
    result=[];decoder=json.JSONDecoder();pos=0
    while pos<len(raw):
        while pos<len(raw) and raw[pos].isspace():pos+=1
        if pos==len(raw):break
        page,pos=decoder.raw_decode(raw,pos)
        if not isinstance(page,list):raise ValueError("Release API must return arrays")
        result.extend(page)
    return result
def releases(items):
    kept=[];tags=set()
    for r in items:
        tag=str(r.get("tag_name",""))
        if not STABLE.fullmatch(tag) or r.get("draft") or r.get("prerelease") or not r.get("published_at"):continue
        if tag in tags:raise ValueError("Duplicate stable release: "+tag)
        tags.add(tag);kept.append(r)
    return sorted(kept,key=lambda r:tuple(map(int,STABLE.fullmatch(r["tag_name"]).groups())),reverse=True)
def section(r):
    tag=r["tag_name"];body=str(r.get("body") or "")
    header=HEADER.search(body)
    if header is not None and header.group().startswith("## "+tag+" ") and "(build " in header.group():
        # Current releases require their explicit versioned/build-labelled title.
        title=header.group().rstrip("\r")
    else:
        # v1.0.28 shipped a historical short heading ('## v1.0.28 ')
        # but retained the immutable, authoritative '**Build 158 distribution:**'
        # footer. Do not modify a published GitHub release merely to fix docs.
        # Allow this strictly scoped format ONLY if both its exact tag and
        # numeric published build footer are present. A malformed descriptive
        # header or missing build information must still fail closed.
        legacy=re.search(r"(?m)^## (v\d+\.\d+\.\d+)[ \t]*\r?$",body)
        footer=re.search(r"(?m)^\*\*Build ([1-9]\d*) distribution:\*\*",body)
        if legacy is None or legacy.group(1)!=tag or footer is None:
            raise ValueError("Invalid published release notes for "+tag)
        header=legacy
        title="## "+tag+" — Android (build "+footer.group(1)+")"
    notes=body[header.end():].split("**Build ",1)[0].splitlines()
    bullets=[line.strip() for line in notes if line.strip().startswith("- ")]
    if not bullets:raise ValueError("Release missing documented changes: "+tag)
    date=r["published_at"][:10]
    if not re.fullmatch(r"\d{4}-\d{2}-\d{2}",date):raise ValueError("Bad publication date")
    return title+"\n\n**Published:** "+date+" · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/"+tag+")\n\n"+"\n".join(bullets[:12])+"\n"
def render(items):
    if not items:raise ValueError("No stable release")
    intro="# BOPAVI — Published stable releases\n\nOnly two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).\nAll previous versions remain at https://github.com/bren-wp/BOPAVI/releases.\n\n"
    return intro+"\n".join(section(r) for r in items[:2])

def three_action_version(tag):
    """Apply only to versions explicitly built with the three-action Home."""
    match=STABLE.fullmatch(tag)
    return match is not None and (1,0,84)<=tuple(map(int,match.groups()))<(1,0,88)

def four_action_version(tag):
    """From v1.0.88, PLAY is an approved user-facing Home route."""
    match=STABLE.fullmatch(tag)
    return match is not None and tuple(map(int,match.groups()))>=(1,0,88)

def three_action_catalog(catalog):
    """Describe reachable production UI, not debug-only gameplay or old saves."""
    catalog["screens"]=["home","skins","shop","settings"]
    catalog["home_navigation_decision"]="approved-three-actions"
    states={
        "home":"visible",
        "flight":"internal-legacy",
        "tap_to_flap":"internal-legacy",
        "procedural_obstacles":"internal-legacy",
        "pause":"internal-legacy",
        "results":"internal-legacy",
        "score":"internal-legacy",
        "berries":"internal-legacy",
        "local_best":"internal-legacy",
        "retry":"internal-legacy",
        "offline":"visible",
        "no_ads":"visible",
        "no_account":"visible",
        "inline_pilot_selection":"removed",
        "quick_options_overlay":"removed",
        "local_records_overlay":"removed",
        "legacy_save_models":"internal-legacy",
        "virtual_wallet_save":"visible",
        "skin_selection_screen":"visible",
        "shop_screen":"visible",
        "settings_screen":"visible",
        "powerup_shop":"visible",
        "achievements_screen":"removed",
        "records_screen":"removed",
        "level_selection_screen":"removed",
        "world_selection_screen":"removed",
    }
    for feature in catalog["features"]:
        if feature["id"] in states:
            feature["status"]=states[feature["id"]]
    existing={f["id"] for f in catalog["features"]}
    for name,state in (("three_home_actions","visible"),
                       ("progress_screen","removed"),
                       ("leaderboard_screen","removed"),
                       ("level_map_screen","removed")):
        if name not in existing:
            catalog["features"].append({"id":name,"status":state})
    return catalog

def three_action_documents(tag,build,catalog):
    """Generate truthful copy only AFTER a successful public stable release."""
    matrix_json=json.dumps(catalog,ensure_ascii=False,indent=2)
    matrix=(
        "# BOPAVI — Current Android-only feature matrix\n\n"
        "This file describes **reachable production UI** in the published APK. "
        "Legacy game data is retained without restoring deleted navigation.\n\n"
        "## Published release evidence\n\n~~~json\n"+matrix_json+"\n~~~\n\n"
        "## Verified production screen routes\n\n"
        "| Screen | Entry point | Available operations |\n"
        "|---|---|---|\n"
        "| Home | MainActivity.showHome | Exactly Shop, Skins, Settings; coin balance is passive |\n"
        "| Skins | MainActivity.showSkins | Select and unlock pilots using local saved coins |\n"
        "| Shop | MainActivity.showShop | Local virtual upgrades, wallet and bonuses |\n"
        "| Settings | MainActivity.showSettingsScreen | Volume, controls, backup, reminders, language and legal notices |\n\n"
        "Progress, Leaderboard, Records, Achievements and Level Map screens "
        "and their entry points are removed. New gameplay cannot be started from Home. "
        "The existing GameSimulation and save keys remain for backward compatibility "
        "and debug-only regression tests; they are not advertised as playable UI.\n\n"
        "Real Android emulator screenshots do not by themselves certify pixel-perfect "
        "identity or physical-device stability. No third-party accounts, ads or Internet permission.\n"
    )
    screens=(
        "# BOPAVI — Current Android screens\n\n"
        "**Published release baseline:** "+tag+" (build "+str(build)+").\n\n"
        "The production Home exposes **exactly three accessible actions**: "
        "**Shop, Skins and Settings**. Coins are displayed as a passive balance. "
        "No Play, Sound shortcut, Achievements, Progress, Leaderboard, "
        "Records or Level Map action remains.\n\n"
        "| Accessible screen | Genuine screenshot | Operation |\n"
        "|---|---|---|\n"
        "| Home | BOPAVI-Android-Home.png | Shop, Skins, Settings |\n"
        "| Skins | BOPAVI-Android-Skins.png | Pilot selection and local unlocks |\n"
        "| Shop | BOPAVI-Android-Shop.png | Offline perk inventory and virtual coins |\n"
        "| Settings | BOPAVI-Android-Settings.png | Real settings, privacy, terms and SAF backup |\n"
        "| Privacy (Settings dialog) | BOPAVI-Android-Privacy.png | Bundled offline privacy notice |\n"
        "| Terms (Settings dialog) | BOPAVI-Android-Terms.png | Bundled offline service terms |\n\n"
        "**Compatibility only:** flight simulation, pause and results still exist "
        "internally and can be exercised by debuggable-only Android QA. "
        "There is intentionally no production Home button for starting a flight. "
        "Stored progress, virtual coins and purchased skins are preserved on update.\n\n"
        "Original same-build emulator captures: https://github.com/bren-wp/BOPAVI/releases/tag/"
        +tag+". Physical-device QA and exact 0% RGB parity are not claimed.\n"
    )
    listing=(
        "# Google Play — BOPAVI (hr-HR) · objavljeno "+tag+
        " (build "+str(build)+")\n\n"
        "<!-- verified-feature-ids: home,three_home_actions,skin_selection_screen,shop_screen,settings_screen,virtual_wallet_save,offline,no_ads,no_account -->\n\n"
        "**Naziv (do 30 znakova):** BOPAVI\n\n"
        "**Kratki opis (do 80 znakova):** Bopiju prilagodi likove, nadogradnje i postavke, bez oglasa i prijave.\n\n"
        "**Puni opis:**\n\n"
        "BOPAVI prikazuje tri glavna izbornika: Shop, Skins i Settings. "
        "U Skins odaberi dostupnog lika, u Shopu pogledaj poboljšanja "
        "koja se otključavaju već spremljenim virtualnim novčićima, "
        "a u Settingsu prilagodi zvuk, kontrole, jezik i sigurnosne kopije.\n\n"
        "Napredak i virtualna sredstva spremljeni su lokalno i ostaju očuvani "
        "nakon ažuriranja. Nisu potrebni registracija, oglasi ni internetska veza. "
        "U postavkama su dostupni uvjeti korištenja i politika privatnosti.\n\n"
        "Zasloni napretka, poretka, rekorda, postignuća i karte razina uklonjeni su. "
        "U ovoj verziji nema dostupnog gumba za pokretanje novog leta s početnog zaslona. "
        "Prijašnji rezultat i spremljene bobice nisu izbrisani.\n\n"
        "**Kategorija:** Igra → Arkadne (potvrditi u Play Consoleu).\n\n"
        "**Oglasi:** Ne. **Kupnje u aplikaciji:** Ne. "
        "Shop koristi isključivo lokalne virtualne novčiće.\n\n"
        "## Autentične slike — ugovor\n\n"
        "Obvezno snimiti pravi Home, Shop, Skins i Settings iz istoga buil-da. "
        "Dodatne gameplay/rezultat snimke služe tehničkoj transparentnosti, "
        "ali nisu dokaz produkcijske navigacije. Izostaviti stare "
        "Achievements, Progress, Leaderboard, Level-Map, Options i Records snimke.\n\n"
        "GitHub izdanje: https://github.com/bren-wp/BOPAVI/releases/tag/"+tag+".\n"
        "Provjera na fizičkom uređaju i Play Store odobrenje nisu potvrđeni.\n"
    )
    checklist=(
        "# BOPAVI — Android release acceptance checklist\n\n"
        "**Last published release evidence:** "+tag+" (build "+str(build)+").\n"
        "**Application:** com.brendigo.bopavi · BRENDIGO · https://brendigo.com.\n\n"
        "## Production UI\n\n"
        "- [ ] Home has exactly three accessible controls: Shop, Skins, Settings.\n"
        "- [ ] No links, buttons, gestures or standalone screens for Progress, "
        "Leaderboard, Records, Achievements, Level Map or Play.\n"
        "- [ ] Shop virtual purchases and Skins ownership stay transactional and offline.\n"
        "- [ ] Settings privacy, legal controls, SAF backup, audio and accessibility work.\n"
        "- [ ] Run Android 15/16 emulator UI and image checks at multiple densities.\n"
        "- [ ] Check physical Android devices and TalkBack before claiming production parity.\n\n"
        "## Data, security and distribution\n\n"
        "- [ ] Preserve historical v1–v6 local save keys and virtual balances.\n"
        "- [ ] No INTERNET permission, ad SDKs or new account requirement.\n"
        "- [ ] Confirm 11 original emulator PNG captures, no removed-screen assets.\n"
        "- [ ] Check Gradle Android Lint, JVM tests, R8 APK/AAB, SHA-256 manifests.\n"
        "- [ ] Sign production distribution privately with publisher-approved key.\n"
        "- [ ] Check Play Console policy, accessibility, data safety and physical-device stability.\n"
        "- [ ] Never rewrite or delete older stable GitHub releases.\n"
    )
    return {
        "docs/product/FEATURE-MATRIX.md":matrix,
        "docs/product/CURRENT_SCREENS.md":screens,
        "docs/play/STORE-LISTING-hr-HR.md":listing,
        "docs/play/RELEASE-CHECKLIST.md":checklist,
    }


def four_action_catalog(catalog):
    """Play is a real production action from v1.0.88 onward (ADR-0003)."""
    catalog=three_action_catalog(catalog)
    catalog["screens"]=["home","loading","flight","pause","results","skins","shop","settings"]
    catalog["home_navigation_decision"]="approved-four-actions-play"
    visible={"home","flight","tap_to_flap","procedural_obstacles","pause","results",
             "score","berries","local_best","retry","offline","no_ads","no_account",
             "virtual_wallet_save","skin_selection_screen","shop_screen",
             "settings_screen","powerup_shop","four_home_actions",
             "gameplay_start_from_home","flight_loading","game_over_screen"}
    for feature in catalog["features"]:
        if feature["id"] in visible:feature["status"]="visible"
        if feature["id"]=="three_home_actions":feature["status"]="removed"
    existing={feature["id"] for feature in catalog["features"]}
    for item in ("four_home_actions","gameplay_start_from_home","flight_loading","game_over_screen"):
        if item not in existing:
            catalog["features"].append({"id":item,"status":"visible"})
    return catalog

def four_action_documents(tag,build,catalog):
    """Current published docs: history lives in CHANGELOG, candidate in NEXT."""
    templates={
        "docs/product/FEATURE-MATRIX.md": "# BOPAVI — Current Android-only feature matrix\n\nThis file describes the reachable production UI of the **published** Android release @@TAG@@ (build @@BUILD@@), not an unreleased candidate. Historical three-action navigation ended with ADR-0003; retained save formats and intentionally removed standalone pages are distinguished below.\n\n## Published release evidence\n\n~~~json\n@@CATALOG@@\n~~~\n\n## Verified source-level production routes (device acceptance remains separate)\n\n| Screen/state | Entry point | Available operations |\n|---|---|---|\n| Home | MainActivity.showHome | Four accessible actions: PLAY, SKINS, SHOP, SETTINGS; passive coin balance |\n| Loading / Ready | MainActivity.startGame | Production PLAY calls the existing flight preparation pipeline |\n| Flight | GameView / GameSimulation | Tap-to-flap, obstacles, coins and gameplay score |\n| Pause / Game Over / Results | GameView and result handlers | Resume, Retry, Home, locally stored results |\n| Skins | MainActivity.showSkins | Select and unlock pilots using saved coins |\n| Shop | MainActivity.showShop | Six virtual upgrades, wallet and inventory |\n| Settings | MainActivity.showSettingsScreen | Audio, controls, backups, reminders, language and legal notices |\n\nProgress, Leaderboard, Records, Achievements and Level Map remain removed as standalone pages. Gameplay is **reachable from Home using PLAY**, not debug-only. Older local save data and historical JSON formats remain supported; their reliability needs independent testing.\n\n**Visual evidence is not pixel-perfect certification.** Ten distinct owner design states have 40 original WebP reference files across four available resolutions; the additional indexed resolution remains optional. Native emulator PNGs, complete RGB comparisons, physical-device SAF recovery, accessibility, FPS and battery results are separate acceptance gates.\n",
        "docs/product/CURRENT_SCREENS.md": "# BOPAVI — Current Android screens\n\n**Published release baseline:** @@TAG@@ (build @@BUILD@@). This corrects the post-publication documentation generator; it does not rewrite the original GitHub release.\n\nThe production Home has **four functional accessible actions**: **PLAY, SKINS, SHOP and SETTINGS**. PLAY starts the actual game through MainActivity.startGame(), Loading and GameView. The passive coin balance is not a fifth action.\n\n| Accessible screen or state | Genuine release screenshot / evidence | Operation |\n|---|---|---|\n| Home | BOPAVI-Android-Home.png | PLAY, SKINS, SHOP, SETTINGS |\n| Loading / Ready | BOPAVI-Android-Flight-Preview.png (ready state) | Transition from PLAY to playable flight |\n| Flight | BOPAVI-Android-Flight-Active.png | Tap-to-flap, obstacles, score and coins |\n| Pause | BOPAVI-Android-Pause.png | Resume and navigation |\n| Game Over | BOPAVI-Android-Game-Over.png | End-state feedback |\n| Results | BOPAVI-Android-Results.png | Saved results, Retry and Home |\n| Skins | BOPAVI-Android-Skins.png | Pilot selection and local unlocks |\n| Shop | BOPAVI-Android-Shop.png | Six offline upgrades and virtual coins |\n| Settings | BOPAVI-Android-Settings.png | Audio, accessibility, privacy, terms and SAF |\n| Privacy (Settings dialog) | BOPAVI-Android-Privacy.png | Bundled privacy notice |\n| Terms (Settings dialog) | BOPAVI-Android-Terms.png | Bundled terms of use |\n\nRemoved standalone pages: Progress, Leaderboard, Records, Achievements and Level Map. Their removal does not disable gameplay or delete compatible historical player data.\n\n**Reference design:** 10 distinct states; 40 genuine owner originals over four available resolutions. Screenshots are real release assets, but source checks and the existence of screenshots alone do **not** establish 1:1 RGB parity or real-device stability.\n\nPublished screenshots: https://github.com/bren-wp/BOPAVI/releases/tag/@@TAG@@. Unreleased changes are documented separately in `docs/release/NEXT.md`.\n",
        "docs/play/STORE-LISTING-hr-HR.md": "# Google Play — BOPAVI (hr-HR) · objavljeno @@TAG@@ (build @@BUILD@@)\n\n<!-- verified-feature-ids: home,four_home_actions,gameplay_start_from_home,flight,skin_selection_screen,shop_screen,settings_screen,virtual_wallet_save,offline,no_ads,no_account -->\n\n**Naziv (do 30 znakova):** BOPAVI\n\n**Kratki opis (do 80 znakova):** Bopi leti jednim dodirom; otključaj likove i nadogradnje bez oglasa.\n\n**Puni opis:**\n\nBOPAVI je offline arkadna igra u kojoj Bopi leti jednim dodirom. Na početnom zaslonu dostupne su četiri stvarne opcije: PLAY, SKINS, SHOP i SETTINGS. PLAY pokreće učitavanje i stvarni let: zamahni dodirom, izbjegavaj prepreke, prikupljaj novčiće i pokušaj ostvariti bolji rezultat.\n\nU Skins odaberi lika i upravljaj lokalnim otključavanjima. U Shopu je dostupno šest nadogradnji koje se kupuju virtualnim novčićima, bez kupnje pravim novcem. U Settingsu prilagodi zvuk, kontrole, jezik i sigurnosne kopije te pregledaj uvjete korištenja i privatnost.\n\nNapredak i virtualna sredstva čuvaju se lokalno. Igra ne zahtijeva registraciju, oglase ni internetsku vezu. Zasebni zasloni poretka, postignuća, rekorda i karte razina nisu dio trenutačne navigacije; to ne sprječava pokretanje igre pomoću PLAY.\n\n**Kategorija:** Igra → Arkadne (potvrditi u Play Consoleu).\n\n**Oglasi:** Ne. **Kupnje u aplikaciji:** Ne. Shop koristi lokalne virtualne novčiće.\n\n## Autentične slike — ugovor\n\nObjavljuju se stvarne snimke istoga Android builda: Home, Shop, Skins, Settings, stanje leta, pauza, rezultati i pravni dijalozi. Ne predstavljati pokrivenost snimkama kao dokaz potpune 1:1 RGB usklađenosti. Izostaviti uklonjene samostalne zaslone.\n\nGitHub izdanje: https://github.com/bren-wp/BOPAVI/releases/tag/@@TAG@@.\nProvjera na fizičkom uređaju i Play Store odobrenje nisu potvrđeni.\n",
        "docs/play/RELEASE-CHECKLIST.md": "# BOPAVI — Android release acceptance checklist\n\n**Last published release evidence:** @@TAG@@ (build @@BUILD@@).\n**Application:** com.brendigo.bopavi · BRENDIGO · https://brendigo.com.\n\n## Production UI\n\n- [ ] Home exposes exactly four accessible controls: PLAY, SKINS, SHOP, SETTINGS.\n- [ ] PLAY reaches Loading and actual playable GameView without QA-only intent extras.\n- [ ] Flight, collisions, score, Pause/Resume, Retry, Home and lifecycle transitions work.\n- [ ] Shop has six real local upgrades with atomic wallet/inventory updates; Skins persist.\n- [ ] Settings audio, legal controls, SAF backup, language and accessibility work.\n- [ ] Progress, Leaderboard, Records, Achievements and Level Map remain absent as standalone pages.\n- [ ] Android 15/16 UIAutomator/emulator tests and screenshots pass at actual densities.\n- [ ] Compare authentic native PNGs against original owner WebPs per distinct state, without resizing; disclose RGB differences.\n- [ ] Test TalkBack, large fonts, rotation and physical Android devices before parity/stability claims.\n\n## Data, security and distribution\n\n- [ ] Preserve v1–v6 historical JSON formats, saved coins, skins, purchases and scores.\n- [ ] Test failed, concurrent, interrupted and externally edited SAF DocumentsProvider operations on physical devices.\n- [ ] Audit Kotlin, manifest, intents, R8, asset licenses and transitive dependencies.\n- [ ] No INTERNET permission, ad SDKs or new account requirement.\n- [ ] Run Android Lint, JVM tests, R8 APK/AAB and checksum verification for the exact commit.\n- [ ] Publish no release with stale three-action or no-PLAY claims; validate release notes before upload.\n- [ ] Sign production distribution privately with publisher-approved key.\n- [ ] Do not rewrite or delete historical GitHub releases.\n",
    }
    data=json.dumps(catalog,ensure_ascii=False,indent=2)
    return {path:value.replace("@@TAG@@",tag).replace("@@BUILD@@",str(build)).replace("@@CATALOG@@",data)
            for path,value in templates.items()}

def assert_home_play_contract(root,notes=None):
    """Fail closed before publishing if playable Home is misrepresented."""
    version,_=release_metadata(root)
    if tuple(map(int,version.split(".")))<(1,0,88):
        return
    activity=(root/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text(encoding="utf-8")
    home=(root/"android/app/src/main/java/com/brendigo/bopavi/HomeReferenceScreen.kt").read_text(encoding="utf-8")
    required=('onPlay={sound.effect("click");startGame()}','onSkins={showSkins()}',
              'onShop={showShop()}','onSettings={showSettingsScreen()}')
    if not all(action in activity for action in required):
        raise ValueError("Production Home does not expose all four genuine callbacks")
    if not all('pill("'+name+'"' in home for name in ("PLAY","SKINS","SHOP","SETTINGS")):
        raise ValueError("Native Home does not contain four labeled controls")
    matrix=(root/"docs/product/FEATURE-MATRIX.md").read_text(encoding="utf-8")
    match=re.search(r"~~~json\s*(\{.*?\})\s*~~~",matrix,re.S)
    if not match:
        raise ValueError("Missing published feature matrix")
    features=json.loads(match.group(1))
    states={entry["id"]:entry["status"] for entry in features["features"]}
    if features["home_navigation_decision"]!="approved-four-actions-play" or any(
        states.get(feature)!="visible" for feature in ("four_home_actions","gameplay_start_from_home","flight")
    ):
        raise ValueError("Published feature matrix advertises obsolete Home navigation")
    banned=("new gameplay cannot be started from home",
            "there is intentionally no production home button",
            "no production home button for starting a flight",
            "nema dostupnog gumba za pokretanje novog leta",
            "pokretanje novog leta nije dostupno",
            "exactly three accessible actions",
            "home has exactly three accessible controls")
    for name in ("README.md","docs/product/CURRENT_SCREENS.md",
                 "docs/play/STORE-LISTING-hr-HR.md","docs/play/RELEASE-CHECKLIST.md"):
        content=(root/name).read_text(encoding="utf-8")
        if any(claim in content.lower() for claim in banned):
            raise ValueError("Obsolete three-action/disabled-PLAY claim in "+name)
    for name,token in (("README.md","PLAY"),("docs/product/CURRENT_SCREENS.md","PLAY"),
                       ("docs/play/STORE-LISTING-hr-HR.md","PLAY"),
                       ("docs/play/RELEASE-CHECKLIST.md","PLAY")):
        if token not in (root/name).read_text(encoding="utf-8"):
            raise ValueError("Missing explicit Play route in "+name)
    if notes is not None:
        content=notes.read_text(encoding="utf-8").lower()
        if any(claim in content for claim in banned) or "candidate / not published" in content:
            raise ValueError("Release notes still contain stale candidate or disabled-PLAY claims")


def sync(root,items,tag):
    public=releases(items)
    if not public or public[0]["tag_name"]!=tag:raise ValueError("Expected successful public stable release "+tag)
    version,build=release_metadata(root)
    if "v"+version!=tag or "(build "+str(build)+")" not in section(public[0]):
        raise ValueError("Published versionCode/versionName mismatch")
    changelog=render(public)
    path=root/"docs/product/FEATURE-MATRIX.md";original=path.read_text(encoding="utf-8")
    match=re.search(r"~~~json\s*(\{.*?\})\s*~~~",original,re.S)
    if not match:raise ValueError("Feature matrix missing")
    catalog=json.loads(match.group(1));previous=str(catalog["release"]);old_build=int(catalog["build"])
    catalog.update(release=tag,build=build,publication_status="published",last_published_github_release=tag)
    new_matrix=original[:match.start(1)]+json.dumps(catalog,ensure_ascii=False,indent=2)+original[match.end(1):]
    updates={"CHANGELOG.md":changelog,"docs/product/FEATURE-MATRIX.md":new_matrix}
    for name in ACTIVE:
        old=(root/name).read_text(encoding="utf-8")
        updates[name]=old.replace(previous,tag).replace("build "+str(old_build),"build "+str(build))
    if four_action_version(tag):
        catalog=four_action_catalog(catalog)
        updates.update(four_action_documents(tag,build,catalog))
    elif three_action_version(tag):
        # Historical v1.0.84–v1.0.87 releases had only three Home actions.
        catalog=three_action_catalog(catalog)
        updates.update(three_action_documents(tag,build,catalog))
    # The publication has finished: it is no longer an unreleased candidate.
    pending=(root/"docs/release/NEXT.md").read_text(encoding="utf-8")
    if re.search(rf"(?m)^## {re.escape(tag)}\s+[—-]",pending):
        updates["docs/release/NEXT.md"]=(
            "# Next Android release\n\nNo unreleased changes are staged.\n"
            "Increase the Android versionName/versionCode and review a new NEXT.md "
            "entry before publishing another release.\n"
        )
    modified=[]
    for name,value in updates.items():
        dest=root/name
        if dest.read_text(encoding="utf-8")!=value:dest.write_text(value,encoding="utf-8");modified.append(name)
    return modified
if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--releases",type=Path)
    p.add_argument("--expected-tag")
    p.add_argument("--check-home-play",action="store_true")
    p.add_argument("--notes",type=Path)
    args=p.parse_args()
    root=Path(__file__).resolve().parents[1]
    if args.check_home_play:
        assert_home_play_contract(root,args.notes)
        print("PASS: published Home PLAY contract and release notes")
    else:
        if args.releases is None or args.expected_tag is None:
            p.error("--releases and --expected-tag are required when syncing")
        changed=sync(root,decode_pages(args.releases.read_text()),args.expected_tag)
        print("PASS: idempotent published-release documentation:",changed or "unchanged")
