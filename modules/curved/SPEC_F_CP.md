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

## The dovetail decision

`CurvePolygon` inherits `Polygon.getExteriorRing()` which is typed
`LinearRing`. A structural CurvePolygon's actual shell is a
`CompoundCurve` (or `CircularString` / `LineString`). The two facts
can't both be true at runtime. We pick one of:

| Option | What `getExteriorRing()` returns | New accessor | Trade-off |
|---|---|---|---|
| **A — legacy fallback** | `LinearRing` of densified chord coordinates at a default tolerance | `getExteriorCurve()` returns the structural Curve | Old callers keep working unchanged; they see a polyline approximation. Two-tier API: structural-aware callers use the new method, legacy callers stay on the inherited one. Trade-off is that "the same geometry" answers two different questions on the same instance. |
| **B — widen return type** | `LineString` (`CompoundCurve` extends `LineString`) | none — the existing method is enough | Single source of truth, no API doubling. Breaks every caller that does `(LinearRing) p.getExteriorRing()` or that relies on `LinearRing`-specific API (very rare in third-party code, ubiquitous in JTS's own internals). Requires sweeping `jts-core` for casts. |
| **C — fail-fast** | throws `UnsupportedOperationException` | `getExteriorCurve()` returns the structural Curve | Loudest diagnostic; forces every caller to migrate. Most painful interim period because *any* third-party code that touches a CurvePolygon via the Polygon API blows up at runtime. Probably only acceptable behind a feature flag. |

### Where we lean, today

**Option A** is the lowest-friction landing for Phase 1: it keeps the
existing Polygon API contract intact for callers that don't know about
curves, and gives curve-aware callers a clean structural accessor. The
default tolerance for the linearised view becomes a `CurvedGeometryFactory`-
level setting (consistent with how `Linearizable.toLinear(0.0)` already
treats a zero-tolerance request as "implementation default"). The cost
is the two-tier API; we accept it as the price of not breaking jts-core.

The argument **against** Option A is real: `getExteriorRing().getNumPoints()`
on a CurvePolygon now returns a tolerance-dependent count, which is a
subtle correctness landmine for code that compares ring point counts as
identity checks. Worth a release-note bullet.

**Option B** is the principled answer if we're willing to do a
core-wide audit. The actual count of `(LinearRing)` casts on results of
`getExteriorRing()` inside `jts-core` is the deciding number; if it's
small enough to clean up in one PR, B becomes attractive again.

**Option C** is the right answer behind a feature flag — useful for
strict pipelines that want to know they aren't accidentally feeding a
CurvePolygon to non-curve-aware code.

### What we are deferring

- The default linearisation tolerance value (if A wins). Likely
  parameterised on the `PrecisionModel`'s scale; precise value is an
  implementation PR decision.
- Whether `getCoordinates()` returns the structural control points or
  the linearised chord coords. Either choice has the same Polygon-API
  contract issue as `getExteriorRing()`. Suggest matching the choice
  made for `getExteriorRing()` to avoid two-tier-API-within-two-tier-API.
- Holes: same three options apply to `getInteriorRingN(int)`. We pick
  once and apply uniformly.

## Smallest concrete next step

1. **Maintainer ack on the option.** A one-line "Option A / B / C"
   reply on the epic issue is enough.
2. **PR `arch:` commit** updating this file to delete the unselected
   rows and record the choice as decided.
3. **Implementation PR** (separate) starts from there:
   - Update `CurvePolygon` (`CurvedGeometryFactory` constructors,
     `copyInternal`, `toLinear`).
   - Update `CurvedWKTReader.readCurvePolygonText` to build a
     structural CurvePolygon.
   - Update `CurvedWKTWriter` to emit COMPOUNDCURVE / CIRCULARSTRING
     tags inside the body.
   - Delete the methods in `CurvePolygonStructuralSpec` that the
     implementation makes pass (the test class shrinks; the
     `FCP-DOVE` method goes with the others).

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
