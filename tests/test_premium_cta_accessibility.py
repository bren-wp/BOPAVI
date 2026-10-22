#!/usr/bin/env python3
"""Check actual Android CTA gradient contrast and retained flight TalkBack text.

Source contract only: does not substitute for emulator or manual accessibility QA.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
source = root / "android/app/src/main/java/com/brendigo/bopavi"
activity = (source / "MainActivity.kt").read_text(encoding="utf-8")
view = (source / "GameView.kt").read_text(encoding="utf-8")

ink_match = re.search(r"private val primaryButtonInk = (0x[0-9a-fA-F]{8})\.toInt\(\)", activity)
assert ink_match, "Primary label ink must be an explicit opaque ARGB colour"
ink = int(ink_match.group(1), 16)

action = activity.split("private fun action(", 1)[1].split("private fun backAction(", 1)[0]
pause = activity.split("private fun showPremiumPause(", 1)[1].split("private fun startGame(){", 1)[0]
assert "setTextColor(if(primary)primaryButtonInk else Color.WHITE)" in action
assert "setTextColor(if(style==0)primaryButtonInk else Color.WHITE)" in pause
assert "if(!primary) setShadowLayer(" in action

gold_action = action.split("val face=if(primary) GradientDrawable(", 1)[1].split("} else if(muted)", 1)[0]
gold_pause = pause.split("0 -> gradient(", 1)[1].split("1 -> gradient(", 1)[0]
action_stops = [int(v, 16) for v in re.findall(r"0x[0-9a-fA-F]{8}", gold_action)[:3]]
pause_stops = [int(v, 16) for v in re.findall(r"0x[0-9a-fA-F]{8}", gold_pause)[:2]]
assert len(action_stops) == 3 and len(pause_stops) == 2

def channel(value: int) -> float:
    c = value / 255
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4

def luminance(argb: int) -> float:
    return sum(w * channel((argb >> shift) & 0xFF)
               for w, shift in ((0.2126, 16), (0.7152, 8), (0.0722, 0)))

def contrast(a: int, b: int) -> float:
    bright, dark = sorted((luminance(a), luminance(b)), reverse=True)
    return (bright + 0.05) / (dark + 0.05)

for stop in action_stops + pause_stops:
    assert (stop >> 24) == 255, "Gradient stops must be opaque for reliable contrast"
    ratio = contrast(ink, stop)
    assert ratio >= 4.5, f"Gold CTA stop {stop:#010x} fails WCAG AA: {ratio:.2f}:1"

assert 'private fun flightHint():String' in view
assert '"Bopi leti. Dodirni za zamah krilima."' in view
assert 'else "$pilotName leti. Dodirni za zamah krilima."' in view
assert 'lastHudAccessibility="${flightHint()} Bodovi: $score; Hrana: $food; Životi: ${game.lives}"' in view
assert "contentDescription=lastHudAccessibility.ifEmpty { flightHint() }" in view
assert 'contentDescription=if(selectedSkin==0)"Bopi leti"' not in view

qa = (root / "tools/qa_android_emulator.sh").read_text(encoding="utf-8")
assert '"DODIRNI ZA ZAMAH KRILIMA" in label(n)' in qa
print("PASS: both gold CTA gradients exceed 4.5:1 and flap keeps Croatian TalkBack instructions")
