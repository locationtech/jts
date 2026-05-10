# jts-curved

Opt-in JTS module providing the OGC Simple Features Access (SFA) /
ISO 19125-2 extended geometry types and a curve-aware WKT reader/writer.

## What it adds

| Geometry type      | Java class                                              | Extends                         |
|--------------------|---------------------------------------------------------|---------------------------------|
| `CircularString`   | `org.locationtech.jts.geom.curved.CircularString`       | `LineString`                    |
| `CompoundCurve`    | `org.locationtech.jts.geom.curved.CompoundCurve`        | `LineString`                    |
| `CurvePolygon`     | `org.locationtech.jts.geom.curved.CurvePolygon`         | `Polygon`                       |
| `MultiCurve`       | `org.locationtech.jts.geom.curved.MultiCurve`           | `MultiLineString`               |
| `MultiSurface`     | `org.locationtech.jts.geom.curved.MultiSurface`         | `MultiPolygon`                  |
| `Triangle`         | `org.locationtech.jts.geom.curved.Triangle`             | `Polygon`                       |
| `PolyhedralSurface`| `org.locationtech.jts.geom.curved.PolyhedralSurface`    | `MultiPolygon`                  |
| `Tin`              | `org.locationtech.jts.geom.curved.Tin`                  | `PolyhedralSurface`             |

Plus:

- `CurvedGeometryFactory` — extends `GeometryFactory`, adds `createCircularString(...)`, `createTriangle(...)`, etc.
- `CurvedWKTReader` — extends `WKTReader`, recognises the eight new keywords via the core `readOtherGeometryText` extension hook.
- `CurvedWKTWriter` — extends `WKTWriter`. Phase-1 marker: the core writer already emits subclass keywords via `Geometry.getGeometryType().toUpperCase()`.
- `Linearizable` interface — `Geometry toLinear(double tolerance)` for converting a curved geometry into a non-curved approximation.

The naming collision with the long-standing static-utility class
`org.locationtech.jts.geom.Triangle` (centroid, circumradius, etc.) is
resolved by package separation: that utility is preserved unchanged in
core; the geometry type lives in `org.locationtech.jts.geom.curved`.

## Usage

```java
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.curved.CurvedGeometryFactory;
import org.locationtech.jts.geom.curved.Linearizable;
import org.locationtech.jts.io.curved.CurvedWKTReader;
import org.locationtech.jts.io.curved.CurvedWKTWriter;

CurvedGeometryFactory factory = new CurvedGeometryFactory();
CurvedWKTReader reader = new CurvedWKTReader(factory);

Geometry g = reader.read("CIRCULARSTRING(1 5, 6 2, 7 3)");
System.out.println(g.getGeometryType());          // CircularString

String wkt = new CurvedWKTWriter().write(g);
// wkt: CIRCULARSTRING (1 5, 6 2, 7 3)

// Linearise to a non-curved approximation
Geometry linear = ((Linearizable) g).toLinear(0.0);
System.out.println(linear.getGeometryType());     // LineString
```

The standard `WKTReader` continues to throw `ParseException("Unknown
geometry type: CIRCULARSTRING")` for the new keywords; a caller has to
opt in by instantiating `CurvedWKTReader`.

## Maven coordinates

```xml
<dependency>
  <groupId>org.locationtech.jts</groupId>
  <artifactId>jts-curved</artifactId>
  <version>1.20.1-SNAPSHOT</version>
</dependency>
```

## Phase 1 limitations

The current implementation is intentionally minimal so the module can
land alongside the core extension hooks without dragging in a years-long
algorithm program. Known limitations:

- **Spatial operations fall through to the parent type.** A
  `CircularString.intersects(g)` is computed against the polyline
  formed by the control points, not against the actual arcs. Use
  `Linearizable.toLinear(tolerance)` to make this explicit.
- **`CompoundCurve` member structure is collapsed** to a flat
  concatenation of control points on read. The writer emits this flat
  form too, and the reader accepts both the flat form and the OGC
  member-structured form on input.
- **`CurvePolygon` / `MultiSurface` round-trip degrades inner curve
  members.** Re-reading a written `MULTISURFACE(CURVEPOLYGON(...))`
  yields `MultiSurface[Polygon]` rather than
  `MultiSurface[CurvePolygon]`, because the writer does not yet emit
  inner-member tags. Tests use `Linearizable.toLinear(...)` for
  structural-fidelity comparison.
- **Validation is best-effort.** Structural rules (Triangle 4-point
  ring, CircularString odd point count, CompoundCurve member
  connectivity, Tin triangle-only patches) are not enforced.
- **No WKB support.** Defer to a follow-up phase for the SFA-MM type
  codes (8/9/10/11/12/15/16/17 with Z/M/ZM variants).
- **`copy()` preserves the subclass** for top-level types via
  overridden `copyInternal()`, but `Polygon.isEquivalentClass` is
  strict — a `Polygon` is *not* `equalsExact` to a `CurvePolygon` with
  identical coordinates. (The same comparison is lenient for
  `LineString` subclasses.) Tests work around this where it matters.
- **No `JTSTestBuilder` UI integration yet.**

## Discovery

This module deliberately does **not** register itself via
`ServiceLoader` or any other automatic-discovery mechanism. Callers
explicitly instantiate `CurvedWKTReader` / `CurvedWKTWriter` /
`CurvedGeometryFactory` when they want curve support. This keeps the
module GraalVM native-image friendly and avoids surprising other
classpath users.

## References

- Discussion: <https://github.com/locationtech/jts/discussions/1193>
- Design template: NetTopologySuite/NetTopologySuite#526
- Specification: OGC Simple Features Access 1.2.1 / ISO 19125-2
