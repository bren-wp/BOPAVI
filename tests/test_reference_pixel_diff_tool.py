#!/usr/bin/env python3
"""Checks pixel diff math, strict original sizes and opt-in pass criteria."""
import json
from pathlib import Path
from PIL import Image
import subprocess
import sys
import tempfile

tool=Path(__file__).resolve().parents[1]/"tools/compare_owner_ui.py"
with tempfile.TemporaryDirectory() as folder:
    folder=Path(folder)
    reference=folder/"reference.png"
    same=folder/"same.png"
    different=folder/"different.png"
    invalid=folder/"invalid.png"
    Image.new("RGB",(941,1672),(10,20,30)).save(reference)
    Image.new("RGB",(941,1672),(10,20,30)).save(same)
    Image.new("RGB",(941,1672),(240,250,255)).save(different)
    Image.new("RGB",(900,1600),(10,20,30)).save(invalid)
    def invoke(path,threshold=None):
        args=[sys.executable,str(tool),"--reference",str(reference),
              "--actual",str(path)]
        if threshold is not None:args+=["--max-mae",str(threshold)]
        return subprocess.run(args,text=True,capture_output=True)
    exact=invoke(same,0)
    assert exact.returncode==0,exact.stderr
    payload=json.loads(exact.stdout)
    assert payload["exact_rgb_match"] and payload["threshold_pass"]
    assert payload["mean_rgb_abs_difference"]==0 and len(payload["regions"])==9
    failed=invoke(different,0)
    assert failed.returncode==1,failed.stderr
    assert json.loads(failed.stdout)["threshold_pass"] is False
    exploratory=invoke(different)
    assert exploratory.returncode==0
    assert json.loads(exploratory.stdout)["threshold_pass"] is None
    resized=invoke(invalid)
    assert resized.returncode==2
    assert "Expected 941x1672" in resized.stderr
print("PASS: PNG pixel diff checks original size, nine regions and exact RGB equality")
