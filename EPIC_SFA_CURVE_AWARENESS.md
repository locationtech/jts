# EPIC: SQL/MM Spatial (ISO/IEC 13249-3) Curve Awareness in JTS

> **Live epic tracker and spec.** See GitHub: [locationtech/jts#1195](https://github.com/locationtech/jts/issues/1195).
>
> Companion red-test "progress meter" lives in
> `modules/curved/src/test/java/org/locationtech/jts/spec/curveawareness/CurveAwarenessSpecTest.java`.
> Each `test_TAG_*` method is a single failing assertion whose message is the
> executable spec for that sub-issue. When a TAG is implemented, **delete its
> test method** (do not turn it green). The count of remaining red methods is the
> live gap meter.
>
> The sub-issue TAGs (e.g. `V-CP`, `M-LEN-CS`) in method names and `fail("TAG: …")`
> messages are the canonical keys that must match entries in this document.
>
> Run the meter:
> ```
> mvn -pl modules/curved test -Dtest=CurveAwarenessSpecTest -DfailIfNoTests=false
> ```

This epic adds first-class support for the OGC SFA / ISO 19125-2 / SQL/MM extended
geometry types (`CIRCULARSTRING`, `COMPOUNDCURVE`, `CURVEPOLYGON`, `MULTICURVE`,
`MULTISURFACE`, plus `TRIANGLE`/`TIN`/`POLYHEDRALSURFACE` for completeness) while
preserving the existing `Polygon`/`LineString` contracts for legacy callers.

Phase 1 (foundations, largely landed):
- Structural `CurvePolygon`, `CircularString`, `CompoundCurve` etc. in the opt-in
  `jts-curved` module (see `SPEC_F_CP.md` for the dovetail decision on legacy API).
- `CurvedWKTReader`/`Writer`, `CurvedGeometryFactory`, `Linearizable.toLinear`.
- Subtype preservation in copy/reverse/etc.

Later phases implement the actual curve-aware algorithms for the TAGs below.

## TAG Categories & Status (as of CurveAwarenessSpecTest)

### Foundations (structural)
- **F-CP** — Structural `CurvePolygon` (shell/holes are `LineString`/`CircularString`/`CompoundCurve`). Landed (Option A legacy fallback). See [SPEC_F_CP.md](modules/curved/SPEC_F_CP.md) and JTS Version History.
- **F-MC** / **F-MS** — Structural `MultiCurve` / `MultiSurface`. Landed alongside F-CP.
- **F-RD** — `CurvedShapeWriter` (and related renderers) must arc-walk `CurvePolygon` rings and `MultiSurface` members. Still red.

### Metrics (M-*)
- **M-LEN-CS** — `CircularString.getLength()` returns analytical arc length (`r·θ`), not chord sum.
- **M-LEN-CC** — `CompoundCurve.getLength()` sums analytical lengths of members.
- **M-AREA-CP** — `CurvePolygon.getArea()` applies circular-segment correction (sector areas) on top of the chord polygon.
- **M-DIM** — `getDimension()` / coordinate dimension for empty curved types is correct and guarded.

### Boundary (B-*)
- **B-CP** — `CurvePolygon.getBoundary()` returns a `CompoundCurve` (preserving arc members).
- **B-MS** — `MultiSurface.getBoundary()` returns a `MultiCurve` (curved rings preserved).
- **B-CC** — Open `CompoundCurve` boundary is its two endpoints (as `MultiPoint`); closed is empty. Needs explicit guard (currently inherits `LineString` behaviour).

### Buffer / Offset (BUF-*, OFF, VBF)
- **BUF-1** — Single-arc `CircularString.buffer(d)` yields `CurvePolygon` whose rings are `CompoundCurve`s containing arcs (outer arc, caps, inner arc).
- **BUF-N** — Compound / mixed buffers preserve arcs where possible.
- **BUF-NEG** — Negative buffer where |d| > R on an arc yields EMPTY cleanly.
- **OFF** — `OffsetCurve` on `CircularString` returns an (analytically) offset `CircularString`.
- **VBF** — `VariableBuffer` interpolates distances along arc-length parameter.

### Distance (D-*)
- **D-PT** — Point-to-arc distance clamps to the arc sweep (analytical, not chord polyline).
- **D-AA** — Arc-to-arc distance via two-circle solve + sweep clip.
- **D-OP** — `DistanceOp` accepts curved inputs without forcing densification.
- **D-HF** — `DiscreteHausdorffDistance` / `DiscreteFrechetDistance` sample by arc-length (uniform sweep) on curved inputs.

### Predicates / Relate (R-*)
- **R-PR** — `Geometry.relate(...)` (and the RelateNG matrix) computed on arc topology, not densified polylines.
- **R-CONT** — Full predicate suite (`contains`, `intersects`, `covers`, `within`, `touches`, `crosses`) for curved inputs must be arc-aware.
- **R-EQ** — `equalsExact` treats a `CircularString` as distinct from a `LineString` with identical control points (subclass + representation identity matters).

### Noding (N-*) — foundation for overlay & robust predicates
- **N-AA** — Public utility for arc-arc intersection (two-circle + sweep clip) returning 0/1/2 points + parameters on each arc.
- **N-AL** — Arc vs. line-segment intersection utility (circle-line + sweep + segment clamp).
- **N-SS** — Arc-aware `SegmentString` / `NodedSegmentString` + Noder support so that `MCIndexNoder`, snap-rounding etc. can carry arc spans.

### Overlay (OV)
- **OV** — Boolean ops (`union`, `intersection`, ...) on curved inputs produce `CurvePolygon` / curved results where the output boundary consists of (portions of) the original arcs. Intersection points become the join vertices between preserved arc pieces.

### Centroid / Interior Point (C-*)
- **C-LIN** — `CircularString` centroid is the arc-length-weighted mean of the arc (not chord average).
- **C-AREA** — `CurvePolygon` centroid combines sector centroids (for the circular segments) with the usual polygon contribution of the chord polyline.
- **C-IP** — `InteriorPoint` for `CurvePolygon` must be provably inside the true curved region (not just the densified approximation).

### Validity (V-*) — **focus of this query**
- **V-CP** — `IsValidOp` (and `Geometry.isValid()`) for `CurvePolygon`.
  - Must analytically verify that arc boundaries do not self-intersect (using exact arc-arc / arc-chord predicates, not control-point densification).
  - Ring orientation must be consistent when measured with sector area (signed circular segments).
  - Holes must lie inside the shell using arc-aware containment (not densified `pointInRing` on chords).
  - Current status (see proofs triage): structural `valid_curve_polygon` exists in the formal model; full geometric validity (self-intersection freedom + hole containment) depends on closing the arc-span soundness gap (`arc_chord_intersect_sound` / promotion of IVT circle crossing to minor-arc crossing). See `NetTopologySuite.Proofs/docs/issue-64-arc-primitives-triage.md` (V-CP / #4c marked PARTIAL; #3c still open).
- **V-CS** — `IsSimpleOp` (and `Geometry.isSimple()`) for `CircularString` and `CompoundCurve`.
  - Must detect self-overlaps and improper intersections using the true arc geometry (e.g. the multi-arc example that loops back over itself).
  - Linear (control-point) `isSimple` is insufficient and can give accidental answers.
  - Depends on the same analytic arc self-intersection primitives as V-CP.

These two (V-CP, V-CS) are **not yet in a deliverable state** for implementation:
- The red tests exist and precisely capture the requirement.
- Foundational arc math (atan2, angle-between, arc length, in-circle, chord-crossing IVT, predicates for `arc_chord_intersects` / `arc_arc_intersects`) has landed (Option A).
- However, the soundness bridge that lets an implementation *trust* the predicates for a validity checker (promoting "crosses the circle" → "crosses the arc span") remains an open gap. No dedicated oracle vectors, ref runners, or adversarial hunters for validity cases yet (unlike M-LEN-*).
- Arc-aware `pointInRing` / contains for holes, plus sector-area orientation checks, are also prerequisites and are themselves still red in the spec.
- See proofs `CurveGeometry.v` (only structural validity today), `ArcIntersectIVT.v` (explicitly notes the span promotion is not closed), and the JTS red tests.

When the proofs side closes the relevant gaps and supplies oracles/vectors, V-CP / V-CS will become deliverable. Implementation will live in `jts-core` (extending `IsValidOp` / `IsSimpleOp` with curve awareness, or providing curve-specific entry points from the curved module) + updates in `jts-curved`.

### Hulls (H-*)
- **H-CV** — `ConvexHull` of an arc (or curved input) returns only the extreme points on the true arc (endpoints + points where the tangent is cardinal), not a densified polyline hull.
- **H-CC** — `ConcaveHull` must be arc-surface aware.

### Simplification (S-*)
- **S-DP** — `DouglasPeuckerSimplifier` on a `CircularString` must preserve the `CircularString` identity (operate on arc parameters or re-fit arcs) rather than collapsing to a 2-point `LineString`.
- **S-VW**, **S-TP** — `VWSimplifier` and `TopologyPreservingSimplifier` must respect arc spans / effective area on the true curve.

### Affine Transforms (AT-*)
- **AT-S** — Similarity transforms (rotate, uniform scale, translate) on a `CircularString` produce another `CircularString` (transformed controls still define a circle).
- **AT-NS** — Non-similarity transforms (shear, anisotropic scale) turn a circle into an ellipse arc (not representable); spec is to detect and fall back to densified + transform the polyline.

### Linear Referencing (LRF-*)
- **LRF-LEN** — `LengthIndexedLine` on `CircularString` must interpret the index as true arc length.
- **LRF-LOC** — `LocationIndexedLine` on `CompoundCurve` must be member-aware (address member index + param within member).

### Densifier (DSF)
- **DSF** — `Densifier` on curved input must delegate to `toLinear(tolerance)` (or an arc-aware densifier) rather than walking raw control coordinates.

### Triangulation / Voronoi (TRI-*)
- **TRI-DT** — `DelaunayTriangulationBuilder` accepts curved sites / boundaries and densifies internally (via `toLinear`) to a tolerance.
- **TRI-VR** — Same for `VoronoiDiagramBuilder`.

### Polygonizer / Coverage (PLG, COV)
- **PLG** — `Polygonizer` accepts `CompoundCurve` edges and can emit `CurvePolygon` faces.
- **COV** — `CoverageUnion` / `CoverageBoundary` on `CurvePolygon` coverages preserve shared arcs as `CIRCULARSTRING` segments in the result.

### Snapping / Precision (PRC-SN)
- **PRC-SN** — Snap-to-grid / precision model on a `CircularString` snaps the three control points and preserves the arc (if the resulting R/C/sweep is still valid on the grid); otherwise densifies. (See proofs for `CURVE_SNAP_DECISION` / `CURVE_SNAP_INVARIANTS_EXACT` oracle + HOLE_PRECISION_AUDIT for #979 interaction.)
  - Fresh oracle from artifact: https://github.com/grootstebozewolf/NetTopologySuite.Proofs/actions/runs/26928081635/artifacts/7402195928 (JTS#979 precision-collapse + power-of-two hot-pixel; also curve snap soundness).
  - Vectors: modules/curved/src/test/resources/.../rocqref/curve_snap_vectors.txt (refreshed header citing this run; cases cross-checked vs exact Q snap+centre-on-grid).
  - Ties to V-CP: arc self-intersect/validity under fixed PM must not be poisoned by precision collapse of small features or arc centres. See also updated hole979_hunt.txt and driver.ml CURVE_SNAP_* + HOLE_* modes.

### TestBuilder integration (TB-*)
- **TB-T** — Dedicated tools in JTSTestBuilder for `CompoundCurve` and `CurvePolygon` (sibling to `CircularStringTool` etc.).
- **TB-FN** — Function tree shows curve-awareness badges (● native, ◯ passthrough, ✕ flattens) driven by annotations on `GeometryFunction` implementations.

## References & Related Work
- GitHub epic: locationtech/jts#1195 (primary).
- Structural foundation PRs and Option A decision: see `SPEC_F_CP.md`.
- Proofs / formal backing: `NetTopologySuite.Proofs` (this workspace's sibling repo). See `proofs-first-batch/06-curve-awareness-epic-support.md`, `docs/issue-64-arc-primitives-triage.md` (especially V-CP / arc validity status), `docs/audit-phase4-curves.md`, `theories/CurveGeometry.v`, `theories/ArcIntersectIVT.v`, and `docs/verified-claims.md` (Phase 4).
- NTS alignment epic: NetTopologySuite/NetTopologySuite#828.
- Oracle / adversarial infrastructure pattern: `CurveAdversarialTest`, `CurveRefRunner`, `rocqref/` vectors (currently strongest for M-LEN; needs expansion for V-*, N-*, etc.).
- JTS Version History and module READMEs for landed pieces.

## How to contribute a TAG
1. Land supporting math / oracles on the proofs side (or note honest gaps).
2. Add / expand vectors + ref runner + hunter (for the property) in `modules/curved/.../adversarial/`.
3. Implement the algorithm in core or curved (curve-aware paths, new utilities under `operation/...` or `algorithm/...`).
4. Delete the corresponding red test method in `CurveAwarenessSpecTest`.
5. Update this document (mark landed, add release note, cross-refs).
6. Port / align corresponding NTS work.

When in doubt, the red test + its `fail` message *is* the spec. Refine only when you have a green implementation that demonstrates the intent.

---

*This document lives in the JTS source tree (grootstebozewolf/jts and locationtech/jts) as the single source of truth for the curve-awareness sub-issue specs and progress meter.*