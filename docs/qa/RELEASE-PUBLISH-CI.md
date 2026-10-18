# GitHub Release CI: documentation-only merge guard

On 2026-10-18, the main push at `96cb160` completed every native Android/iOS job but the final publishing job failed: `v0.2.21` already targets released commit `952d213`. Evidence: https://github.com/bren-wp/BOPAVI/actions/runs/39161110161. This is **not** a BOPAVI application crash.

The existing release and binary assets remain immutable. If a main push changes only documentation (excluding graphics under docs/assets), tests, README or the audited CI workflow, the publisher fetches the existing release commit, compares file paths and **skips creating a new release**. App sources, build configuration, art/generators or other paths still fail closed and require a genuinely new tested version. Test: `python3 tests/test_release_publish_guard.py`.

This prevents repeat version/tag publication from metadata-only merges; it does not establish physical-device crash-free operation or Play approval. A future workflow-only edit that impacts binary compilation requires tightening this allowlist or a version bump.
