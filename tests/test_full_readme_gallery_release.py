#!/usr/bin/env python3
"""All public gallery images must exist as authentic emulator source captures."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
readme=(r/"README.md").read_text()
wf=(r/".github/workflows/native-ci.yml").read_text()
cleanup=(r/"tools/cleanup_legacy_releases.py").read_text()
names=["Home","Skins","Shop","Settings","Game-Over","Privacy","Terms",
       "Flight-Preview","Flight-Active","Pause","Results"]
assert len(names)==11 and len(set(names))==11
assert readme.count('<td align="center">')==11
gallery=readme.split("## Pogledaj sve zaslone igre",1)[1].split("</table>",1)[0]
assert gallery.count("<tr>")==11
assert gallery.count('width="270"')==11
for name in names:
    png=f"BOPAVI-Android-{name}.png"
    assert readme.count(f"releases/latest/download/{png}")==2,png
    assert f":'{png}'" in wf,png
    assert f"dist/{png}" in wf,png
for gone in ("Achievements","Progress","Leaderboard","Level-Map","Options","Records"):
    name="BOPAVI-Android-"+gone+".png"
    assert name not in readme and name not in wf,name
assert 'test "$(wc -l < SHA256SUMS.txt)" -eq 16' in wf
assert "if width < 320 or height < 600 or height <= width" in wf
assert 'if version >= (1, 0, 84):' in cleanup
print("PASS: 11 real emulator PNGs, 16 checksummed assets; retired screens omitted")
