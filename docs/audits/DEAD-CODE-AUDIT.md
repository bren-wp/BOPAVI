# BOPAVI — Dead code and test reachability (incremental)
Date: 2026-11-12; baseline 9d6baf881c3a177524d1effef4e3c7044ba4c01f.

Five tests existed but were not directly included in native-ci.yml source-audit:
- test_bopavi_3d_shared_skin_contract.py: outdated expectation of adjacent dismiss+action; replace it with explicit one-action/double-tap assertions.
- test_forest_lives.py: current contents correctly assert GameSimulation pickup ownership; preserve without an unnecessary edit.
- test_reference_3d_home_scaling.py: checks scalable Home vectors.
- test_shop_single_screen_qa_contract.py: checks real API36 Shop smoke setup.
- test_splash_home_owner_864_fidelity.py: source-level historical Splash/Home reference gate.

All five are restored to CI; a separate new wallet contract is also included. **No Kotlin code or graphic asset was deleted**, because static reference count cannot prove it is unreachable. Full graph-based R8/reflection, asset and runtime navigation audit is still open. Save keys, migrations and skin identity remain unchanged.
