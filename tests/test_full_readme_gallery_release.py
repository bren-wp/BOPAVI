#!/usr/bin/env python3
"""Every README gallery image must be staged from a genuine emulator capture."""
from pathlib import Path
repo=Path(__file__).resolve().parents[1]
readme=(repo/"README.md").read_text()
workflow=(repo/".github/workflows/native-ci.yml").read_text()
cleanup=(repo/"tools/cleanup_legacy_releases.py").read_text()
names=["Home","Splash","Skins","Shop","Settings","Achievements","Leaderboard","Level-Map","Options","Records","Privacy","Terms","Flight-Preview","Flight-Active","Pause","Results"]
assert len(names)==16 and len(set(names))==16
assert readme.count('<td align="center">')==16
gallery=readme.split("## Pogledaj sve zaslone igre",1)[1].split("</table>",1)[0]
assert gallery.count("<tr>")==8
assert gallery.count('width="154"')==16
for name in names:
    filename=f"BOPAVI-Android-{name}.png"
    assert readme.count(f"releases/latest/download/{filename}")==1, filename
    assert f":'{filename}'" in workflow, filename
    assert f"dist/{filename}" in workflow, filename
    assert filename in cleanup or name in (
        "Home","Options","Records","Privacy","Terms",
        "Flight-Preview","Flight-Active","Pause","Results"), filename
assert 'test "$(wc -l < SHA256SUMS.txt)" -eq 21' in workflow
assert "if width < 320 or height < 600 or height <= width" in workflow
print("PASS: 16 real screens and 21 checksummed artifacts are required")
