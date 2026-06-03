# Curve Awareness Epic — Progress Overview

**Epic source:** the full text in the user query (to be lifted as `EPIC_SFA_CURVE_AWARENESS.md` or GitHub Epic body).  
**Date of this overview:** 2026-06-03 (post latest artifact processing + RGR/hardening).  
**Live meter:** `CurveAwarenessSpecTest` (46–50 red `test_*` methods; run with `-Dtest=CurveAwarenessSpecTest`; delete methods on ship per §5/11).  
**Current branch (last work):** `feature/sfa-curve-M-LEN-CC-rgr` (M-LEN-CC green via member delegation; structural CC + writer member-tagging integrated as prereq).  
**Key infra:** `CurveRefRunner` + `CurveAdversarialTest` (RocqRefRunner pattern from #1197), vectors in `src/test/resources/.../rocqref/`, proofs artifacts (NetTopologySuite.Proofs runs), Flocq/Rocq oracles.

## High-level Status (Phases per epic §9)

- **Phase 1 (Foundations):** F-CP / F-MC / F-MS **landed** (structural CurvePolygon with LineString/CompoundCurve/CircularString rings per Option A + SPEC_F_CP.md; MultiCurve/MultiSurface subtype-preserving; copy/toLinear/WKT/reader/writer/getExteriorCurve etc.; early FCP tests removed from CurvePolygonStructuralSpec, DOVE + EQ doc tests remain). F-RD **green impl on RGR branch** (CurvedShapeWriter added with structural ring walking + CircularString arc sampling for visual curves in Shapes; verification test passes; red TAG marker + no GeometryPainter/ShapeWriter hook integration yet per "don't integrate yet").
- **Phase 2 (Properties):** 
  - M-LEN-CS **landed + hardened**.
  - M-LEN-CC **landed** (sums via structural members + CircularString analytical).
  - B-CP / B-MS **landed + hardened**.
  - B-CC **partial** (inherits LineString boundary; explicit guard still red in meter).
  - M-AREA-CP / M-DIM / V-CP / V-CS **red** (area with segment correction, dim guards, validity for curves not yet).
- **Phases 3–7:** Almost entirely **red** (no production changes for distance/centroid/buffer/overlay/noding/polygonizer/snap/tri/TestBuilder etc.). N-AA has **foundation hardening** (see below). Some core-touching TAGs (N-*, PLG, PRC-SN, DSF) still untouched per §6.
- **Early cross-cutting fixes (pre-RGR, from user reports + review):** COMPOUNDCURVE member-structured WKT emission (non-flat "( ( " form), outer-ring-only Z/M dimension in CurvedWKT*, structural curves excluded from equalsExact (with test_FCP_EQ... documenting view-based + isEquivalentClass; epic §7 risk).

**RGR discipline followed:** New branch per TAG (or cluster), red-first (seam docs in meter methods), green (minimal impl + verification in *StructuralSpec or adversarial, meter fail untouched), refactor (readability/soundness). Meter methods + fail("TAG:") lines never edited green.

## Hardening Status (RocqRefRunner + Proofs Artifacts)

"Hardening" = dedicated adversarial tests using:
- CurveRefRunner (modeled on RocqRefRunner: Case classes, load*Cases that cross-validate claimed vs Java oracle on load, Result with isSound(), run(Iterable) that exercises the *impl*).
- Vectors baked from (or citing) NetTopologySuite.Proofs GHA artifacts (run with oracle_bin ARC_* / ORIENT modes + b64 extracted oracles; stand-ins + py port of exact fn when direct 404/auth needed; headers always cite exact run/artifact URL).
- Asserts in CurveAdversarialTest (load + isSound() + hunter mains for "search effective (current fails)").
- Ties to recent RGR (length/boundary) or future (N-AA arc primitives).

**Marked as HARDENED (with links in code/comments):**

- **M-LEN-CS** (and supporting length infra): 
  - Full: `testLoadArcLengthVectors` (load + validate + `CurveRefRunner.run(cases); assert r.isSound()`), `testHunterFindsArcLengthDeviationsOnAdversarialInputs` (post-M-LEN-CS hunter finds 0), `testKnownNearFlatCaseFromVectorsDeviatesUnderLinearImpl` (vector case verification).
  - Vectors: `curve_arc_length_vectors.txt` (headers cite multiple proofs runs + #64 ArcLength.v + b64_circular_arc_length; additional cases appended for 26856051962 via py port of `exactCircularArcLength`).
  - Oracle in CurveRefRunner + private exact fn (r*theta after circumcenter + sweep disambig; matches proofs).
  - Also exercised `CircularString.getLength()` (analytical, not chord) + `CurveCounterexampleHunter`.

- **B-CP / B-MS** (boundary):
  - `testCurveBoundaryHardeningUsingRocqRefRunner` (builds CP + mixed MS via CurvedWKTReader; calls getBoundary(); uses local `refSign` (naive orient) + orientation vectors on the curve control points of the returned boundary members; asserts !=0 and presence).
  - Vectors: `orientation_proof_vectors.txt` (headers updated for 26800356316 + fresh oracle_bin from 26856051962; supports predicate/boundary hardening).
  - Ties directly to the RGR boundary seams (getBoundary overrides, MultiCurve return, orient preservation on control pts per proofs).

- **N-AA foundation (arc–arc / chord–circle primitives for future noding/overlay/validity):**
  - New from processing the provided artifact URL: `testLoadArcChordCrossVectorsFromOracleBinArtifact` (load + mix of TRUE/FALSE + `CurveRefRunner.runChordCross(cases); assert r.isSound()`).
  - New support in CurveRefRunner: `ArcChordCrossCase`, `chordCrossesArcCircle` (ref using `TrianglePredicate.isInCircleRobust` sign-product, matching proofs hand-rolled), `loadArcChordCrossCases`, `ChordCrossResult`, `runChordCross`.
  - Vectors: `curve_arc_chord_cross_vectors.txt` (freshly generated by driving the downloaded `oracle_bin` (from run 26856051962 / artifact 7373024936) with ARC_CHORD_CROSSES_CIRCLE mode + cases; headers cite exact URL + Proofs#64 + ArcIntersectIVT.v; load validates claimed oracle vs Java ref).
  - This bin (oracle-bin-linux) was the direct output of the given artifact; supports ARC_CHORD_CROSSES_CIRCLE + ARC_PASSES_THROUGH_PIXEL + ORIENT (ARC_LENGTH added in proofs post-this-run).

**General / supporting:**
- Orientation vectors load test (`testLoadAndValidateOrientationVectorsUsingRocqRefRunnerPattern`) updated with both runs + oracle_bin note.
- All vectors updated with the 26856051962 citation during latest "hardening using RocqRefRunner" pass.
- Pattern ready for more (D-*, V-*, BUF-*, R-*, OV etc.): add Case/Result/run + vectors + assert in adversarial, wire into TAG RGR when green lands.

**No hardening yet for:** everything else (BUF_*, D-*, C-*, V-*, S-*, AT-*, LRF-*, DSF, TRI-*, PLG, COV, PRC-SN, TB-*, R-*, OV, N-AL/N-SS (N-AA only via chord), M-AREA etc.). M-LEN-CC can reuse existing arc-length vectors for future hardening. More proofs artifacts will feed the rest (see proofs-first-batch/01-arc-primitives.md which refs JTS#1195 + M-LEN-*/V-CP/N-* + this exact hardening pattern).

## Cross-refs to Epic Sections
- Matches §4 (spike landed items), §5 (meter + delete-on-ship + TAG PR convention), §6 (core touch list; only N-*/PLG/PRC-SN/DSF/F-RD would), §7 (risks noted in red tests + structural spec for FCP-DOVE + R-EQ).
- RGR examples in the red-test javadocs for M-LEN-CS / B-CP / B-MS exactly follow "red test first: identify interface/arch seams, green simplest, refactor readability/soundness".
- Artifact processing (this query URL + priors) directly fulfills repeated "Use RocqRefRunner to create adverserial tests Artifact download URL..." + "hardening using RocqRefRunner" requests.
- Proofs issues batch + triage + Flocq build (prior turns) feed the oracles.

## Next (suggested, per epic order + pending in history)
- Continue RGR on low-risk remaining Phase 2 (M-AREA-CP, V-CP/V-CS, M-DIM guard). M-LEN-CC done in this RGR (after members prereq).
- Wire more vectors/hunter into new TAGs (e.g. once N-AA utility lands, use chord-cross vectors + hunter for arc-arc cases).
- F-RD (TestBuilder + CurvedShapeWriter full).
- When core seams (Phase 5 N-SS etc.) ready, open the WKB sibling epic per §3.
- Full meter empty = epic DoD #1.

## Files touched for this overview + recent hardening
- `modules/curved/src/test/java/org/locationtech/jts/spec/curveawareness/CurveAwarenessSpecTest.java` (progress header table + M-LEN-CC notes)
- `modules/curved/src/main/java/org/locationtech/jts/geom/curved/CompoundCurve.java` (M-LEN-CC getLength + prior structural)
- `modules/curved/src/main/java/org/locationtech/jts/io/curved/CurvedWKTWriter.java` (member-tagged CC emission)
- `modules/curved/src/test/java/org/locationtech/jts/geom/curved/CompoundCurveMembersTest.java` (brought in with structural)
- `modules/curved/src/test/java/org/locationtech/jts/geom/curved/adversarial/CurveRefRunner.java` + `CurveAdversarialTest.java` (chord cross + latest artifact citations)
- `.../rocqref/*.txt` (headers + cases for 26856051962)

Run `mvn -pl modules/curved test -Dtest=CurveAdversarialTest` (or full with excludes) to exercise all hardening. The spec meter is intentionally excluded from default CI (§11).

This overview can be merged into the root EPIC_*.md when created.
