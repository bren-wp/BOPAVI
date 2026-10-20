# BOPAVI — Android collision geometry and near-miss QA

**Current Kotlin sources:** GameSimulation.kt, LevelEngine.kt and GameView.kt.
**World:** 480×800 logical units. Bopi's *physical body* is a circle centered
at (126, y), radius 18. Transparent sprite pixels, moving wing art, scarves
and glow halos do **not** collide.

## Painted rock correspondence

Each procedural gate has a top and bottom shaft, with 7-unit-radius rounded
bands at the opening lips. The shaft uses its real X/width and opening top/bottom;
the lip adds 7 logical units of limited overhang, only within its painted band.
An infinite vertical rectangle for the overhang would cause false collisions.
Render and collision use the same LevelEngine.opening(gate, time, difficulty).

The swept predicate, PillarCollision.sweptBodyHitsPillar, interpolates the bird
Y, moving gate X and moving lip positions within a bounded <=120Hz physics
slice. It rejects disjoint broad-phase geometry without allocating new sprites.
Signed-distance subdivision detects shallow mid-slice contact that an
endpoint-only test could miss. A static tangency is not treated as a hit.

Berries use an independent swept segment-to-circle contact check. A pickup
must not alter pillar collision or double-count collected food.

## Regression evidence and limits

LevelEngineTest checks rounded lip misses and penetrations, tangent edges,
moving-corner sweeps, finite opening bounds, 30/60/90/120Hz replay, and
long-distance world coordinate precision. Debug collision outlines must only
render when the app is debuggable and explicitly opted in.

**Outstanding visual sign-off:** capture real Android overlay/contact and
near-miss frames and compare the outline with the actually painted rock pixels.
CI passing does not prove screenshot-level 1:1 fidelity on every device.
