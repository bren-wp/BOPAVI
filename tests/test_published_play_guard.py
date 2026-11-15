#!/usr/bin/env python3
"""Regression: never publish a future release that hides production PLAY."""
import sys
import tempfile
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/"tools"))
from sync_release_docs import assert_home_play_contract

class PublishedPlayGuardTests(unittest.TestCase):
    def test_current_published_docs_and_native_routes(self):
        assert_home_play_contract(ROOT)

    def test_release_notes_reject_no_play_and_stale_candidate_status(self):
        with tempfile.TemporaryDirectory() as tmp:
            path=Path(tmp)/"notes.md"
            for invalid in (
                "New gameplay cannot be started from Home.",
                "U ovoj verziji nema dostupnog gumba za pokretanje novog leta.",
                "**CANDIDATE / NOT PUBLISHED.**",
            ):
                path.write_text("## v1.0.89 — Android (build 219)\n"+invalid+"\n",
                                encoding="utf-8")
                with self.assertRaisesRegex(ValueError,"Release notes"):
                    assert_home_play_contract(ROOT,path)
            path.write_text("## v1.0.89 — Android (build 219)\n- PLAY starts gameplay.\n",
                            encoding="utf-8")
            assert_home_play_contract(ROOT,path)

if __name__=="__main__":
    unittest.main(verbosity=2)
