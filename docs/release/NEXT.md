# Next Android release — under review

## v1.0.88 — SAF recovery journal and fail-closed owner screenshot QA (build 218)

**CANDIDATE / NOT PUBLISHED.** Draft PR #236 must pass source, unit, emulator, provider-fault and the separate 50/50 visual acceptance gate before it can be merged or released.

- Save the previous SAF document bytes in a synced, integrity-checked private recovery journal before beginning a potentially truncating provider write. Verify the completed bytes and try to restore the old backup on I/O failure; preserve a journal across process restart if recovery fails.
- Require readable SAF targets for recoverable overwrite and serialize old-document grant release behind writes. Do not modify the local v1–v6 JSON save schema or virtual wallet.
- Add JVM fault-injection tests for truncation, power-loss-style restart and stale destination; provider behavior and out-of-process writes remain to be tested on real devices.
- Track 50 original owner WebPs and genuine Android PNGs independently of build success. Block a new release when any original is missing, a capture is absent/duplicated, or even one RGB pixel differs.
- Known blockers: 10 missing original 1440×3040 WebPs, incomplete native Game Over B evidence, no zero-difference results, legal MIT/assets/Terms review and physical SAF-provider tests.

Unsigned debug/release APK and AAB artifacts may be built for QA. Do not represent any artifact as publisher-signed or overwrite v1.0.87.
