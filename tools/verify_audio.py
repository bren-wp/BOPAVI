#!/usr/bin/env python3
"""Validate the generated, original BOPAVI native gameplay soundtrack."""
from pathlib import Path
import math
import struct
import wave

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/"android/app/src/main/res/raw"
RATE=11025
for name in [f"world_{n}" for n in range(8)]:
    left=A/(name+".wav")
    assert left.is_file(), name
    with wave.open(str(left)) as sound:
        assert sound.getnchannels()==1 and sound.getsampwidth()==2
        assert sound.getframerate()==RATE, name
        assert sound.getnframes()==48*RATE, (name,sound.getnframes())
        # Check silence, clipping, abrupt cut and overly loud loops.
        frames=sound.readframes(sound.getnframes())
    data=memoryview(frames).cast("h")
    head=[int(x) for x in data[:512]]
    tail=[int(x) for x in data[-512:]]
    peak=max(abs(v) for v in data[::103])
    assert 100<peak<15000,(name,peak)
    # A legitimate oscillator crossing can move by hundreds of PCM counts
    # in one 11 kHz sample. Compare the seam against normal local slew,
    # rather than rejecting a seamless sine at a steep zero crossing.
    local_step=max(abs(int(data[i])-int(data[i-1])) for i in range(1,4096))
    seam_step=abs(int(data[0])-int(data[-1]))
    assert seam_step <= 1.5*local_step+32,(name,"audible loop click",seam_step,local_step)
    rms=math.sqrt(sum(v*v for v in data[::113])/len(data[::113]))/32768.0
    assert 0.005<rms<0.18,(name,rms)
for name in ("tap","collect","level","hit","click"):
    left=A/(name+".wav")
    with wave.open(str(left)) as sound:
        assert 0<sound.getnframes()<RATE*2, name
print("PASS: eight 48-second low-volume seam-safe native music loops, and 5 verified Android SFX")
