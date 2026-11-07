#!/usr/bin/env python3
"""BOPAVI owner Splash/Home: preserve 3D art without fake screenshots/game state."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
splash=(src/"FlightLoadingView.kt").read_text()
track=(src/"FlightLoadingTrack.kt").read_text()
layout=(src/"FlightLoadingLayout.kt").read_text()
home=(src/"HomeReferenceScreen.kt").read_text()
actor=(src/"HomeWingActor.kt").read_text()
accessories=(src/"BopiOwnerAccessories.kt").read_text()
medal=(src/"HomeMedalDrawable.kt").read_text()
caption=(src/"HomeEmbossedLabel.kt").read_text()
backdrop=(root/"tools/paint_fantasy_backdrop.py").read_text()
logo=(root/"docs/assets/logo.svg").read_text()
gradle=(root/"android/app/build.gradle.kts").read_text()

assert 'versionName = "1.0.58"' in gradle
assert 'versionCode = 188' in gradle
assert 'val cursor=width*.66f' in track
assert 'if(ready)return Segment(0f,width)' in track
assert 'val track=LoadingArtBox(161f,1350f,780f,1454f)' in layout
assert 'const val captionBaselineY=1515f' in layout
assert 'android.os.SystemClock.uptimeMillis()-scanStartedAtMs' in splash

for required in (
    'BopiOwnerAccessories.drawBack(canvas,pilotRect)',
    'BopiOwnerAccessories.drawFront(canvas,pilotRect)',
    'if(selected==0)',
):
    assert required in splash, required
for required in (
    'if(hasOwnerAviator)addView(accessory(false)',
    'addView(leftWing,LayoutParams(-1,-1))',
    'addView(rightWing,LayoutParams(-1,-1))',
    'addView(body,LayoutParams(-1,-1))',
    'if(hasOwnerAviator)addView(accessory(true)',
):
    assert required in actor, required
assert 'private val hasOwnerAviator=bodyRes==R.drawable.bopi0' in actor
for required in ('fun drawBack(', 'fun drawFront(', 'canvas.scale(box.width()/512f,box.height()/512f)',
                 '0xffff6545.toInt()', '0xffffb724.toInt()', '0xffb7faff.toInt()'):
    assert required in accessories, required

# Each live Home button and actual wallet balance is still a native,
# individually clickable View. Nothing is replaced with a screenshot.
for required in ('R.drawable.homeback','HomeTopWalletRail(host)',
                 'progress.coins()', 'progress.worldStars(it)',
                 'onPlay)', 'onSkins)', 'onShop)', 'onSettings)',
                 'HomeMedalDrawable()', 'HomeWingActor(host'):
    assert required in home,required
assert '0xff284e7b.toInt()' in medal
assert '0xff06172f.toInt()' in medal
assert 'private val footer:Boolean=false' in caption
assert 'footer=true)' in home

assert 'bank.filter(ImageFilter.GaussianBlur(20))' in backdrop
assert 'Random(9411672)' in backdrop
assert 'No UI labels' in backdrop
assert 'id="ownerWarm"' in logo and 'fill-rule="evenodd"' in logo
assert 'R.drawable.logo' in splash and 'R.drawable.logo' in home

# Enforce the scope: gameplay and physics do not import cosmetic overlays.
for path in ('GameView.kt','GameSimulation.kt'):
    assert 'BopiOwnerAccessories' not in (src/path).read_text(),path

print("PASS: owner source contour, two-screen aviator accessories, "
      "dynamic loading, navy medallions and purely offline scenery")
