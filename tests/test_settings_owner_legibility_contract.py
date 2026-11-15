#!/usr/bin/env python3
"""Settings owner reference: increase readable real text/icon, retain every row."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
s=(r/"android/app/src/main/java/com/brendigo/bopavi/SettingsScreen.kt").read_text()
assert 'title(text,if(text=="Mode")13 else 16,navy)' in s
assert 'setAutoSizeTextTypeUniformWithConfiguration(11,16,1,' in s
assert 'BopaviUiIconDrawable(kind,Color.WHITE,dp(30))' in s
assert 'LinearLayout.LayoutParams(dp(34),dp(34))' in s
assert 'SettingsReferenceLayout.rows[index]' in s
for marker in ('onMusic','onEffects','onSound','onHaptic','onMotion','onDifficulty',
 'onPrivacy','onTerms','onRestore','onCloud','onNotifications',
 'onLanguage','onControls','SettingsLegalArrow('):
 assert marker in s,marker
assert 'setOnCheckedChangeListener{_,checked->onChange(checked)}' in s
assert 'bar?.progress?.let(::save)' in s
print("PASS: Settings native labels/icons enlarged; all callbacks preserved")
