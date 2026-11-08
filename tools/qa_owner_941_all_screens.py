#!/usr/bin/env python3
"""Strict unscaled 941x1672 owner-vs-Android screenshot audit.

All nine original owner references are pinned by SHA-256. Pixels are compared
without resizing, masking, color correction or tolerances. A separate Android
screen-state / UI hierarchy record is mandatory proof of real functionality:
pixel equality alone could be a noninteractive screenshot.
"""
from __future__ import annotations
import argparse
import hashlib
import json
from pathlib import Path
from PIL import Image, ImageChops, ImageStat

SIZE=(941,1672)
OWNER_SHA256={
    'Splash':'cce13679bafb2d18410d35a56beef2cbfb249a8e636c356eed824e90778eb3e8',
    'Home':'e83c8f214c7dcee6fa87db737bfd412cb80cc37a8e73b5535bacf8b146617803',
    'Gameplay':'b80164e67da0e1b56e5b05d2f80fb383721d1a812dca5407255793a4de1ded08',
    'LevelMap':'f648d884b89d8c74049dd2f91841896dc7e931ed381773b5a8dc3e2566448c2a',
    'Shop':'9e7b6f8aebe0a129c1a048459d3f24f58c7fb9c82d797bb12d3fd418305d21d8',
    'Skins':'1c0585aff3c9374b6afe3288358c7b1bec814947409ddb91019d104e1c27dd2a',
    'Settings':'37c2ef39d3f5fd09b5bef33ef1ef6242f316c54dd96984a742d697988349b5ff',
    'LevelComplete':'da8018acdc67822fae5b29da710ab82414a38405f4a2b51400532d5cc29882f3',
    'GameOver':'0fd6b82f6d54564a65eb7f44f0b4ac3027639cba406832c3b49edc484ae0a3df',
}
REGIONS={
    'Splash':{
        'logo':(95,90,855,398),'hero':(85,390,820,885),
        'rings':(0,825,800,1260),'loading':(105,1300,840,1508),
        'left_world':(0,100,120,1290),'right_world':(820,100,941,1290),
    },
    'Home':{
        'wallets':(445,8,930,105),'logo':(100,98,840,390),
        'hero':(100,385,810,886),'play':(100,881,840,1080),
        'skins_shop':(119,1080,830,1250),
        'settings':(215,1220,722,1400),
        'footer':(180,1420,770,1630),
        'left_world':(0,100,120,1250),
        'right_world':(830,100,941,1260),
    },
}

def png_rgb(path:Path)->Image.Image:
    with Image.open(path) as image:
        if image.format!='PNG' or image.size!=SIZE:
            raise ValueError(f'{path}: original unscaled PNG must be {SIZE[0]}x{SIZE[1]}')
        return image.convert('RGB')

def mean_rgb(diff:Image.Image)->float:
    return sum(ImageStat.Stat(diff).mean)/3.0

def pixel_report(reference:Image.Image,capture:Image.Image,
                 screen:str)->dict:
    if reference.size!=SIZE or capture.size!=SIZE:
        raise ValueError(f'{screen}: non-native pixel dimensions')
    difference=ImageChops.difference(reference,capture)
    r,g,b=difference.split()
    # Count a pixel changed if ANY color channel differs, even by one unit.
    changed=SIZE[0]*SIZE[1]-ImageChops.lighter(
        ImageChops.lighter(r,g),b).histogram()[0]
    mae=mean_rgb(difference)
    boxes=REGIONS.get(screen)
    if boxes is None:
        xs=(0,SIZE[0]//3,2*SIZE[0]//3,SIZE[0])
        ys=(0,SIZE[1]//3,2*SIZE[1]//3,SIZE[1])
        boxes={f'row_{j+1}_col_{i+1}':(xs[i],ys[j],xs[i+1],ys[j+1])
               for j in range(3) for i in range(3)}
    regions=[{'name':name,'rgb_mae':round(mean_rgb(difference.crop(rect)),10),
              'bounds':list(rect)} for name,rect in boxes.items()]
    regions.sort(key=lambda v:v['rgb_mae'],reverse=True)
    return {
        'total_pixels':SIZE[0]*SIZE[1], 'different_pixels':changed,
        'different_pixel_percent':round(changed/(SIZE[0]*SIZE[1])*100,4),
        'rgb_mae':round(mae,10),
        'rgb_deviation_percent':round(mae/255*100,10),
        'exact_rgb_match':changed==0,
        'regions_by_deviation':regions,
    }

def compare(screen:str,reference_path:Path,capture_path:Path)->dict:
    if screen not in OWNER_SHA256:
        raise ValueError(f'Unsupported screen {screen}; expected {sorted(OWNER_SHA256)}')
    digest=hashlib.sha256(reference_path.read_bytes()).hexdigest()
    if digest!=OWNER_SHA256[screen]:
        raise ValueError(f'{screen}: source image SHA-256 mismatch; '
                         'use the original unmodified owner upload')
    source=png_rgb(reference_path)
    actual=png_rgb(capture_path)
    return {
        'screen':screen,
        'reference_sha256':digest,
        'capture_sha256':hashlib.sha256(capture_path.read_bytes()).hexdigest(),
        **pixel_report(source,actual,screen),
    }

def main()->int:
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--pair',action='append',nargs=3,
                        metavar=('SCREEN','REFERENCE_PNG','ANDROID_PNG'),required=True)
    parser.add_argument('--report',type=Path)
    parser.add_argument('--enforce-exact',action='store_true')
    parser.add_argument('--require-all',action='store_true')
    args=parser.parse_args()
    seen=set()
    reports=[]
    for screen,reference,actual in args.pair:
        if screen in seen:parser.error(f'Duplicate screen {screen}')
        seen.add(screen)
        try:reports.append(compare(screen,Path(reference),Path(actual)))
        except (ValueError,OSError) as error:parser.error(str(error))
    missing=sorted(set(OWNER_SHA256)-seen)
    result={
        'source':'Nine owner-provided PNG references; each 941x1672, pinned SHA-256',
        'measurement':'Unscaled 8-bit RGB; zero only when all pixels match exactly',
        'capture_warning':'Pixel equality is NOT proof of an interactive APK. '
            'Verify native taps, Accessibility hierarchy, process state, and '
            'screen provenance separately. Dynamic wallets, animation phases '
            'and game state must be aligned for visual comparisons.',
        'screens':reports,
        'missing_screens':missing,
        'all_selected_exact':all(x['exact_rgb_match'] for x in reports),
        'all_nine_exact':not missing and all(x['exact_rgb_match'] for x in reports),
    }
    output=json.dumps(result,indent=2,ensure_ascii=False)+'\n'
    if args.report:
        args.report.parent.mkdir(parents=True,exist_ok=True)
        args.report.write_text(output,encoding='utf-8')
    print(output,end='')
    if args.require_all and missing:return 1
    if args.enforce_exact and not result['all_selected_exact']:return 1
    return 0

if __name__=='__main__':raise SystemExit(main())
