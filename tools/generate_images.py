#!/usr/bin/env python3
from pathlib import Path
from PIL import Image
import io, json, cairosvg
r=Path(__file__).resolve().parents[1]
a=r/'android/app/src/main/res';b=r/'ios/BOPAVI/Assets.xcassets'
for p in [a/'drawable',a/'drawable-nodpi',b/'AppIcon.appiconset',b/'Hero.imageset',b/'Logo.imageset',b/'LaunchArt.imageset']:p.mkdir(parents=True,exist_ok=True)
icon=Image.open(io.BytesIO(cairosvg.svg2png(url=str(r/'docs/assets/icon.svg'),output_width=1024,output_height=1024))).convert('RGB')
icon.save(b/'AppIcon.appiconset/icon-1024.png',optimize=True)
icon.resize((512,512),Image.Resampling.LANCZOS).save(a/'drawable/app_icon.png',optimize=True)
brand=Image.open(io.BytesIO(cairosvg.svg2png(url=str(r/'docs/assets/logo.svg'),output_width=780))).convert('RGBA')
brand.save(a/'drawable-nodpi/logo.png',optimize=True)
brand.save(b/'Logo.imageset/logo.png',optimize=True)
hero=Image.open(io.BytesIO(cairosvg.svg2png(url=str(r/'docs/assets/hero.svg'),output_width=900))).convert('RGB')
hero.save(a/'drawable-nodpi/hero.png',optimize=True)
hero.save(b/'Hero.imageset/hero.png',optimize=True)
launch=Image.new('RGB',(900,1600),(14,79,133))
from PIL import ImageDraw
draw=ImageDraw.Draw(launch)
for y in range(1600):
    t=y/1600
    draw.line((0,y,900,y),fill=(int(13+20*t),int(110-55*t),int(191-75*t)))
launch.paste(hero,(0,340))
logoCrop=brand.copy()
logoCrop.thumbnail((740,250),Image.Resampling.LANCZOS)
launch.paste(logoCrop,((900-logoCrop.width)//2,130),logoCrop)
launch.save(a/'drawable-nodpi/splash.png',optimize=True)
launch.save(b/'LaunchArt.imageset/launch.png',optimize=True)
(b/'Contents.json').write_text(json.dumps({'info':{'author':'xcode','version':1}}))
(b/'AppIcon.appiconset/Contents.json').write_text(json.dumps({'images':[{'filename':'icon-1024.png','idiom':'universal','platform':'ios','size':'1024x1024'}],'info':{'author':'xcode','version':1}}))
(b/'Logo.imageset/Contents.json').write_text(json.dumps({'images':[{'filename':'logo.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))
(b/'LaunchArt.imageset/Contents.json').write_text(json.dumps({'images':[{'filename':'launch.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))
(b/'Hero.imageset/Contents.json').write_text(json.dumps({'images':[{'filename':'hero.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))

# Extract Bopi from the approved hero artwork, without external assets or sprite licenses.
# Keep the same body, scarf and goggles on both native platforms.
import re, colorsys
hero_markup=(r/'docs/assets/hero.svg').read_text()
bird=re.search(r'<g transform="translate\(454 336\) rotate\(-12\)">([\s\S]*?)</g>',hero_markup)
if bird is None:
    raise RuntimeError('Bird group missing from hero art')
defs=re.search(r'<defs>(.*?)</defs>',hero_markup,re.S)
if defs is None:
    raise RuntimeError('Hero gradient definitions missing')
bird_svg=('<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" '
          'viewBox="-260 -260 520 520"><defs>'+defs.group(1)+'</defs><g>'
          +bird.group(1)+'</g></svg>')
original=Image.open(io.BytesIO(cairosvg.svg2png(bytestring=bird_svg.encode(),output_width=512,output_height=512))).convert('RGBA')
# Recolor only the saturated blue feathers and body; retain goggles, beak and scarf.
palette=[None,.145,.975,.75,.46,.60]
for skin,hue in enumerate(palette):
    if hue is None:
        variant=original
    else:
        variant=original.copy()
        pixels=variant.load()
        for yy in range(variant.height):
            for xx in range(variant.width):
                red,green,blue,alpha=pixels[xx,yy]
                if alpha<12: continue
                h,s,v=colorsys.rgb_to_hsv(red/255,green/255,blue/255)
                if 0.49<=h<=0.72 and s>=0.34 and blue>red*1.16:
                    nr,ng,nb=colorsys.hsv_to_rgb(hue,s,v)
                    pixels[xx,yy]=(int(nr*255),int(ng*255),int(nb*255),alpha)
    name=f'bopi{skin}'
    variant.save(a/'drawable-nodpi'/f'{name}.png',optimize=True)
    skinset=b/(f'Bopi{skin}.imageset')
    skinset.mkdir(exist_ok=True)
    variant.save(skinset/(name+'.png'),optimize=True)
    (skinset/'Contents.json').write_text(json.dumps({'images':[{'filename':name+'.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))
from generate_worlds import generate_worlds
generate_worlds(a,b)
print('Generated native art: icons, splash, Bopi sprites and 8 world backdrops')
