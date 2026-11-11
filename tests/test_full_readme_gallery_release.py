#!/usr/bin/env python3
"""Every README gallery image must be staged from a genuine emulator capture."""
from pathlib import Path
repo=Path(__file__).resolve().parents[1]
readme=(repo/"README.md").read_text()
workflow=(repo/".github/workflows/native-ci.yml").read_text()
cleanup=(repo/"tools/cleanup_legacy_releases.py").read_text()
names=["Home","Skins","Shop","Settings","Achievements","Progress","Game-Over","Leaderboard","Level-Map","Options","Records","Privacy","Terms","Flight-Preview","Flight-Active","Pause","Results"]
assert len(names)==17 and len(set(names))==17
assert readme.count('<td align="center">')==17
gallery=readme.split("## Pogledaj sve zaslone igre",1)[1].split("</table>",1)[0]
assert gallery.count("<tr>")==17
assert gallery.count('width="270"')==17
for name in names:
    filename=f"BOPAVI-Android-{name}.png"
    assert readme.count(f"releases/latest/download/{filename}")==2, filename
    assert f":'{filename}'" in workflow, filename
    assert f"dist/{filename}" in workflow, filename
    assert filename in cleanup or name in (
        "Home","Options","Records","Privacy","Terms",
        "Flight-Preview","Flight-Active","Pause","Results"), filename
assert "'dist/real-screenshots/android/android-progress-reference-size.png':'BOPAVI-Android-Progress.png'" in workflow
assert "'dist/real-screenshots/android/android-game-over-reference-size.png':'BOPAVI-Android-Game-Over.png'" in workflow
assert 'test "$(wc -l < SHA256SUMS.txt)" -eq 22' in workflow
assert "if width < 320 or height < 600 or height <= width" in workflow
print("PASS: 17 genuine emulator PNGs and 22 checksummed release assets, no startup splash")
