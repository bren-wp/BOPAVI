#!/usr/bin/env python3
from pathlib import Path
from PIL import Image
import io, json, cairosvg
r=Path(__file__).resolve().parents[1]
a=r/'android/app/src/main/res';b=r/'ios/BOPAVI/Assets.xcassets'
for p in [a/'drawable',a/'drawable-nodpi',b/'AppIcon.appiconset',b/'Hero.imageset']:p.mkdir(parents=True,exist_ok=True)
icon=Image.open(io.BytesIO(cairosvg.svg2png(url=str(r/'docs/assets/icon.svg'),output_width=1024,output_height=1024))).convert('RGB')
icon.save(b/'AppIcon.appiconset/icon-1024.png',optimize=True)
icon.resize((512,512),Image.Resampling.LANCZOS).save(a/'drawable/app_icon.png',optimize=True)
hero=Image.open(io.BytesIO(cairosvg.svg2png(url=str(r/'docs/assets/hero.svg'),output_width=900))).convert('RGB')
hero.save(a/'drawable-nodpi/hero.png',optimize=True)
hero.save(b/'Hero.imageset/hero.png',optimize=True)
(b/'Contents.json').write_text(json.dumps({'info':{'author':'xcode','version':1}}))
(b/'AppIcon.appiconset/Contents.json').write_text(json.dumps({'images':[{'filename':'icon-1024.png','idiom':'universal','platform':'ios','size':'1024x1024'}],'info':{'author':'xcode','version':1}}))
(b/'Hero.imageset/Contents.json').write_text(json.dumps({'images':[{'filename':'hero.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))
print('Generated native assets')
