#!/usr/bin/env python3
"""Run every registered source contract and report all failures in one CI job.

This preflight is deliberately non-short-circuiting: legacy references to
deleted screens must be enumerated and corrected, never silently ignored.
The existing per-test required CI steps remain in place as final gates.
"""
from pathlib import Path
import re
import subprocess
import sys

ROOT=Path(__file__).resolve().parents[1]
WF=(ROOT/".github/workflows/native-ci.yml").read_text()
commands=re.findall(r"^\s+- run: (python (?:tests/test_[^\n]+\.py|tools/[^\n]+\.py))\s*$",
                    WF,re.MULTILINE)
assert len(commands)>=75, f"Missing registered source contracts: {len(commands)}"
# A handful of visual contracts need the documented original backdrop built
# before their individual assertions, not a simulated or generated UI.
subprocess.run([sys.executable,"tools/paint_fantasy_backdrop.py"],cwd=ROOT,
               check=True)
failures=[]
for index,command in enumerate(commands,1):
    _,script=command.split(" ",1)
    result=subprocess.run([sys.executable,script],cwd=ROOT,text=True,
                          stdout=subprocess.PIPE,stderr=subprocess.STDOUT,
                          timeout=180)
    if result.returncode:
        print(f"FAIL ({index}/{len(commands)}): {script}\n{result.stdout[-2200:]}",
              flush=True)
        failures.append(script)
    elif index%15==0:
        print(f"PASS: {index}/{len(commands)} source QA contracts checked",
              flush=True)
print(f"Source preflight checked {len(commands)} registered commands; failures={len(failures)}")
if failures:
    for path in failures: print("FAILED:",path)
    sys.exit(1)
