#!/usr/bin/env python3
"""Generate BOPAVI's original offline world music and interaction sounds from code."""
from pathlib import Path
import math, wave, struct
ROOT = Path(__file__).resolve().parents[1]
DSTS = [ROOT/'android/app/src/main/res/raw', ROOT/'ios/BOPAVI/Audio']
for d in DSTS: d.mkdir(parents=True, exist_ok=True)
SR=11025
MELODIES = [
  [60,64,67,72,67,64,62,67],  # meadow
  [62,65,69,74,72,69,65,62],  # beach
  [67,72,76,79,76,72,67,64],  # ice
  [48,51,55,60,58,55,51,48],  # lava
  [65,69,72,77,74,72,69,65],  # temple
  [55,58,62,67,62,58,55,53],  # night
  [64,67,71,76,74,71,67,64],  # crystals
  [60,67,72,79,74,67,64,60],  # space
]
def midi(n):return 440*2**((n-69)/12)
def save(name, samples):
    pcm=struct.pack('<'+'h'*len(samples),*(int(max(-1,min(1,s))*16000) for s in samples))
    for d in DSTS:
        with wave.open(str(d/(name+'.wav')),'wb') as w:
            w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes(pcm)
for k, seq in enumerate(MELODIES):
    data=[]
    step=0.26 + (k%3)*.035
    for ix,note in enumerate(seq):
        f=midi(note); count=int(SR*step)
        for j in range(count):
            t=j/SR
            fade=min(1.0,j/(SR*.02),(count-j)/(SR*.06))
            wave1=math.sin(2*math.pi*f*t)*0.42+math.sin(2*math.pi*f*2*t)*0.10
            bass=math.sin(2*math.pi*f*.5*t)*0.13
            data.append((wave1+bass)*max(.0,fade))
    save('world_'+str(k),data)
for key,notes in {'tap':[72],'collect':[79,84],'level':[72,76,79,84],'hit':[46,39],'purchase':[67,74,79],'click':[65]}.items():
    data=[]
    for note in notes:
        f=midi(note);dur=.11 if key not in ['hit','level'] else .17;count=int(SR*dur)
        for i in range(count):
            t=i/SR;fade=max(0,(1-i/count))**1.6
            data.append(fade*(.55*math.sin(2*math.pi*f*t)+.13*math.sin(2*math.pi*2*f*t)))
    save(key,data)
print('Generated 28 local audio files')
