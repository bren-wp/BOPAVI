#!/usr/bin/env python3
"""Regression tests: legacy-release retention can never delete v1+ releases."""
import importlib.util
import json
import subprocess
from pathlib import Path
import unittest
from unittest.mock import patch

PATH = Path(__file__).resolve().parents[1] / "tools/cleanup_legacy_releases.py"
spec = importlib.util.spec_from_file_location("cleanup_legacy_releases", PATH)
cleanup = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cleanup)


def release(tag, published="2026-10-19T00:00:00Z", **flags):
    return {"tag_name": tag, "published_at": published, "draft": False,
            "prerelease": False, "assets": [], **flags}


class RetentionTests(unittest.TestCase):
    def test_paginates_all_api_results(self):
        first = [release("v0.2.0"), release("v0.2.1")]
        second = [release("v0.2.2"), release("v0.2.3"), release("v1.0.0")]
        payload = json.dumps(first, indent=2) + "\n" + json.dumps(second)
        with patch.object(cleanup, "gh", return_value=payload) as mocked:
            records = cleanup.list_releases("bren-wp/BOPAVI")
        self.assertEqual(len(records), 5)
        mocked.assert_called_once()
        kept, removed = cleanup.plan_cleanup(records)
        self.assertEqual(removed, ["v0.2.0"])
        self.assertEqual(set(kept), {"v0.2.1", "v0.2.2", "v0.2.3"})

    def test_rejects_malformed_api_page(self):
        with patch.object(cleanup, "gh", return_value='{"unexpected": true}'):
            with self.assertRaisesRegex(ValueError, "array"):
                cleanup.list_releases("bren-wp/BOPAVI")

    def test_zero_through_four_legacy_releases(self):
        for count in range(10):
            records = [release(f"v0.2.{n}", f"2026-10-{n+1:02d}T00:00:00Z") for n in range(count)]
            kept, removed = cleanup.plan_cleanup(records)
            self.assertEqual(len(kept), min(3, count))
            self.assertEqual(len(removed), max(0, count-3))
            self.assertTrue(set(kept).isdisjoint(removed))
            self.assertEqual(kept[:1], [f"v0.2.{count - 1}"] if count else [])

    def test_stable_release_is_never_deleted_at_any_version(self):
        records = [release(f"v0.2.{n}", f"2026-10-{n+1:02d}T00:00:00Z") for n in range(6)]
        records.extend(release(f"v1.0.{n}", f"2026-10-{n+7:02d}T00:00:00Z") for n in range(8))
        records += [release("v1.1.0"), release("v2.0.0"), release("v10.7.5")]
        kept, removed = cleanup.plan_cleanup(records)
        self.assertEqual(removed, ["v0.2.0", "v0.2.1", "v0.2.2"])
        self.assertEqual(kept, ["v0.2.5", "v0.2.4", "v0.2.3"])
        self.assertTrue(all(cleanup.LEGACY.fullmatch(tag) for tag in removed))

    def test_never_prune_nonstandard_drafts_or_prereleases(self):
        records = [release(f"v0.2.{n}", f"2026-10-{n+1:02d}T00:00:00Z") for n in range(4)]
        records += [{"tag_name": "v0.2.99", "published_at": "2026-10-20T00:00:00Z",
                    "draft": True, "prerelease": False},
                    {"tag_name": "v0.2.98", "published_at": "2026-10-20T00:00:00Z",
                     "draft": False, "prerelease": True},
                    release("nightly-test"), release("v1.0.0")]
        kept, removed = cleanup.plan_cleanup(records)
        self.assertEqual(removed, ["v0.2.0"])
        self.assertNotIn("nightly-test", removed)
        self.assertNotIn("v1.0.0", removed)

    def test_fail_closed_when_api_returns_duplicates(self):
        with self.assertRaisesRegex(ValueError, "Duplicate"):
            cleanup.plan_cleanup([release("v0.2.0"), release("v0.2.0")])

    def test_minimum_three_legacy_releases(self):
        with self.assertRaises(ValueError):
            cleanup.plan_cleanup([release("v0.2.0")], keep_legacy=2)


    def test_missing_release_removes_only_confirmed_legacy_orphan_tag(self):
        missing = subprocess.CalledProcessError(
            1, ["gh", "api"], stderr="gh: Not Found (HTTP 404)"
        )
        with patch.object(cleanup, "gh", side_effect=[missing, "tag exists", missing, "deleted"]) as api:
            cleanup.prune_orphan_tag("bren-wp/BOPAVI", "v0.1.85")
        self.assertEqual(api.call_count, 4)
        self.assertEqual(
            api.call_args.args,
            ("api", "-X", "DELETE", "repos/bren-wp/BOPAVI/git/refs/tags/v0.1.85"),
        )

    def test_never_delete_stable_or_unknown_tags(self):
        with patch.object(cleanup, "gh") as api:
            for tag in ("v1.0.0", "v1.0.1", "v2.0.0", "nightly"):
                with self.assertRaisesRegex(ValueError, "non-legacy"):
                    cleanup.prune_orphan_tag("bren-wp/BOPAVI", tag)
            api.assert_not_called()

    def test_api_errors_other_than_404_abort_without_deletion(self):
        forbidden = subprocess.CalledProcessError(
            1, ["gh", "api"], stderr="gh: Resource not accessible (HTTP 403)"
        )
        with patch.object(cleanup, "gh", side_effect=forbidden) as api:
            with self.assertRaises(subprocess.CalledProcessError):
                cleanup.prune_orphan_tag("bren-wp/BOPAVI", "v0.1.85")
            api.assert_called_once()

    def test_existing_release_blocks_orphan_tag_removal(self):
        with patch.object(cleanup, "gh", return_value="existing release") as api:
            with self.assertRaisesRegex(ValueError, "still exists"):
                cleanup.prune_orphan_tag("bren-wp/BOPAVI", "v0.1.85")
            api.assert_called_once()

    def test_interrupted_inventory_is_idempotent_and_continues(self):
        initial = [release(f"v0.2.{n}", f"2026-10-{n+1:02d}T00:00:00Z") for n in range(5)]
        after_missing = initial[1:]
        with patch("sys.argv", ["cleanup", "--repo", "bren-wp/BOPAVI",
                                "--new-tag", "v1.0.0", "--apply"]), \\
             patch.object(cleanup, "verify_current_release") as verify, \\
             patch.object(cleanup, "list_releases",
                          side_effect=[initial, after_missing, after_missing, after_missing]), \\
             patch.object(cleanup, "prune_orphan_tag") as orphan, \\
             patch.object(cleanup, "gh") as api:
            cleanup.main()
            verify.assert_called_once()
            orphan.assert_any_call("bren-wp/BOPAVI", "v0.2.0")
            orphan.assert_any_call("bren-wp/BOPAVI", "v0.1.85")
            api.assert_called_once_with(
                "release", "delete", "v0.2.1",
                "--repo", "bren-wp/BOPAVI", "--yes", "--cleanup-tag"
            )

    def test_protected_legacy_versions_changing_fail_closed(self):
        initial = [release(f"v0.2.{n}", f"2026-10-{n+1:02d}T00:00:00Z") for n in range(5)]
        changed = initial[:-1]
        with patch("sys.argv", ["cleanup", "--repo", "bren-wp/BOPAVI",
                                "--new-tag", "v1.0.0", "--apply"]), \\
             patch.object(cleanup, "verify_current_release"), \\
             patch.object(cleanup, "list_releases", side_effect=[initial, changed]), \\
             patch.object(cleanup, "prune_orphan_tag") as orphan, \\
             patch.object(cleanup, "gh") as api:
            with self.assertRaisesRegex(ValueError, "Protected"):
                cleanup.main()
            orphan.assert_not_called()
            api.assert_not_called()


if __name__ == "__main__":
    unittest.main(verbosity=2)
