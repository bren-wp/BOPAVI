#!/usr/bin/env python3
"""Legal screen follows owner's native 3D panel without losing legal rights."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
main=(src/"MainActivity.kt").read_text()
panel=(src/"BopaviLegalDialog.kt").read_text()
for phrase in (
    'assets.open("legal/$file")',
    'android.text.Html.fromHtml(markup,android.text.Html.FROM_HTML_MODE_COMPACT)',
    'android.widget.ScrollView(this)',
    'movementMethod=android.text.method.LinkMovementMethod.getInstance()',
    'linksClickable=true',
    'BopaviLegalDialog.show(this,title,scrolling)',
    'showLegalNotice("Politika privatnosti","privacy.html")',
    'showLegalNotice("Uvjeti korištenja","terms.html")'
):
    assert phrase in main,phrase
for phrase in (
    'dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)',
    'Bopavi3DTheme.Finish.SKY',
    'Bopavi3DTheme.Finish.GREEN',
    'setOnClickListener{dialog.dismiss()}',
    'scrolling.background=parchment',
    'root.addView(scrolling,LinearLayout.LayoutParams(-1,0,1f)',
    'dialog.window?.setLayout(width,height)',
    'setCancelable(true)'
):
    assert phrase in panel,phrase
assert "R.drawable.bopavi_owner_privacy_screenshot" not in panel
assert "ImageView" not in panel
print("PASS: legal HTML, links, native scrolling and functioning 3D dialog CTA")
