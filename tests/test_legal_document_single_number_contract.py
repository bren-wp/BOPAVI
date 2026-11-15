#!/usr/bin/env python3
"""Offline legal dialog must show document number exactly once from real HTML."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
p=r/"android/app/src/main/java/com/brendigo/bopavi"
dlg=(p/"BopaviLegalDialog.kt").read_text()
main=(p/"MainActivity.kt").read_text()
for f,label in (("privacy.html","Dokument 01"),("terms.html","Dokument 02")):
 html=(r/"android/app/src/main/assets/legal"/f).read_text()
 assert html.count('class="section-label">'+label)==1  # Terms also has 'Službeni kontakt'.
 assert label in html
assert "val docNumber=TextView(host)" not in dlg
assert "root.addView(docNumber," not in dlg
assert "scrolling.background=parchment" in dlg
assert "root.addView(scrolling,LinearLayout.LayoutParams(-1,0,1f)" in dlg
assert "setOnClickListener{dialog.dismiss()}" in dlg
assert 'movementMethod=android.text.method.LinkMovementMethod.getInstance()' in main
assert 'assets.open("legal/$file")' in main
print("PASS: one legal document number per genuine offline HTML; native links and close preserved")
