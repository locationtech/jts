# JTS Topology Suite - Features

A description of the features and functions provided by JTS, linked to
the relevant [Javadoc](https://locationtech.github.io/jts/javadoc/index.html){.javadoc} for the current
version.

## Geometry Model

- Support for all
  [Geometry](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html){.javadoc}
  types defined in the OGC *Simple Features for SQL* specification,
  including:
  - [Point](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Point.html){.javadoc} and
    [MultiPoint](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/MultiPoint.html){.javadoc}
  - [LineString](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/LineString.html){.javadoc}
    and
    [MultiLineString](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/MultiLineString.html){.javadoc}
  - [Polygon](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Polygon.html){.javadoc}
    and
    [MultiPolygon](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/MultiPolygon.html){.javadoc}
  - heterogeneous
    [GeometryCollection](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/GeometryCollection.html){.javadoc}

## Geometry Operations

- Topological
  [validity](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#isValid())
  checking
- [Area](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#getArea()) and
  [Length/Perimeter](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#getLength())
- [Distance between
  geometries](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#distance(org.locationtech.jts.geom.Geometry))
  and
  [isWithinDistance](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#isWithinDistance(org.locationtech.jts.geom.Geometry,%20double)){.javadoc}
  predicate
- Spatial Predicates based on the Egenhofer DE-9IM model, including the
  named predicates:
  - [contains](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#contains(org.locationtech.jts.geom.Geometry)){.javadoc},
    [within](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#within(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [covers](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#covers(org.locationtech.jts.geom.Geometry)){.javadoc},
    [coveredBy](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#coveredBy(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [intersects](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#intersects(org.locationtech.jts.geom.Geometry)){.javadoc},
    [disjoint](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#disjoint(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [crosses](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#crosses(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [overlaps](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#overlaps(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [touches](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#touches(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [equals](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#equals(org.locationtech.jts.geom.Geometry)){.javadoc}

  and the general
  [relate](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#relate(org.locationtech.jts.geom.Geometry)){.javadoc}
  operation returning the DE-9IM [intersection
  matrix](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/IntersectionMatrix.html).
- Overlay functions including
  - [intersection](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#intersection(org.locationtech.jts.geom.Geometry)){.javadoc},
  - [difference](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#difference(org.locationtech.jts.geom.Geometry)){.javadoc},
  - [union](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#union(org.locationtech.jts.geom.Geometry)){.javadoc},
  - [symmetric
    difference](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#symDifference(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [unary
    union](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#union()){.javadoc},
    providing fast union of geometry collections
- [Buffer](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#buffer(double))
  computation (also known as Minkowski sum with a circle)
  - selection of different [end-cap and
    join](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#buffer(double,%20int,%20int))
    styles.
- [Convex
  hull](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Geometry.html#convexHull())
- [Geometric
  simplification](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/simplify/package-summary.html)
  including the
  [Douglas-Peucker](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/simplify/DouglasPeuckerSimplifier.html)
  algorithm and [topology-preserving
  simplification](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/simplify/TopologyPreservingSimplifier.html)
- Geometric
  [densification](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/densify/Densifier.html)
- [Linear
  referencing](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/linearref/package-summary.html)

## Precision Handling

- Explicit coordinate [Precision
  Model](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/PrecisionModel.html)
- Geometry precision reduction

## Geometric Constructions

- [Delaunay
  triangulation](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/triangulate/DelaunayTriangulationBuilder.html)
  and [Conforming Delaunay
  triangulation](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/triangulate/ConformingDelaunayTriangulationBuilder.html)
- [Voronoi diagram
  generation](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/triangulate/VoronoiDiagramBuilder.html)
- [Minimum
  Diameter](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/MinimumDiameter.html)
  of a geometry
- [Minimum Enclosing
  Rectangle](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/MinimumDiameter.html#getMinimumRectangle())
- [Minimum Bounding
  Circle](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/MinimumBoundingCircle.html)

## Metric Functions

- [Distance between
  geometries](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/operation/distance/DistanceOp.html),
  with supporting points
- [Discrete
  Hausdorff](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/distance/DiscreteHausdorffDistance.html)
  distance
- [Area](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/match/AreaSimilarityMeasure.html)
  and
  [Hausdorff](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/match/HausdorffSimilarityMeasure.html)
  similarity measures

## Spatial algorithms

- [Robust line segment
  intersection](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/RobustLineIntersector.html)
- Efficient line arrangement [intersection and
  noding](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/noding/package-summary.html)
- [Snap-rounding](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/noding/snapround/package-summary.html)
  for noding line arrangements
- Efficient
  [Point-in-Polygon](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/locate/package-summary.html)
  testing

## Mathematical Functions

- [Angle](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/Angle.html){.javadoc}
  computation
- [Vector](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/VectorMath.html)
  arithmetic

## Spatial structures

- Spatial index structures including:
  - [Quadtree](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/index/quadtree/Quadtree.html){.javadoc}
  - [STR-tree](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/index/strtree/STRtree.html)
  - [KD-tree](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/index/kdtree/KdTree.html)
  - [Interval
    R-tree](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/index/intervalrtree/package-summary.html)
  - [Monotone
    Chains](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/index/chain/package-summary.html)
- [Planar
  graphs](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/planargraph/PlanarGraph.html) and
  [graph
  algorithms](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/planargraph/algorithm/package-summary.html)

## Input/Output

- WKT (Well-Known Text)
  [reading](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/io/WKTReader.html) and
  [writing](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/io/WKTWriter.html)
- WKB (Well-Known Binary)
  [reading](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/io/WKBReader.html) and
  [writing](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/io/WKBWriter.html)
- GML(Geography Markup Language) Version 2
  [reading](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/io/gml2/GMLReader.html) and
  [writing](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/io/gml2/GMLWriter.html)
- Java Swing/AWT Shape
  [writing](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/awt/package-summary.html)

## High-Precision Arithmetic

- [Robust evaluation of 2x2 double-precision
  determinants](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/RobustDeterminant.html)
- [DoubleDouble](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/math/DD.html)
  extended-precision arithmetic
