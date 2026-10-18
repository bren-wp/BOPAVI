#!/usr/bin/env python3
"""Create BOPAVI's original, calm, seamless 48-second offline music loops and FX.

All notes and samples are synthesized, not imported. Both Android and iOS get
bit-identical mono PCM. Every oscillator is quantized to the 48-second loop
period; adjacent chords crossfade, including the wrap from chord 8 to chord 1.
No short repeating lead melody, beat or startling percussive loop.
"""
from array import array
from pathlib import Path
import math
import sys
import wave

ROOT = Path(__file__).resolve().parents[1]
DSTS = [ROOT / "android/app/src/main/res/raw", ROOT / "ios/BOPAVI/Audio"]
SR = 11025
LOOP_SECONDS = 48
FRAMES = SR * LOOP_SECONDS
MUSIC_NAMES = [f"world_{n}" for n in range(8)]
# Base roots and chord quality reflect each biome; all harmonies stay soft.
ROOTS = (48, 50, 53, 45, 55, 46, 51, 48)
PROGRESSION = (0, 5, 3, 7, 0, 5, 2, 7)
EFFECTS = {
    "tap": [72], "collect": [79, 84], "level": [72, 76, 79, 84],
    "hit": [46, 39], "click": [65],
}

def midi(note):
    return 440.0 * 2.0 ** ((note - 69) / 12.0)

def harmonic(note):
    # Exact integer oscillator periods make the loop endpoints continuous.
    return round(midi(note) * LOOP_SECONDS) / LOOP_SECONDS

def save(name, samples):
    pcm = array("h", (int(max(-1.0, min(1.0, s)) * 14000) for s in samples))
    if sys.byteorder != "little":
        pcm.byteswap()
    data = pcm.tobytes()
    for directory in DSTS:
        directory.mkdir(parents=True, exist_ok=True)
        with wave.open(str(directory / (name + ".wav")), "wb") as target:
            target.setnchannels(1)
            target.setsampwidth(2)
            target.setframerate(SR)
            target.writeframes(data)

def ambient(world):
    base = ROOTS[world]
    chords = []
    for shift in PROGRESSION:
        note = base + shift
        # Soft consonant triads; distant sustained harmonic pad, little upper energy.
        chords.append(tuple(harmonic(n) for n in (note, note + 7, note + 12)))
    for i in range(FRAMES):
        t = i / SR
        beat = t / 6.0
        current = int(beat) % 8
        fade = beat - int(beat)
        # Constant-power-free linear crossfade avoids loud transition accents.
        a = 1.0 - fade
        b = fade
        def pad(chord):
            low, fifth, octave = chord
            return (
                math.sin(2.0 * math.pi * low * t + 0.16) * 0.43
                + math.sin(2.0 * math.pi * fifth * t + 1.05) * 0.24
                + math.sin(2.0 * math.pi * octave * t + 2.1) * 0.09
            )
        chord_pad = a * pad(chords[current]) + b * pad(chords[(current + 1) % 8])
        # A very quiet background glow with exactly periodic breathing.
        breath = 0.92 + 0.08 * math.sin(2.0 * math.pi * t / 12.0)
        drone = 0.11 * math.sin(2.0 * math.pi * harmonic(base - 12) * t + 0.31)
        yield (chord_pad + drone) * 0.25 * breath

def effect_notes(key, notes):
    # Familiar responsive one-shot effects remain short; only background changes.
    for note in notes:
        frequency = midi(note)
        seconds = 0.17 if key in ("hit", "level") else 0.11
        count = int(SR * seconds)
        for j in range(count):
            t = j / SR
            fade = max(0.0, 1.0 - j / count) ** 1.6
            yield fade * (
                0.55 * math.sin(2.0 * math.pi * frequency * t)
                + 0.13 * math.sin(4.0 * math.pi * frequency * t)
            )

def main():
    for world, name in enumerate(MUSIC_NAMES):
        save(name, ambient(world))
    for name, notes in EFFECTS.items():
        save(name, effect_notes(name, notes))
    print("Generated 26 local audio files; eight 48-second seamless calm loops per platform")

if __name__ == "__main__":
    main()
