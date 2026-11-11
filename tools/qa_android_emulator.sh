#!/bin/sh
# Android visual-flow smoke: capture actual home, gameplay and collision result.
set -eu
mkdir -p qa/screenshots
# A hosted Android emulator can disappear from ADB after many successful
# navigation assertions. This is a runner transport failure, NOT proof that
# BOPAVI crashed. Mark only an actual missing/offline ADB device at exit,
# so CI may re-create an emulator and repeat the *entire* interactive test.
# Never retry an assertion error while the device is connected; such a failure
# must stay red and preserve diagnostics.
qa_exit_diagnostic() {
  qa_exit_code="$1"
  trap - 0
  # Restore the emulator display even after a failed compact/tall layout test.
  if [ "${BOPAVI_RESIZE_IN_PROGRESS:-0}" = 1 ]; then
    adb shell wm size reset >/dev/null 2>&1 || true
    adb shell wm density reset >/dev/null 2>&1 || true
  fi
  if [ "$qa_exit_code" -ne 0 ]; then
    qa_adb_state="$(adb get-state 2>&1 || true)"
    if [ "$qa_adb_state" != "device" ]; then
      printf 'Confirmed ADB transport loss: %s\n' "$qa_adb_state" >&2
      printf 'ADB transport unavailable at test exit: %s\n' "$qa_adb_state" > qa/screenshots/adb-transport-offline.retry
      adb devices -l >&2 || true
    else
      echo 'Connected Android device: preserving genuine application/test failure; no emulator retry' >&2
    fi
  fi
  exit "$qa_exit_code"
}
trap 'qa_exit_diagnostic "$?"' 0
adb wait-for-device
adb install -r qa/apk/app-debug.apk
# Preserve an isolated launch window. Emulator cold-boot log spam can evict
# app exceptions from the ordinary main log before our startup timeout.
adb logcat -c >/dev/null 2>&1 || true
# Debug-only QA: keep the real first active frame stable through slow Pixel
# screencap calls. GameSimulation resumes with a second genuine flap, and the
# production (non-debuggable) build ignores this Intent extra entirely.
adb shell am start -W -n com.brendigo.bopavi/.MainActivity --ez bopavi_qa_capture_first_flap true
# Android 16 needs extra time after the cold boot before System UI and
# Accessibility Manager can reliably serve window hierarchy dumps.
sleep 12
# On AGP/edge-to-edge migrations, a process that vanished before the first
# screenshot is a real startup failure. Capture the OS exception and process
# death record before the emulator is torn down; do NOT treat it as an ADB
# transport error or retry it away.
if ! adb shell pidof com.brendigo.bopavi; then
  echo 'FAIL: BOPAVI process exited during native Activity startup' >&2
  adb logcat -b crash -d -v threadtime > qa/screenshots/android-startup-crash.txt 2>&1 || true
  adb logcat -d -v threadtime -s AndroidRuntime:* ActivityManager:* ActivityTaskManager:*       > qa/screenshots/android-startup-processes.txt 2>&1 || true
  adb shell dumpsys activity activities > qa/screenshots/android-startup-activities.txt 2>&1 || true
  cat qa/screenshots/android-startup-crash.txt >&2 || true
  grep -E -C 10 'com.brendigo.bopavi|FATAL EXCEPTION|crash|died|has died|force-finishing|Start proc'       qa/screenshots/android-startup-processes.txt | tail -n 200 >&2 || true
  exit 1
fi

capture(){
  file="qa/screenshots/$1.png"
  attempt=1
  while [ "$attempt" -le 4 ]; do
    if adb exec-out screencap -p > "$file" &&
       python3 - "$file" <<'PY'
import sys
from pathlib import Path
path=Path(sys.argv[1])
assert path.stat().st_size>2000
assert path.read_bytes()[:8]==bytes.fromhex("89504e470d0a1a0a")
PY
    then
      echo "PASS: screenshot $file"
      return 0
    fi
    echo "Screenshot attempt $attempt failed for $file" >&2
    attempt=$((attempt+1))
    sleep 3
  done
  adb logcat -d -t 180 | tail -n 75 || true
  return 1
}

