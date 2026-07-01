# F-CP — Structural CurvePolygon

> Spike / dovetail notes for the **F-CP** sub-issue of the SFA Curve
> Awareness epic ([locationtech/jts#1195](https://github.com/locationtech/jts/issues/1195)).
> Companion to the red-test suite at
> [`CurvePolygonStructuralSpec.java`](src/test/java/org/locationtech/jts/spec/curveawareness/CurvePolygonStructuralSpec.java).

## What F-CP is

Today `CurvePolygon` extends `Polygon` and the `CurvedWKTReader` collapses
its rings to flat `LinearRing`s on read. Phase 1 of the curve epic
(*Foundations*) calls for the structural form: a `CurvePolygon` whose
shell and holes are **Curves** — `LineString`, `CircularString`, or
`CompoundCurve` — exposed without lossy linearisation. This is the hard
prerequisite for every Phase-2 TAG that needs to talk about a curved
boundary (`B-CP`), curved area (`M-AREA-CP`), or curve-aware validity
(`V-CP`).

## Why a focused spike

The implementation has one design decision that **can't be made inside
the implementation PR** — it changes the contract of `Polygon`'s public
API for a subclass. Picking the answer in code review is the wrong
shape: we want the decision settled in writing first, then a clean PR
lands the chosen option.

That decision lives in epic §7 risk #1 and surfaces here as the
`FCP-DOVE` red test.

## The dovetail decision (resolved)

**Chosen: Option A — legacy fallback.**

`CurvePolygon` (post F-CP) inherits `Polygon.getExteriorRing()` which
returns a `LinearRing` view built from the control-point polyline of the
structural ring (phase-1 linear view). Curve-aware callers use the new
`getExteriorCurve()` / `getInteriorCurveN(int)` to obtain the structural
`LineString` (`CircularString` / `CompoundCurve` / `LineString`).

**Phase-1 note on linearisation:** `toLinear(tolerance)` (and thus the
legacy ring views) currently return the raw control points; there is no
arc tessellation / densification. The `tolerance` parameter is accepted
for the `Linearizable` interface contract but is a no-op in phase 1.
Real arc-aware densification (honouring tolerance with chord error or
segment count) is deferred to later phases that need geometric accuracy
(e.g. area, buffer, predicates). The "linear view" here is the control
polyline required to satisfy the `Polygon` parent contract.

See implementation in `CurvePolygon` (structural ctors + fields,
`copyInternal`, `reverseInternal`, `toLinear`, `normalize` overrides)
and `CurvedWKT*` (reader collects structural rings; writer emits tagged
curved rings inside CURVEPOLYGON).

### Why A (for Phase 1)

- Keeps the existing `Polygon` API contract intact for legacy callers
  (including all of jts-core and third-party code that casts to
  `LinearRing` or calls `getNumPoints` etc on rings).
- Gives structural access via two new methods.
- Lowest friction; no core sweep, no runtime explosions.
- Trade-off (documented): two-tier API; the legacy view is a control
  polyline (not a true arc tessellation). See release notes.

(B and C were ruled out for Phase 1 per epic §7 risk #1 and SPEC
discussion; they remain options for a future breaking 2.0 if desired.)

### What was deferred / noted

- Default linearisation tolerance (implementation chose 0.0 meaning
  "factory default", consistent with `Linearizable.toLinear(0.0)`).
- `getCoordinates()` continues to return the densified view (to match
  the legacy ring contract); structural control points via the curve
  accessors.
- Holes treated uniformly with shell.
- `equalsExact` / R-EQ remains view-based (structural curves do not
  affect it); explicit `test_FCP_EQ` and doc in `CurvePolygon` javadoc.
  Full arc-aware equality deferred to R-EQ TAG.
- Release note recommended for the tolerance-dependent ring metrics.

## Implementation status

**Landed** under Option A in the F-CP implementation PR (stacked on
#1194). The `CurvePolygonStructuralSpec` red-test methods were deleted
by the feat commit (per epic convention); `test_FCP_DOVE_*` and
`test_FCP_EQ_*` remain as executable documentation of the chosen
contract and equality semantics.

## Smallest concrete next step (done)

1. Maintainer ack on Option A (see epic #1195 and PR #1194 thread).
2. `arch:` commit (this update) recorded the decision.
3. Implementation PR landed the structural `CurvePolygon` (Option A),
   reader/writer updates, overrides for preservation, shrinking of the
   red-test spec (kept DOVE/EQ as docs), plus verification tests.

## Pre-requisite that's already landed

The `feature/sfa-curve-compoundcurve-members` branch on the fork
restructures `CompoundCurve` to store `LineString[]` members, with
`getCurveN(int)` / `getNumCurves()` accessors. That work is the
precondition for any "CurvePolygon shell is a CompoundCurve" sentence
to be meaningful, and F-CP picks up from it.

## Leaving the door open

This document is a starting point, not a contract. If a Phase-2 TAG
(`B-CP`, `V-CP`, etc.) surfaces a constraint we missed — for example,
that exposing the structural shell to algorithms that internally cast
to `LinearRing` poisons more call sites than expected — the option
choice is fair game to revisit before the implementation PR lands.
After the implementation PR lands, changing the option becomes a
breaking change and goes through a release-note + deprecation cycle.

## References

- Epic: [locationtech/jts#1195](https://github.com/locationtech/jts/issues/1195) §7 risk #1.
- Source PR: [locationtech/jts#1194](https://github.com/locationtech/jts/pull/1194) — Phase-1 extension hooks + opt-in jts-curved.
- Precondition: `feature/sfa-curve-compoundcurve-members` on the fork.
- Red tests: `CurvePolygonStructuralSpec.java`.
