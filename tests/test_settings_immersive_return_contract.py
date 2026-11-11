#!/usr/bin/env python3
"""Regression contract: Android OS tutorial cannot mask Settings -> Home."""
from pathlib import Path
shell=(Path(__file__).resolve().parents[1]/"tools/qa_android_emulator.sh").read_text()
assert 'return_home_from_settings(){' in shell
assert 'android:id/immersive_cling_title' in shell
assert 'android:id/ok' in shell
assert 'n.get("package")=="android"' in shell
assert 'dismissed exact Android immersive-mode education dialog' in shell
assert 'Settings native HOME button did not return Home' in shell
assert 'legal_close_attempt=1' in shell
assert 'n.get("resource-id")=="android:id/alertTitle"' in shell
assert 'n.get("resource-id")=="android:id/button1"' in shell
assert 'n.get("text")=="ZATVORI"' in shell
assert 'retried verified legal dialog close' in shell
assert 'while [ "$legal_close_attempt" -le 3 ]' in shell
# Both the legal dialog's scroll and the accessible Settings page's scroll
# are real native widgets. The title/HOME check must first return to the
# actual top of the app; never accept a launcher or a hidden fake button.
assert 'PY_SCROLL_TO_SETTINGS_TOP' in shell
assert 'Settings top unreachable; actual app ScrollView absent' in shell
assert 'Settings header and native HOME are visible' in shell
assert 'after 8 upward scrolls' in shell
# Full owner Settings checks must visit offscreen rows on real compact
# Android ScrollView, not assume every option is in the first hierarchy dump.
assert 'def scroll_settings(nodes,direction):' in shell
assert 'def match(nodes,label,clickable):' in shell
assert 'native Settings item absent after real scrolling' in shell
assert 'n.get("package")=="com.brendigo.bopavi"' in shell
assert 'for direction in ("down","up"):' in shell
assert 'and n.get("content-desc")=="HOME"' in shell
assert 'and n.get("class")=="android.widget.ScrollView"' in shell
assert 'PY_LEGAL_STILL_OPEN' in shell
assert 'adb shell input keyevent KEYCODE_BACK' in shell
assert 'dismissed verified native legal modal by Android Back' in shell
assert shell.count('if acknowledge_verified_fullscreen_education; then')>=3
print("PASS: Settings -> Home handles only the exact Android OS immersive tutorial")