# Find the real accessible PLAY view instead of guessing a screen percentage.
dump_ui(){
  # API36 may return a text error in place of hierarchy XML immediately
  # after boot or during an unrelated System UI ANR. Never treat that
  # diagnostic as a screen, nor let stale XML count as a passing test.
  attempt=1
  while [ "$attempt" -le 5 ]; do
    adb shell rm -f /sdcard/bopavi-window.xml >/dev/null 2>&1 || true
    adb shell uiautomator dump /sdcard/bopavi-window.xml >/dev/null 2>&1 || true
    adb exec-out cat /sdcard/bopavi-window.xml > qa/screenshots/android-current-ui.xml 2>/dev/null || true
    if python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
try:
    root=ET.parse(sys.argv[1]).getroot()
    assert root.tag=="hierarchy" and list(root.iter("node"))
except (ET.ParseError,AssertionError,OSError):
    sys.exit(1)
PY
    then
      return 0
    fi
    echo "Waiting for valid Android hierarchy, attempt $attempt/5" >&2
    attempt=$((attempt+1))
    sleep 4
  done
  echo "FAIL: Android System UI did not provide a valid interactive hierarchy" >&2
  adb shell pidof com.brendigo.bopavi || true
  adb logcat -d -t 200 | tail -n 75 >&2 || true
  return 1
}
# Android 15 and 16 can consume an injected Back key while a dismissed
# AlertDialog window is detaching. Do not rely on that timing to leave Settings:
# tap its actual accessible HOME button and require Home to appear afterward.
# This verifies a real application route rather than treating Back as success.
return_home_from_settings(){
  dump_ui
  # A real Android 16 device can deliver the native ZATVORI click while the
  # legal dialog's window is still transitioning. Retry ONLY a verified
  # BOPAVI-owned legal dialog (exact title and close button), never blindly
  # press Back or bypass the subsequent real Settings/Home assertions.
  # A fresh fullscreen navigation can surface a SYSTEM tutorial over
  # Settings after the legal dialog closes. Acknowledge only its fully
  # verified AOSP title/description/button, then re-read the native page.
  if acknowledge_verified_fullscreen_education; then
    sleep 1
    dump_ui
  fi
  legal_close_attempt=1
  while [ "$legal_close_attempt" -le 3 ]; do
    if python3 - <<'PY'
import re,subprocess,sys,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
titles=[n for n in nodes
    if n.get("package")=="com.brendigo.bopavi"
    and n.get("resource-id")=="android:id/alertTitle"
    and n.get("text") in ("Politika privatnosti","Uvjeti korištenja")]
if len(titles)!=1:
    sys.exit(1)
closing=[n for n in nodes
    if n.get("package")=="com.brendigo.bopavi"
    and n.get("resource-id")=="android:id/button1"
    and n.get("text")=="ZATVORI"
    and n.get("clickable")=="true"]
if len(closing)!=1:
    raise SystemExit("FAIL: recognized legal dialog lacks native Close")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",closing[0].get("bounds",""))
if not m:
    raise SystemExit("FAIL: legal close bounds invalid")
x,y,z,w=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((x+z)//2),str((y+w)//2)],check=True)
print("QA: retried verified legal dialog close",flush=True)
PY
    then
      sleep 2
      dump_ui
    else
      break
    fi
    legal_close_attempt=$((legal_close_attempt+1))
  done
  # A valid legal AlertDialog may ignore adb's synthetic coordinate tap
  # when the emulator window is still animating. Use Android's native Back
  # dismissal only after verifying the exact app-owned modal and its close
  # button. Then require real Settings and a real Home button as usual.
  if python3 - qa/screenshots/android-current-ui.xml <<'PY_LEGAL_STILL_OPEN'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
titles=[n for n in nodes if n.get("package")=="com.brendigo.bopavi"
    and n.get("resource-id")=="android:id/alertTitle"
    and n.get("text") in ("Politika privatnosti","Uvjeti korištenja")]
closing=[n for n in nodes if n.get("package")=="com.brendigo.bopavi"
    and n.get("resource-id")=="android:id/button1"
    and n.get("text")=="ZATVORI" and n.get("enabled")=="true"]
sys.exit(0 if len(titles)==1 and len(closing)==1 else 1)
PY_LEGAL_STILL_OPEN
  then
    adb shell input keyevent KEYCODE_BACK
    sleep 2
    dump_ui
    echo "QA: dismissed verified native legal modal by Android Back"
  fi
  if acknowledge_verified_fullscreen_education; then
    sleep 1
    dump_ui
  fi
  # The real 12-row accessible Settings ScrollView may be scrolled all the
  # way to Terms after checking legal links. Its header/HOME are offscreen.
  # Navigate *within the app* back to the top; never bypass navigation or
  # accept launcher/OS pages in place of Settings.
  python3 - <<'PY_SCROLL_TO_SETTINGS_TOP'
import re,subprocess,time,xml.etree.ElementTree as ET
path="qa/screenshots/android-current-ui.xml"
for attempt in range(9):
    nodes=list(ET.parse(path).getroot().iter("node"))
    title=[n for n in nodes if n.get("package")=="com.brendigo.bopavi"
           and n.get("text","").upper()=="SETTINGS"]
    home=[n for n in nodes if n.get("package")=="com.brendigo.bopavi"
          and n.get("clickable")=="true" and n.get("content-desc")=="HOME"]
    if len(title)==1 and len(home)==1:
        print("PASS: Settings header and native HOME are visible",flush=True)
        break
    scrolling=[n for n in nodes if n.get("package")=="com.brendigo.bopavi"
               and n.get("class")=="android.widget.ScrollView"
               and n.get("scrollable")=="true"]
    if len(scrolling)!=1:
        raise SystemExit("FAIL: Settings top unreachable; actual app ScrollView absent")
    m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
                   scrolling[0].get("bounds",""))
    if not m:
        raise SystemExit("FAIL: Settings ScrollView bounds invalid")
    a,b,c,d=map(int,m.groups())
    x=a+(c-a)//2
    start=b+max(1,(d-b)//3)
    end=b+max(2,((d-b)*5)//6)
    if start>=end:
        raise SystemExit("FAIL: invalid Settings scroll area")
    subprocess.run(["adb","shell","input","swipe",str(x),str(start),
                    str(x),str(end),"320"],check=True,timeout=20)
    time.sleep(.4)
    subprocess.run(["adb","shell","uiautomator","dump",
                    "/sdcard/bopavi-settings-top.xml"],check=True,
                   timeout=45,capture_output=True)
    with open(path,"wb") as output:
        subprocess.run(["adb","exec-out","cat",
                        "/sdcard/bopavi-settings-top.xml"],check=True,
                       stdout=output,timeout=25)
else:
    raise SystemExit("FAIL: Settings title and HOME not reachable after 8 upward scrolls")
PY_SCROLL_TO_SETTINGS_TOP
  dump_ui
  python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
if not any(n.get("text","").upper()=="SETTINGS" for n in nodes):
    raise SystemExit("FAIL: Settings title missing before Home navigation")
buttons=[n for n in nodes if n.get("clickable")=="true" and
         n.get("content-desc")=="HOME"]
if len(buttons)!=1:
    raise SystemExit("FAIL: Settings native HOME button missing or duplicated")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",buttons[0].get("bounds",""))
if not m:raise SystemExit("FAIL: Settings HOME button bounds invalid")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
  sleep 1
  dump_ui
  # Android's own one-time immersive-mode education popup may intercept the
  # return to Home on a fresh emulator. Dismiss ONLY this exact OS window,
  # never a BOPAVI dialog, and still require the real Home hierarchy below.
  if python3 - <<'PY'
import re,subprocess,sys,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
title=[n for n in nodes if n.get("package")=="android" and
       n.get("resource-id")=="android:id/immersive_cling_title" and
       n.get("text")=="Viewing full screen"]
if not title:
    sys.exit(1)
ok=[n for n in nodes if n.get("package")=="android" and
    n.get("resource-id")=="android:id/ok" and
    n.get("text")=="Got it" and n.get("clickable")=="true"]
if len(title)!=1 or len(ok)!=1:
    raise SystemExit("FAIL: unexpected immersive system dialog")
m=re.fullmatch(r"\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]",ok[0].get("bounds",""))
if not m:
    raise SystemExit("FAIL: immersive education button bounds invalid")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
print("PASS: dismissed exact Android immersive-mode education dialog")
PY
  then
    sleep 1
    dump_ui
  fi
  python3 - <<'PY'
import xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
home=[n for n in nodes if n.get("content-desc")=="Otvori opcije igre"
      and n.get("clickable")=="true"]
if len(home)!=1:
    raise SystemExit("FAIL: Settings native HOME button did not return Home")
PY
}
# The hosted Pixel emulator can show a *system launcher* ANR over a healthy
# BOPAVI Activity. Recover only that exact dialog; never dismiss a BOPAVI ANR.
# The next assertion still requires the real destination screen to be visible.
recover_launcher_anr(){
  adb shell pidof com.brendigo.bopavi >/dev/null || return 1
  python3 - qa/screenshots/android-current-ui.xml <<'PY'
import re, subprocess, sys, xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
titles=[n.get("text","") for n in root.iter("node")
        if n.get("resource-id")=="android:id/alertTitle"]
waits=[n for n in root.iter("node") if n.get("resource-id")=="android:id/aerr_wait"
       and n.get("text")=="Wait" and n.get("clickable")=="true"]
if len(waits)!=1 or titles not in (
    ["Pixel Launcher isn't responding"],["Quickstep isn't responding"],
    ["System UI isn't responding"]):
    sys.exit(1)
m=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',waits[0].get("bounds",""))
if not m:
    raise SystemExit("FAIL: invalid system launcher Wait button bounds")
left,top,right,bottom=map(int,m.groups())
# Pixel Launcher and System UI are distinct system processes. Recovery
# is allowed ONLY for these verified dialog titles, never for a BOPAVI ANR.
if titles in (["Pixel Launcher isn't responding"],
              ["Quickstep isn't responding"]):
    # AOSP API36's launcher is Quickstep/com.android.launcher3; Pixel
    # Launcher is the distinct Google launcher package. Never kill BOPAVI.
    launcher=("com.android.launcher3" if titles==["Quickstep isn't responding"]
              else "com.google.android.apps.nexuslauncher")
    print("Stopping confirmed unresponsive system launcher: "+launcher,flush=True)
    subprocess.run(["adb","shell","am","force-stop",launcher],check=True)
else:
    # Never force-stop System UI (which owns status/navigation surfaces).
    # Press the exact native Wait action to let it recover, then require the
    # real BOPAVI page to appear before any smoke-test PASS.
    print("Waiting for confirmed hung System UI to recover (not BOPAVI)",flush=True)
    subprocess.run(["adb","shell","input","tap",
                    str((left+right)//2),str((top+bottom)//2)],check=True)
subprocess.run(["adb","shell","pidof","com.brendigo.bopavi"],check=True,
               stdout=subprocess.DEVNULL)
PY
}
# Android may show its own one-time immersive-mode confirmation when
# Flight first hides the navigation bar. This is a system-owned education
# overlay, NOT a BOPAVI screen or a failed PLAY route. Dismiss only the exact
# Android 15 title/instructions/button, then still assert the real GameView.
# A custom dialog, a BOPAVI crash or an unrelated overlay must never pass.
acknowledge_verified_fullscreen_education(){
  adb shell pidof com.brendigo.bopavi >/dev/null || return 1
  python3 - qa/screenshots/android-current-ui.xml <<'PY_FULLSCREEN'
import re, subprocess, sys, xml.etree.ElementTree as ET
try:
    root=ET.parse(sys.argv[1]).getroot()
except (OSError,ET.ParseError):
    sys.exit(1)
nodes=list(root.iter("node"))
# AOSP API35 reports package "android"; AOSP API36 reports
# "com.android.systemui" for the SAME built-in immersive guidance.
system_packages=("android","com.android.systemui")
titles=[n for n in nodes if n.get("package") in system_packages
        and n.get("text")=="Viewing full screen"]
if len(titles)!=1:sys.exit(1)
system_package=titles[0].get("package")
def system_text(value):
    return [n for n in nodes if n.get("package")==system_package and
            n.get("text")==value]
if len(system_text("To exit, swipe down from the top of your screen"))!=1:
    sys.exit(1)
buttons=[n for n in nodes if n.get("package")==system_package and
         n.get("class")=="android.widget.Button" and n.get("text")=="Got it"
         and n.get("clickable")=="true" and n.get("enabled")=="true"]
if len(buttons)!=1:sys.exit(1)
m=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',buttons[0].get("bounds",""))
if not m:sys.exit(1)
a,b,c,d=map(int,m.groups())
if a>=c or b>=d:sys.exit(1)
print("Acknowledging verified Android system immersive education",flush=True)
subprocess.run(["adb","shell","input","tap",
                str((a+c)//2),str((b+d)//2)],check=True)
PY_FULLSCREEN
}
# New production UX: one Play -> live endless game -> genuine result -> Retry.
sleep 2
dump_ui
# A system-only Pixel Launcher ANR can cover an otherwise running BOPAVI
# after emulator cold boot. Recover ONLY the allowlisted system dialog,
# then capture a fresh hierarchy and demand the actual game's PLAY control.
if recover_launcher_anr; then
  sleep 3
  dump_ui
fi
# Wait for real accessible native Home controls before tapping Settings.
# This is a bounded stability check, not a simulated pass: absent controls
# still fail and their genuine UI hierarchy is kept for diagnosis.
home_ready=0
# API 36 hosted emulators sometimes finish drawing the first Home frame only
# after system launcher/input stabilizes. Still require the SAME real, enabled
# Play/Settings/Skins/Shop View hierarchy; no synthetic screen accepted.
for attempt in $(seq 1 16); do
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY_HOME'
import sys, xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
actions={n.get("content-desc","") for n in nodes
         if n.get("clickable")=="true" and n.get("enabled")=="true"}
expected={"▶  PLAY","Otvori opcije igre","Otvori izbor likova",
          "Otvori trgovinu nadogradnji"}
sys.exit(0 if expected.issubset(actions) else 1)
PY_HOME
  then home_ready=1; break; fi
  echo "Waiting for real Home buttons ($attempt/16)" >&2
  # Recover only a positively identified Pixel Launcher/System UI hang.
  # Never restart, force-stop or silently skip a failing BOPAVI Activity.
  if recover_launcher_anr; then
    echo "Recovered confirmed SYSTEM-owned dialog; rechecking real Home" >&2
  fi
  # Splash/Home's immersive camera can trigger Android's first-use tutorial
  # before Play. Dismiss ONLY the verified system-owned tutorial, then
  # require real native Home actions on the next iteration.
  if acknowledge_verified_fullscreen_education; then
    echo "Dismissed OS immersive tutorial on Home; rechecking native controls" >&2
  fi
  sleep 2
done
if [ "$home_ready" -ne 1 ]; then
  echo "FAIL: Home native controls remained inaccessible" >&2
  echo "Last real UI hierarchy follows (limited to 120 lines):" >&2
  head -n 120 qa/screenshots/android-current-ui.xml >&2 || true
  adb shell dumpsys activity activities | tail -n 45 >&2 || true
  cp qa/screenshots/android-current-ui.xml qa/screenshots/android-home-failed-ui.xml || true
  # Capture the actual ANR diagnostics rather than treating an inaccessible
  # Home as a UI-automation flake. The next build must fix the main thread.
  adb logcat -d -t 1600 > qa/screenshots/android-home-failed-logcat.txt 2>&1 || true
  adb shell dumpsys activity processes > qa/screenshots/android-home-failed-processes.txt 2>&1 || true
  adb shell dumpsys activity anr > qa/screenshots/android-home-failed-anr.txt 2>&1 || true
  adb shell dumpsys input > qa/screenshots/android-home-failed-input.txt 2>&1 || true
  exit 1
fi
capture android-home
# A freshly recovered hosted Launcher can swallow one tap. Recheck the
# ACTUAL legal dialog and owner body text rather than counting a tap as success.
# Retry a native Home link at most three times while the game remains reachable;
# missing content, crashes or a non-home screen must still fail closed.
open_legal_dialog(){
  legal_link="$1"
  legal_title="$2"
  legal_owner_text="$3"
  legal_required_text="$4"
  legal_opened=0
  # The illustrated Home contains no legal footer; open the real Settings
  # route first and scroll its native settings page to the legal rows.
  dump_ui
  python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
options=[n for n in nodes if n.get("clickable")=="true"
         and n.get("content-desc")=="Otvori opcije igre"]
if len(options)!=1:raise SystemExit("FAIL: Home Settings missing for offline legal check")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",options[0].get("bounds",""))
if not m:raise SystemExit("FAIL: invalid native Settings tap bounds")
x,y,z,w=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((x+z)//2),str((y+w)//2)],check=True)
PY
  # Settings has TWO valid layouts: the fixed 941px reference camera
  # (both legal buttons already visible), and a compact ScrollView (links
  # may be below the fold). Never send an unconditional swipe before reading
  # real native controls. On the fixed camera that gesture could land over
  # the Terms row and open the WRONG modal during the Privacy check.
  # Search/scroll below only if the requested accessible control is absent.
  sleep 1
  dump_ui
  # A fresh Settings transition may trigger the exact Android OS one-time
  # immersive tutorial BEFORE Settings becomes accessible. A failing API35
  # run proved that its system-only window hides the legal controls even
  # though BOPAVI itself is healthy. Acknowledge ONLY the verified Android
  # system guidance, then still require the real Settings legal link.
  if acknowledge_verified_fullscreen_education; then
    sleep 1
    dump_ui
    echo "QA: resumed real Settings after AOSP immersive education"
  fi
  for legal_attempt in 1 2 3; do
    dump_ui
    if acknowledge_verified_fullscreen_education; then
      sleep 1
      dump_ui
    fi
    if ! python3 - "$legal_link" <<'PY'
import re,subprocess,sys,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
links=[n for n in nodes if n.get("clickable")=="true"
       and n.get("content-desc","")==sys.argv[1]]
if len(links)!=1:
    # The Settings dialog scrolls; its legal footer may be below the viewport.
    # Keep this a genuine accessibility gate, not an unconditional success.
    import time
    for _ in range(5):
        size=subprocess.check_output(["adb","shell","wm","size"],text=True)
        dims=re.findall(r"(\d+)x(\d+)",size)
        if not dims:raise SystemExit("FAIL: missing Settings viewport dimensions")
        width,height=map(int,dims[-1])
        subprocess.run(["adb","shell","input","swipe",str(width//2),str(height*76//100),
                        str(width//2),str(height*24//100),"400"],check=True)
        time.sleep(.5)
        subprocess.run(["adb","shell","uiautomator","dump","/sdcard/bopavi-legal-search.xml"],
                       check=True,capture_output=True,timeout=40)
        output=subprocess.check_output(["adb","exec-out","cat","/sdcard/bopavi-legal-search.xml"])
        nodes=list(ET.fromstring(output).iter("node"))
        links=[n for n in nodes if n.get("clickable")=="true"
               and n.get("content-desc","")==sys.argv[1]]
        if len(links)==1:break
if len(links)!=1:raise SystemExit("FAIL: Settings legal control missing after scrolling")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",links[0].get("bounds",""))
if not m:raise SystemExit("FAIL: native legal tap bounds missing")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
    then
      echo 'FAIL: Settings legal control missing' >&2
      cp qa/screenshots/android-current-ui.xml qa/screenshots/android-settings-legal-failed.xml || true
      adb shell dumpsys activity activities > qa/screenshots/android-settings-legal-activities.txt 2>&1 || true
      return 1
    fi
    sleep 2
    dump_ui
    # Verify the owner/title/required section and dismiss from the SAME
    # hierarchy. On slow API35 uiautomator, an additional dump can show the
    # resumed Home instead of the already-verified modal.
    if python3 - "$legal_title" "$legal_owner_text" "$legal_required_text" <<'PY'
import re,subprocess,sys,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
alltext=" ".join(label(n) for n in nodes)
for required in sys.argv[1:]:
    if required not in alltext:raise SystemExit("FAIL: in-app legal dialog lacks "+required)
# Capture the actual verified native legal window before dismissing it.
# These are original adb pixels, not screenshots fabricated from HTML.
from pathlib import Path
screen="android-privacy" if sys.argv[1]=="POLITIKA PRIVATNOSTI" else "android-terms"
capture=subprocess.check_output(["adb","exec-out","screencap","-p"])
if capture[:8]!=bytes.fromhex("89504e470d0a1a0a") or len(capture)<2000:
    raise SystemExit("FAIL: original Android legal screenshot is invalid")
Path("qa/screenshots/"+screen+".png").write_bytes(capture)
closing=[n for n in nodes if n.get("clickable")=="true" and "ZATVORI" in label(n)]
if len(closing)!=1:raise SystemExit("FAIL: native legal dialog Close missing")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",closing[0].get("bounds",""))
if not m:raise SystemExit("FAIL: native legal Close bounds missing")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
    then legal_opened=1;break; fi
    echo "Legal dialog not yet visible after native tap $legal_attempt/3" >&2
  done
  test "$legal_opened" -eq 1 || { echo "FAIL: native legal dialog did not open with owner text" >&2; return 1; }
  # Legal dialog was dismissed; use the actual native Settings HOME action.
  return_home_from_settings
}
# Read the ACTUAL native policy dialog before entering flight, not static copy.
open_legal_dialog "Pročitaj politiku privatnosti" "POLITIKA PRIVATNOSTI" "BRENDIGO" "OBRIŠI PODATKE"
echo "PASS: Croatian privacy dialog accessible, offline and dismissible"
sleep 1
dump_ui
# Terms must work offline from the second bottom link on the same Home screen.
open_legal_dialog "Pročitaj uvjete korištenja" "UVJETI KORIŠTENJA" "BRENDIGO" "OGRANIČENU"
echo "PASS: native terms dialog accessible, offline and dismissible"
sleep 1
dump_ui
# Verify the full native Settings screen, difficulty, and return Home.
python3 - <<'PY'
import subprocess,re,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
settings=[n for n in nodes if n.get("clickable")=="true"
          and n.get("content-desc")=="Otvori opcije igre"]
if len(settings)!=1:raise SystemExit("FAIL: Home Settings inaccessible")
coords=list(map(int,re.findall("[0-9]+",settings[0].get("bounds",""))))
if len(coords)!=4:raise SystemExit("FAIL: Settings hitbox invalid")
a,b,c,d=coords
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
sleep 1
dump_ui
capture android-options
python3 - <<'PY'
import re,subprocess,time,xml.etree.ElementTree as ET

def fresh():
    subprocess.run(["adb","shell","uiautomator","dump","/sdcard/bopavi-settings-control-check.xml"],
                   check=True,capture_output=True,timeout=45)
    data=subprocess.check_output(["adb","exec-out","cat",
                                  "/sdcard/bopavi-settings-control-check.xml"],timeout=25)
    return list(ET.fromstring(data).iter("node"))

def match(nodes,label,clickable):
    hits=[n for n in nodes if n.get("package")=="com.brendigo.bopavi"
          and (n.get("text","")==label or n.get("content-desc","")==label)
          and (clickable is None or n.get("clickable")=="true")]
    if len(hits)>1:
        # Cloud Save owns both a visible title and a real Android Switch
        # with the same accessible name. Verify that exact paired structure
        # instead of treating two different native roles as duplicate items.
        text=[n for n in hits if n.get("class")=="android.widget.TextView"
              and n.get("text")==label]
        toggle=[n for n in hits if n.get("class")=="android.widget.Switch"
                and n.get("content-desc")==label
                and n.get("clickable")=="true"]
        if label=="Cloud Save" and len(hits)==2 and len(text)==1 and len(toggle)==1:
            return toggle[0] if clickable else text[0]
        raise SystemExit("FAIL: duplicate native Settings item: "+label)
    return hits[0] if hits else None

def scroll_settings(nodes,direction):
    # On high-density phones 12 actual Android setting rows exceed viewport.
    # Move the app's true ScrollView, never skip off-screen controls.
    scrolling=[n for n in nodes if n.get("package")=="com.brendigo.bopavi"
               and n.get("class")=="android.widget.ScrollView"
               and n.get("scrollable")=="true"]
    if not scrolling:
        return False
    if len(scrolling)!=1:
        raise SystemExit("FAIL: duplicate Settings scrollers")
    m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
                   scrolling[0].get("bounds",""))
    if not m:
        raise SystemExit("FAIL: Settings scroll bounds invalid")
    a,b,c,d=map(int,m.groups())
    x=a+(c-a)//2
    upper=b+(d-b)//4
    lower=b+4*(d-b)//5
    if lower<=upper:
        raise SystemExit("FAIL: Settings scroll gesture bounds invalid")
    first,second=(lower,upper) if direction=="down" else (upper,lower)
    subprocess.run(["adb","shell","input","swipe",str(x),str(first),
                    str(x),str(second),"310"],check=True,timeout=20)
    time.sleep(.55)
    return True

def find(label,clickable=None):
    nodes=fresh()
    hit=match(nodes,label,clickable)
    if hit is not None:
        return hit
    for direction in ("down","up"):
        for _ in range(8):
            if not scroll_settings(nodes,direction):
                break
            nodes=fresh()
            hit=match(nodes,label,clickable)
            if hit is not None:
                return hit
    raise SystemExit("FAIL: native Settings item absent after real scrolling: "+label)

def tap(label,clickable=None):
    n=find(label,clickable)
    m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",n.get("bounds",""))
    if not m:raise SystemExit("FAIL: invalid native Settings bounds for "+label)
    a,b,c,d=map(int,m.groups())
    subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],
                   check=True,timeout=20)
    time.sleep(.85)

# Reference includes nine options; actual Difficulty is reached via Controls,
# not three extra inline buttons that disrupt 1:1 Settings composition.
for label in ("SETTINGS","Music","Sound Effects","Notifications",
              "Language","Controls","Vibration","Cloud Save",
              "Privacy Policy","Terms of Service","Restore Purchases"):
    find(label)
tap("Touch",True)
find("Controls")
tap("Difficulty: Standardno",True)
find("Opušteno")
tap("Opušteno",True)
find("SETTINGS")
tap("Touch",True)
find("Difficulty: Opušteno")
tap("Difficulty: Opušteno",True)
tap("Standardno",True)
find("SETTINGS")
tap("Touch",True)
find("Difficulty: Standardno")
subprocess.run(["adb","shell","input","keyevent","KEYCODE_BACK"],check=True,timeout=20)
time.sleep(.6)
find("SETTINGS")
print("PASS: real nine-row Settings, dynamic difficulty dialog and persisted selection",flush=True)
PY
return_home_from_settings
echo "PASS: full native Settings, persisted difficulty and Home navigation"
sleep 1
dump_ui
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
records=[n for n in nodes if n.get("clickable")=="true" and
    n.get("content-desc")=="Pogledaj svoje rekorde"]
if len(records)!=1:raise SystemExit("FAIL: Home records action missing")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",records[0].get("bounds",""))
if not m:raise SystemExit("FAIL: records action bounds missing")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
sleep 1
dump_ui
capture android-records
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
alltext=" ".join(label(n) for n in nodes)
for expected in ("LEADERBOARD","OFFLINE","THIS DEVICE","BEST SCORE","TOP 10 LOCAL SCORES","HOME"):
    if expected not in alltext:raise SystemExit("FAIL: native offline leaderboard missing "+expected)
home=[n for n in nodes if n.get("clickable")=="true"
      and n.get("content-desc","").upper()=="HOME"]
if len(home)!=1:raise SystemExit("FAIL: Leaderboard Home action missing")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",home[0].get("bounds",""))
if not m:raise SystemExit("FAIL: Leaderboard Home hitbox invalid")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
print("PASS: local records screen visible and returns Home",flush=True)
PY
sleep 1
dump_ui
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
root=ET.parse("qa/screenshots/android-current-ui.xml").getroot()
nodes=list(root.iter("node"))
click=[n for n in nodes if n.get("clickable")=="true"]
def label(n):return (n.get("text","")+" "+n.get("content-desc","")).upper()
play=[n for n in click if "PLAY" in label(n) or "IGRAJ" in label(n)]
if len(play)!=1:raise SystemExit("FAIL: one native PLAY action required")
if any("BEST SCORE" in label(n) or "NAJBOLJI REZULTAT" in label(n) for n in nodes):raise SystemExit("FAIL: obsolete Home record badge returned")
# HomeReferenceScreen uses a real clickable LinearLayout with the
# TalkBack action on its parent and visible text on a child TextView.
# Require BOTH the actionable parent and the actual visible label.
for menu,action in (
    ("SKINS","Otvori izbor likova"),
    ("SHOP","Otvori trgovinu nadogradnji"),
):
    actions=[n for n in click if n.get("content-desc")==action]
    labels=[n for n in nodes if n.get("text","").strip().upper()==menu]
    if len(actions)!=1 or len(labels)!=1:
        raise SystemExit("FAIL: Home "+menu+" must have one tappable action and visible label")
    m=lambda n: re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",n.get("bounds",""))
    parent,child=m(actions[0]),m(labels[0])
    if not parent or not child:
        raise SystemExit("FAIL: Home "+menu+" accessibility bounds invalid")
    ax,ay,bx,by=map(int,parent.groups())
    cx,cy,dx,dy=map(int,child.groups())
    if not (ax<=cx<dx<=bx and ay<=cy<dy<=by):
        raise SystemExit("FAIL: Home "+menu+" text not inside its real button")
for bad in ("PROFILE","PROFIL","CLOUD SAVE","RESTORE PURCHASES"):
    if any(bad in label(n) for n in click):raise SystemExit("FAIL: unsupported Home route: "+bad)
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",play[0].get("bounds",""))
if not m:raise SystemExit("FAIL: PLAY bounds unavailable")
a,b,c,d=map(int,m.groups())
if c-a<130 or d-b<40:raise SystemExit("FAIL: PLAY hit target too small")
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
print("PASS: accessible Home Play, Skins and Shop with separate Settings route",flush=True)
PY
# A successful tap on the Home PLAY button is not proof that the Flight view
# has appeared. Wait for the *actual native GameView accessibility state*.
# The earlier flow sometimes captured a second Home animation as Flight Preview.
ready=0
for attempt in $(seq 1 15); do
  adb shell pidof com.brendigo.bopavi >/dev/null || {
    echo "FAIL: BOPAVI process exited before gameplay" >&2; exit 1;
  }
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
def label(n):return (n.get("text","")+" "+n.get("content-desc","")).upper()
game=[n for n in nodes if n.get("class")=="android.view.View"
      and n.get("clickable")=="true"
      and "ŽIVOTI:" in label(n)
      and ("DODIRNI ZA ZAMAH KRILIMA" in label(n)
           or "TAP TO FLY WITH" in label(n)
           or "DODIRNI ZA LET" in label(n))]
play=[n for n in nodes if n.get("clickable")=="true"
      and ("▶  PLAY" in label(n) or "▶  IGRAJ" in label(n))]
sys.exit(0 if len(game)==1 and not play else 1)
PY
  then
    ready=1; break
  fi
  # The first immersive flight can be covered by Android's own "Got it"
  # tutorial. Dismiss only that exact system-owned overlay, then re-read
  # the hierarchy and require actual live idle GameView in the next pass.
  if acknowledge_verified_fullscreen_education; then
    echo "System fullscreen prompt dismissed; rechecking native Flight" >&2
  fi
  echo "Waiting for actual Flight idle GameView, attempt $attempt/15" >&2
  sleep 2
done
test "$ready" -eq 1 || { echo "FAIL: tapping PLAY did not reach Flight" >&2; exit 1; }
capture android-gameplay-ready
printf 'STATE: flight-idle confirmed by native GameView\n' >> qa/screenshots/state-evidence.txt

screen_size="$(adb shell wm size | tail -1 | sed 's/.*: //' | tr -d '\r')"
width="$(printf '%s' "$screen_size" | cut -dx -f1)"
height="$(printf '%s' "$screen_size" | cut -dx -f2)"
center=$((width/2))
adb shell input tap "$center" "$((height/2))"
# The debug-only first-flap capture gate holds the real active GameSimulation
# until the *second* tap. Require the accessible flying state AND native pause
# control instead of treating any changed frame as active gameplay.
flying=0
for attempt in $(seq 1 15); do
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
flight=[n for n in nodes if n.get("class")=="android.view.View"
        and n.get("clickable")=="true"
        and "DODIRNI ZA ZAMAH KRILIMA" in label(n)
        and all(token in label(n) for token in
                ("REZULTAT:","NOVČIĆI:","KRISTALI:","GORE DESNO PAUZA"))]
# SCORE and Pause are drawn as true touch targets within the same
# native GameView, not exported as a second, fake accessibility node.
sys.exit(0 if len(flight)==1 else 1)
PY
  then flying=1; break; fi
  sleep 1
done
test "$flying" -eq 1 || { echo "FAIL: active GameView score/coins/crystals/pause accessibility not found"; exit 1; }
capture android-gameplay-active
printf 'STATE: real native active Gameplay with live SCORE, local coins, crystals and Pause\n' >> qa/screenshots/state-evidence.txt
if cmp -s qa/screenshots/android-gameplay-ready.png qa/screenshots/android-gameplay-active.png; then
  echo "FAIL: active flight still duplicates idle screenshot" >&2; exit 1
fi
# The visible orange Pause button and Android system Back both open the same
# real native dialog. Exercise Back as the accessibility alternative here;
adb shell input keyevent 4
pause_ready=0
for attempt in $(seq 1 12); do
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
names=[label(n) for n in nodes if n.get("clickable")=="true"]
headings=[label(n) for n in nodes]
ok=(any("PAUZA" in n for n in headings)
    and sum("NASTAVI" in n for n in names)==1
    and sum("PONOVO" in n for n in names)==1
    and sum("IZLAZ" in n for n in names)==1)
sys.exit(0 if ok else 1)
PY
  then pause_ready=1; break; fi
  sleep 1
done
test "$pause_ready" -eq 1 || { echo "FAIL: real Pause dialog/buttons did not appear" >&2; exit 1; }
capture android-pause
printf 'STATE: pause verified by real NASTAVI/PONOVO/IZLAZ dialog\n' >> qa/screenshots/state-evidence.txt
if cmp -s qa/screenshots/android-pause.png qa/screenshots/android-gameplay-active.png; then
  echo "FAIL: Pause screenshot duplicates active gameplay" >&2; exit 1
fi
python3 - qa/screenshots/android-current-ui.xml <<'PY'
import re,subprocess,sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
continue_buttons=[n for n in nodes if n.get("clickable")=="true" and "NASTAVI" in label(n)]
assert len(continue_buttons)==1,"FAIL: no unique Continue control in Pause"
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",continue_buttons[0].get("bounds",""))
assert m,"FAIL: Continue tap bounds missing"
x1,y1,x2,y2=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((x1+x2)//2),str((y1+y2)//2)],check=True)
print("PASS: real Pause Continue action pressed",flush=True)
PY
# Dismissing an Android AlertDialog is asynchronous. A tap sent immediately
# after NASTAVI can be consumed by the still-attached modal window instead
# of GameView, leaving the deliberate first-flap screenshot hold frozen.
# Wait for the original live flight view (not a newly created preview).
flight_resumed=0
for attempt in $(seq 1 14); do
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
click=[label(n) for n in nodes if n.get("clickable")=="true"]
active=any(n.get("class")=="android.view.View" and
           all(token in label(n) for token in
               ("REZULTAT:","NOVČIĆI:","KRISTALI:","GORE DESNO PAUZA"))
           for n in nodes)
modal=any("NASTAVI" in s for s in click)
sys.exit(0 if active and not modal else 1)
PY
  then flight_resumed=1; break; fi
  sleep 1
done
test "$flight_resumed" -eq 1 || {
  echo "FAIL: Continue did not return to the actual live flight window" >&2; exit 1;
}
printf 'STATE: Pause Continue returned to native flight HUD\n' >> qa/screenshots/state-evidence.txt
# The debug-only first-flap still capture now releases its hold on second tap.
adb shell input tap "$center" "$((height/2))"
result_ready=0
for attempt in $(seq 1 18); do
  adb shell pidof com.brendigo.bopavi >/dev/null || { echo "FAIL: BOPAVI process exited during gameplay" >&2; exit 1; }
  if dump_ui && (grep -q 'RETRY' qa/screenshots/android-current-ui.xml || grep -q 'PONOVO' qa/screenshots/android-current-ui.xml); then
    result_ready=1; break
  fi
  sleep 2
done
test "$result_ready" -eq 1 || { echo "FAIL: real game did not expose a result after collision" >&2; exit 1; }
sleep 0.5
capture android-result
if cmp -s qa/screenshots/android-result.png qa/screenshots/android-gameplay-ready.png ||
   cmp -s qa/screenshots/android-result.png qa/screenshots/android-gameplay-active.png; then
  echo "FAIL: Results PNG duplicates active or idle gameplay" >&2; exit 1
fi
# This is the real finished simulation on an Android 941x1672 viewport.
BOPAVI_RESIZE_IN_PROGRESS=1
adb shell wm size 941x1672
adb shell wm density 320
sleep 2
capture android-game-over-reference-size
python3 - <<'PY'
import struct
from pathlib import Path
data=Path("qa/screenshots/android-game-over-reference-size.png").read_bytes()
assert data[:8]==bytes.fromhex("89504e470d0a1a0a")
assert struct.unpack(">II",data[16:24])==(941,1672)
print("PASS: native Game Over exactly 941x1672, without image scaling")
PY
adb shell wm size reset
adb shell wm density reset
BOPAVI_RESIZE_IN_PROGRESS=0
dump_ui
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
root=ET.parse("qa/screenshots/android-current-ui.xml").getroot()
nodes=list(root.iter("node"))
def label(n):return (n.get("text","")+" "+n.get("content-desc","")).upper()
labels=" ".join(label(n) for n in nodes)
# This is an actual finished endless game: food and score are true run data.
# There is no gem wallet or endless level-coin award in this simulation.
for required in ("GAME","OVER","DISTANCE","FOOD COLLECTED","SCORE"):
    if required not in labels:
        raise SystemExit("FAIL: Game Over did not expose real stat "+required)
if any(fake in labels for fake in ("GEMS COLLECTED","COLLECTED GEMS","COINS COLLECTED","CLOUD SAVE")):
    raise SystemExit("FAIL: fictional endless result reward")
if labels.count("GAME OVER STATISTICS")!=1:
    raise SystemExit("FAIL: Game Over statistics group missing or duplicated")
click=[n for n in nodes if n.get("clickable")=="true"]
retry=[n for n in click if "RETRY" in label(n) or "PONOVO" in label(n)]
home=[n for n in click if "HOME" in label(n) or "POČETNA" in label(n)]
if len(retry)!=1 or len(home)!=1:raise SystemExit("FAIL: Retry/Home result controls missing")
for bad in ("WORLDS","SVJETOVI","SHOP","TRGOVINA","LEVELS","LEVELI"):
    if any(bad in label(n) for n in click):raise SystemExit("FAIL: deleted menu exposed "+bad)
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",retry[0].get("bounds",""))
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
print("PASS: genuine loss result and Retry/Home controls",flush=True)
PY
# Verify that RETRY returns to the *new idle GameView*, not a second
# screenshot of the Results screen or Home, before accepting the image.
retry_ready=0
for attempt in $(seq 1 12); do
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
labels=" ".join((n.get("text","")+" "+n.get("content-desc","")).upper() for n in nodes)
sys.exit(0 if "DODIRNI ZA ZAMAH KRILIMA" in labels
         and "ŽIVOTI:" in labels
         and "RETRY" not in labels and "PONOVO" not in labels else 1)
PY
  then retry_ready=1; break; fi
  sleep 1
done
test "$retry_ready" -eq 1 || { echo "FAIL: RETRY did not reach idle Flight" >&2; exit 1; }
capture android-gameplay-retry
if cmp -s qa/screenshots/android-gameplay-retry.png qa/screenshots/android-result.png; then
  echo "FAIL: Retry did not launch another flight" >&2; exit 1
fi
echo "PASS: simplified real-device Home, game, result and Retry"

# Optional second-stage responsive verification. These are real emulator
# window-size/density overrides after the unchanged screenshot-release flow.
# Every assertion runs against actual native clickable controls, not mock art.
if [ "${BOPAVI_VIEWPORT_QA:-0}" = "1" ]; then
  BOPAVI_RESIZE_IN_PROGRESS=1
  for spec in 480x800:160:small 600x960:240:short 720x1280:320:compact 941x1672:320:reference 1080x1920:420:standard 1080x2400:420:tall 1440x2960:560:large; do
    dims="$(printf '%s' "$spec" | cut -d: -f1)"
    density="$(printf '%s' "$spec" | cut -d: -f2)"
    label="$(printf '%s' "$spec" | cut -d: -f3)"
    adb shell wm size "$dims"
    adb shell wm density "$density"
    adb shell am force-stop com.brendigo.bopavi
    adb shell am start -W -n com.brendigo.bopavi/.MainActivity
    sleep 4
    adb shell pidof com.brendigo.bopavi >/dev/null || {
      echo "FAIL: BOPAVI exited at $dims/$density" >&2; exit 1;
    }
    dump_ui
    capture "android-home-$label"
    # Compact Home uses a native ScrollView: verify every action AFTER a
    # real finger swipe rather than wrongly requiring all actions on page 1.
    # The helper rejects missing/duplicate, clipped and <48dp hit targets.
    python3 tools/qa_home_viewports.py "$dims" "$density" "$label"
    # Capture the same native Splash/Loading View used in production,
    # held via the debug-only QA flag so no invented progress percentage
    # or timed startup animation is needed for a deterministic screenshot.
    adb shell am force-stop com.brendigo.bopavi
    adb shell am start -W -n com.brendigo.bopavi/.MainActivity \
      --ez bopavi_qa_loading_preview true
    sleep 2
    adb shell pidof com.brendigo.bopavi >/dev/null || {
      echo "FAIL: native Loading disappeared at $dims/$density" >&2; exit 1;
    }
    capture "android-loading-$label"
    python3 tools/qa_splash_viewports.py "$dims" "$density" "$label"
  done
  adb shell wm size reset
  adb shell wm density reset
  BOPAVI_RESIZE_IN_PROGRESS=0
fi

# Actual dynamic text scaling on compact and tall Android displays.
# Keep the release screenshot flow unchanged; these additional emulator
# states validate native touch geometry and scroll reachability only.
if [ "${BOPAVI_VIEWPORT_QA:-0}" = "1" ]; then
  python3 tools/qa_android_fontscale.py
fi

# Separate real-device geometry QA after the original screenshot/gameplay flow.
# Verify landscape Home and a flight that survives rotation without a restart.
if [ "${BOPAVI_VIEWPORT_QA:-0}" = "1" ]; then
  python3 tools/qa_android_reference_menus.py
  python3 tools/qa_android_pause_reference.py
  python3 tools/qa_android_background.py
  python3 tools/qa_android_rotation.py
  # Exact native Loading view used by real asynchronous level preparation.
  python3 tools/qa_android_loading.py
fi

# Matched-pixel source reference capture. This does not claim visual equality:
# it records the REAL native GameView with exactly the owner's 941x1672 viewport,
# so subsequent region-by-region differences can be measured without resizing.
# Full app interaction checks above have already completed successfully.
if [ "${BOPAVI_VIEWPORT_QA:-0}" = "1" ]; then
  BOPAVI_RESIZE_IN_PROGRESS=1
  adb shell wm size 941x1672
  adb shell wm density 320
  adb shell am force-stop com.brendigo.bopavi
  adb shell am start -W -n com.brendigo.bopavi/.MainActivity --ez bopavi_qa_capture_first_flap true
  sleep 4
  reference_home=0
  for attempt in $(seq 1 12); do
    if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import re,subprocess,sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
play=[n for n in nodes if n.get("clickable")=="true" and
      ("▶  PLAY" in label(n) or "▶  IGRAJ" in label(n))]
if len(play)!=1:sys.exit(1)
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",play[0].get("bounds",""))
if not m:sys.exit(1)
x1,y1,x2,y2=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((x1+x2)//2),str((y1+y2)//2)],check=True)
PY
    then reference_home=1; break; fi
    sleep 2
  done
  test "$reference_home" -eq 1 || { echo "FAIL: reference-size Home PLAY was not accessible" >&2; exit 1; }
  reference_ready=0
  for attempt in $(seq 1 15); do
    if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
ready=[n for n in nodes if n.get("class")=="android.view.View" and
       n.get("clickable")=="true" and
       "ŽIVOTI:" in label(n) and
       "DODIRNI ZA ZAMAH KRILIMA" in label(n)]
sys.exit(0 if len(ready)==1 else 1)
PY
    then reference_ready=1; break; fi
    sleep 2
  done
  test "$reference_ready" -eq 1 || { echo "FAIL: reference-size gameplay did not appear" >&2; exit 1; }
  capture android-gameplay-ready-reference-size
  # The debug-only first-flap hold freezes the real ACTIVE simulation (no
  # synthetic player, score, pickups or mock screen), preserving a deterministic
  # frame while slow CI screenshots are captured.
  adb shell input tap 470 800
  sleep 1
  test -n "$(adb shell pidof com.brendigo.bopavi)" || {
    echo "FAIL: real GameView crashed after first flap at reference resolution" >&2
    exit 1
  }
  capture android-gameplay-active-reference-size
  if cmp -s qa/screenshots/android-gameplay-ready-reference-size.png \
            qa/screenshots/android-gameplay-active-reference-size.png; then
    echo "FAIL: active flight identical to idle; first flap/tutorial transition missing" >&2
    exit 1
  fi
  python3 - <<'PY'
import struct
from pathlib import Path
for name in ("android-gameplay-ready-reference-size.png",
             "android-gameplay-active-reference-size.png"):
    shot=(Path("qa/screenshots")/name).read_bytes()
    assert shot[:8]==bytes.fromhex("89504e470d0a1a0a")
    assert struct.unpack(">II",shot[16:24])==(941,1672),name+" dimensions mismatch"
print("PASS: real idle AND active gameplay at 941x1672, distinct unscaled frames")
PY
  # Read actual non-resized emulator pixels. The lower hazard strip must
  # visibly change only after the first genuine flap, independent of the
  # disappearing tutorial prompt or animated Bopi near the centre of screen.
  python3 tools/qa_first_flap_hazards.py \
    qa/screenshots/android-gameplay-ready-reference-size.png \
    qa/screenshots/android-gameplay-active-reference-size.png
  printf 'STATE: real flight-idle AND first-flap active at 941x1672 captured\n' >> qa/screenshots/state-evidence.txt
  adb shell wm size reset
  adb shell wm density reset
  BOPAVI_RESIZE_IN_PROGRESS=0
fi

# Owner's two actual 864x1536 references require ORIGINAL Android pixels
# at that resolution. Do not resize screenshots offline to claim zero error.
# This is separate from seven established viewport smoke-test profiles.
if [ "${BOPAVI_VIEWPORT_QA:-0}" = "1" ]; then
  BOPAVI_RESIZE_IN_PROGRESS=1
  adb shell wm size 864x1536
  adb shell wm density 320
  adb shell am force-stop com.brendigo.bopavi
  adb shell am start -W -n com.brendigo.bopavi/.MainActivity \
    --ez bopavi_qa_capture_first_flap true
  sleep 3
  dump_ui
  python3 - qa/screenshots/android-current-ui.xml <<'PY_OWNER_HOME'
import sys, xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
accessible={n.get("content-desc","") for n in nodes
            if n.get("clickable")=="true"}
assert "▶  PLAY" in accessible and "Otvori opcije igre" in accessible, \
    "Owner-size Home has no native Play/Settings controls"
PY_OWNER_HOME
  capture android-home-owner-native-864
  adb shell am force-stop com.brendigo.bopavi
  adb shell am start -W -n com.brendigo.bopavi/.MainActivity \
    --ez bopavi_qa_loading_preview true
  sleep 2
  dump_ui
  python3 - qa/screenshots/android-current-ui.xml <<'PY_OWNER_SPLASH'
import sys, xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
assert any(n.get("content-desc")=="Loading level" for n in nodes), \
    "Owner-size Splash not present as a real native Loading view"
PY_OWNER_SPLASH
  capture android-loading-owner-native-864
  python3 - <<'PY_OWNER_PIXELS'
from pathlib import Path
import struct
for name in ("android-home-owner-native-864.png",
             "android-loading-owner-native-864.png"):
    data=(Path("qa/screenshots")/name).read_bytes()
    assert data[:8]==bytes.fromhex("89504e470d0a1a0a")
    assert struct.unpack(">II",data[16:24])==(864,1536),name
print("PASS: actual owner-size Home and Splash Android PNGs are 864x1536")
PY_OWNER_PIXELS
  adb shell wm size reset
  adb shell wm density reset
  BOPAVI_RESIZE_IN_PROGRESS=0
fi
