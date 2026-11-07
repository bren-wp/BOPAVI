#!/usr/bin/env python3
"""Detect lower spikes appearing only after real first flap on Android.

Reads original non-resized 941x1672 PNG emulator captures with standard
library only. The central bottom strip has no tutorial/avatar/HUD, and
must transition from ordinary scenery to live 3D solid-floor spikes.
Not a claim of overall pixel-perfect 1:1 matching.
"""
from pathlib import Path
import struct
import sys
import zlib

WIDTH, HEIGHT = 941, 1672
ROI = (210, 1535, 830, 1615)


def paeth(a, b, c):
    p = a + b - c
    da, db, dc = abs(p-a), abs(p-b), abs(p-c)
    return a if da <= db and da <= dc else b if db <= dc else c


def read_rgb_crop(path):
    raw = Path(path).read_bytes()
    if not raw.startswith(b'\x89PNG\r\n\x1a\n'):
        raise ValueError(f'{path}: not a PNG')
    offset, idat, spec = 8, bytearray(), None
    while offset + 12 <= len(raw):
        n = struct.unpack_from('>I', raw, offset)[0]
        kind = raw[offset+4:offset+8]
        data = raw[offset+8:offset+8+n]
        if kind == b'IHDR':
            spec = struct.unpack('>IIBBBBB', data)
        elif kind == b'IDAT':
            idat.extend(data)
        elif kind == b'IEND':
            break
        offset += 12+n
    if spec is None:
        raise ValueError(f'{path}: missing IHDR')
    width,height,depth,ctype,compression,filter_method,interlace=spec
    if (width,height)!=(WIDTH,HEIGHT) or depth!=8 or ctype not in (2,6) or interlace:
        raise ValueError(f'{path}: unsupported PNG screenshot format {spec}')
    if compression or filter_method:
        raise ValueError(f'{path}: invalid PNG compression/filtering')
    bpp=4 if ctype==6 else 3
    stride=width*bpp
    source=zlib.decompress(idat)
    if len(source)!=(stride+1)*height:
        raise ValueError(f'{path}: damaged decoded raster length')
    previous=bytearray(stride)
    out=bytearray()
    pos=0
    left,top,right,bottom=ROI
    for y in range(height):
        mode=source[pos];pos+=1
        row=bytearray(source[pos:pos+stride]);pos+=stride
        if mode==1:
            for i in range(bpp,stride):
                row[i]=(row[i]+row[i-bpp])&255
        elif mode==2:
            for i in range(stride):
                row[i]=(row[i]+previous[i])&255
        elif mode==3:
            for i in range(stride):
                row[i]=(row[i]+((row[i-bpp] if i>=bpp else 0)+previous[i])//2)&255
        elif mode==4:
            for i in range(stride):
                row[i]=(row[i]+paeth(row[i-bpp] if i>=bpp else 0,
                    previous[i],previous[i-bpp] if i>=bpp else 0))&255
        elif mode!=0:
            raise ValueError(f'{path}: unknown PNG filter {mode}')
        if top<=y<bottom:
            for x in range(left,right):
                i=x*bpp
                out.extend(row[i:i+3])
        previous=row
    return out


def validate(ready_path, active_path):
    idle,active=read_rgb_crop(ready_path),read_rgb_crop(active_path)
    expected=(ROI[2]-ROI[0])*(ROI[3]-ROI[1])*3
    if len(idle)!=expected or len(active)!=expected:
        raise ValueError('Incorrect screenshot ROI payload')
    changed=sum(1 for i in range(0,expected,3)
                if max(abs(idle[i+j]-active[i+j]) for j in range(3))>=36)
    fraction=changed/(expected//3)
    if fraction<.06:
        raise AssertionError(
            f'FAIL: floor-hazard region changed only {fraction:.1%} '
            'after first flap. Spikes may already be drawn on idle Gameplay '
            'or may not appear after the real gameplay start.')
    print(f'PASS: first flap reveals real floor hazards: {fraction:.1%} '
          f'of pixels changed in native ROI {ROI}')


if __name__=='__main__':
    if len(sys.argv)!=3:
        raise SystemExit('usage: qa_first_flap_hazards.py idle.png active.png')
    validate(sys.argv[1],sys.argv[2])
