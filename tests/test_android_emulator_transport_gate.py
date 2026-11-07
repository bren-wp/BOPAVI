"""Release guard against mistaking hosted emulator ADB disconnect for app QA.

Run during source audit before any native builds; actual Android 15 emulator,
flap/coin/collision and genuine screenshots are still mandatory for release.
"""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
workflow=(root/".github/workflows/native-ci.yml").read_text()
qa=(root/"tools/qa_android_emulator.sh").read_text()
job=workflow.split("  android-smoke:\n",1)[1].split("  play-listing:\n",1)[0]

assert "id: api35_primary" in job
assert "continue-on-error: true" in job
assert "id: api35_transport_retry" in job
assert "id: api35_primary" in job.split("continue-on-error: true")[0]
assert "script: BOPAVI_VIEWPORT_QA=1 sh tools/qa_android_emulator.sh" in job
assert job.count("script: BOPAVI_VIEWPORT_QA=1 sh tools/qa_android_emulator.sh")==2
assert "hashFiles('qa/screenshots/adb-transport-offline.retry')" in job
assert "hashFiles('qa/first-emulator-attempt/adb-transport-offline.retry')" in job
assert 'if: ${{ always() }}' in job
assert 'PRIMARY: ${{ steps.api35_primary.outcome }}' in job
assert 'RETRY: ${{ steps.api35_transport_retry.outcome }}' in job
assert 'if [ "$PRIMARY" = success ]; then' in job
assert 'if [ -f qa/first-emulator-attempt/adb-transport-offline.retry ] &&' in job
assert '[ "$RETRY" = success ]; then' in job
assert 'exit 1' in job
assert "qa/first-emulator-attempt/*" not in job
assert "name: QA-Android35-ADB-transport-diagnostic" in job

# The exit hook marks only a disconnected/offline transport. It must not
# turn a live-device failure, process death or failed UI assertion into green.
assert "qa_exit_code" in qa
assert 'if [ "$qa_exit_code" -ne 0 ]; then' in qa
assert 'qa_adb_state="$(adb get-state 2>&1 || true)"' in qa
assert 'if [ "$qa_adb_state" != "device" ]; then' in qa
assert 'adb-transport-offline.retry' in qa
assert 'trap \'qa_exit_diagnostic "$?"\' 0' in qa
assert "FAIL: BOPAVI process exited during gameplay" in qa
assert "FAIL: Results PNG duplicates active or idle gameplay" in qa

# A failing UI assertion while ADB is connected must never be retried.
# One retry must complete the *whole* QA suite; no partial/flat screenshot
# from the first attempt is accepted as passing evidence.
def release_gate(primary,retry,transport_marker):
    if primary=="success":return True
    return transport_marker and retry=="success"

for primary,retry,marker,expected in [
    ("success","skipped",False,True),
    ("failure","skipped",False,False),  # genuine UI assertion
    ("failure","success",False,False),  # retry without evidence illegal
    ("failure","failure",True,False),   # twice lost ADB
    ("failure","success",True,True),    # complete second flight QA
    ("cancelled","success",False,False),
]:
    assert release_gate(primary,retry,marker)==expected

# Regression: fullscreen gameplay triggers Android's FIRST-USE immersive
# education on API 35, and API 36 AOSP launcher is Quickstep. Recover only
# their exact OS-owned UI; the real GameView/Home assertions remain mandatory.
assert 'acknowledge_verified_fullscreen_education(){' in qa
assert 'system_packages=("android","com.android.systemui")' in qa
assert 'n.get("text")=="Viewing full screen"' in qa
assert 'system_package=titles[0].get("package")' in qa
assert 'n.get("package")==system_package' in qa
assert '"To exit, swipe down from the top of your screen"' in qa
assert 'n.get("text")=="Got it"' in qa
assert 'if acknowledge_verified_fullscreen_education; then' in qa
assert '"Quickstep isn\'t responding"' in qa
assert '"com.android.launcher3"' in qa
assert '"com.google.android.apps.nexuslauncher"' in qa
assert '["System UI isn\'t responding"]' in qa
assert 'subprocess.run(["adb","shell","am","force-stop",launcher],check=True)' in qa
assert 'subprocess.run(["adb","shell","pidof","com.brendigo.bopavi"],check=True,' in qa
# Dismissing the OS overlay must never substitute for real native screen checks.
assert 'test "$ready" -eq 1 || { echo "FAIL: tapping PLAY did not reach Flight"' in qa
assert 'if [ "$home_ready" -ne 1 ]; then' in qa
assert 'assert "BOPAVI" not in titles' not in qa  # No broad ANR dismissal

print("PASS: Android 15 transport-only one-retry gate never bypasses gameplay QA")
