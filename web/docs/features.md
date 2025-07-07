:::: header
![](jts_logo.png)

::: header-text
JTS Topology Suite - Features
:::
::::

::: content
A description of the features and functions provided by JTS, linked to
the relevant [Javadoc](javadoc/index.html){.javadoc} for the current
version.

### Geometry Model

- Support for all
  [Geometry](javadoc/org/locationtech/jts/geom/Geometry.html){.javadoc}
  types defined in the OGC *Simple Features for SQL* specification,
  including:
  - [Point](javadoc/org/locationtech/jts/geom/Point.html){.javadoc} and
    [MultiPoint](javadoc/org/locationtech/jts/geom/MultiPoint.html){.javadoc}
  - [LineString](javadoc/org/locationtech/jts/geom/LineString.html){.javadoc}
    and
    [MultiLineString](javadoc/org/locationtech/jts/geom/MultiLineString.html){.javadoc}
  - [Polygon](javadoc/org/locationtech/jts/geom/Polygon.html){.javadoc}
    and
    [MultiPolygon](javadoc/org/locationtech/jts/geom/MultiPolygon.html){.javadoc}
  - heterogeneous
    [GeometryCollection](javadoc/org/locationtech/jts/geom/GeometryCollection.html){.javadoc}

### Geometry Operations

- Topological
  [validity](javadoc/org/locationtech/jts/geom/Geometry.html#isValid())
  checking
- [Area](javadoc/org/locationtech/jts/geom/Geometry.html#getArea()) and
  [Length/Perimeter](javadoc/org/locationtech/jts/geom/Geometry.html#getLength())
- [Distance between
  geometries](javadoc/org/locationtech/jts/geom/Geometry.html#distance(org.locationtech.jts.geom.Geometry))
  and
  [isWithinDistance](javadoc/org/locationtech/jts/geom/Geometry.html#isWithinDistance(org.locationtech.jts.geom.Geometry,%20double)){.javadoc}
  predicate
- Spatial Predicates based on the Egenhofer DE-9IM model, including the
  named predicates:
  - [contains](javadoc/org/locationtech/jts/geom/Geometry.html#contains(org.locationtech.jts.geom.Geometry)){.javadoc},
    [within](javadoc/org/locationtech/jts/geom/Geometry.html#within(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [covers](javadoc/org/locationtech/jts/geom/Geometry.html#covers(org.locationtech.jts.geom.Geometry)){.javadoc},
    [coveredBy](javadoc/org/locationtech/jts/geom/Geometry.html#coveredBy(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [intersects](javadoc/org/locationtech/jts/geom/Geometry.html#intersects(org.locationtech.jts.geom.Geometry)){.javadoc},
    [disjoint](javadoc/org/locationtech/jts/geom/Geometry.html#disjoint(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [crosses](javadoc/org/locationtech/jts/geom/Geometry.html#crosses(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [overlaps](javadoc/org/locationtech/jts/geom/Geometry.html#overlaps(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [touches](javadoc/org/locationtech/jts/geom/Geometry.html#touches(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [equals](javadoc/org/locationtech/jts/geom/Geometry.html#equals(org.locationtech.jts.geom.Geometry)){.javadoc}

  and the general
  [relate](javadoc/org/locationtech/jts/geom/Geometry.html#relate(org.locationtech.jts.geom.Geometry)){.javadoc}
  operation returning the DE-9IM [intersection
  matrix](javadoc/org/locationtech/jts/geom/IntersectionMatrix.html).
- Overlay functions including
  - [intersection](javadoc/org/locationtech/jts/geom/Geometry.html#intersection(org.locationtech.jts.geom.Geometry)){.javadoc},
  - [difference](javadoc/org/locationtech/jts/geom/Geometry.html#difference(org.locationtech.jts.geom.Geometry)){.javadoc},
  - [union](javadoc/org/locationtech/jts/geom/Geometry.html#union(org.locationtech.jts.geom.Geometry)){.javadoc},
  - [symmetric
    difference](javadoc/org/locationtech/jts/geom/Geometry.html#symDifference(org.locationtech.jts.geom.Geometry)){.javadoc}
  - [unary
    union](javadoc/org/locationtech/jts/geom/Geometry.html#union()){.javadoc},
    providing fast union of geometry collections
- [Buffer](javadoc/org/locationtech/jts/geom/Geometry.html#buffer(double))
  computation (also known as Minkowski sum with a circle)
  - selection of different [end-cap and
    join](javadoc/org/locationtech/jts/geom/Geometry.html#buffer(double,%20int,%20int))
    styles.
- [Convex
  hull](javadoc/org/locationtech/jts/geom/Geometry.html#convexHull())
- [Geometric
  simplification](javadoc/org/locationtech/jts/simplify/package-summary.html)
  including the
  [Douglas-Peucker](javadoc/org/locationtech/jts/simplify/DouglasPeuckerSimplifier.html)
  algorithm and [topology-preserving
  simplification](javadoc/org/locationtech/jts/simplify/TopologyPreservingSimplifier.html)
- Geometric
  [densification](javadoc/org/locationtech/jts/densify/Densifier.html)
- [Linear
  referencing](javadoc/org/locationtech/jts/linearref/package-summary.html)

### Precision Handling

- Explicit coordinate [Precision
  Model](javadoc/org/locationtech/jts/geom/PrecisionModel.html)
- Geometry precision reduction

### Geometric Constructions

- [Delaunay
  triangulation](javadoc/org/locationtech/jts/triangulate/DelaunayTriangulationBuilder.html)
  and [Conforming Delaunay
  triangulation](javadoc/org/locationtech/jts/triangulate/ConformingDelaunayTriangulationBuilder.html)
- [Voronoi diagram
  generation](javadoc/org/locationtech/jts/triangulate/VoronoiDiagramBuilder.html)
- [Minimum
  Diameter](javadoc/org/locationtech/jts/algorithm/MinimumDiameter.html)
  of a geometry
- [Minimum Enclosing
  Rectangle](javadoc/org/locationtech/jts/algorithm/MinimumDiameter.html#getMinimumRectangle())
- [Minimum Bounding
  Circle](javadoc/org/locationtech/jts/algorithm/MinimumBoundingCircle.html)

### Metric Functions

- [Distance between
  geometries](javadoc/org/locationtech/jts/operation/distance/DistanceOp.html),
  with supporting points
- [Discrete
  Hausdorff](javadoc/org/locationtech/jts/algorithm/distance/DiscreteHausdorffDistance.html)
  distance
- [Area](javadoc/org/locationtech/jts/algorithm/match/AreaSimilarityMeasure.html)
  and
  [Hausdorff](javadoc/org/locationtech/jts/algorithm/match/HausdorffSimilarityMeasure.html)
  similarity measures

### Spatial algorithms

- [Robust line segment
  intersection](javadoc/org/locationtech/jts/algorithm/RobustLineIntersector.html)
- Efficient line arrangement [intersection and
  noding](javadoc/org/locationtech/jts/noding/package-summary.html)
- [Snap-rounding](javadoc/org/locationtech/jts/noding/snapround/package-summary.html)
  for noding line arrangements
- Efficient
  [Point-in-Polygon](javadoc/org/locationtech/jts/algorithm/locate/package-summary.html)
  testing

### Mathematical Functions

- [Angle](javadoc/org/locationtech/jts/algorithm/Angle.html){.javadoc}
  computation
- [Vector](javadoc/org/locationtech/jts/algorithm/VectorMath.html)
  arithmetic

### Spatial structures

- Spatial index structures including:
  - [Quadtree](javadoc/org/locationtech/jts/index/quadtree/Quadtree.html){.javadoc}
  - [STR-tree](javadoc/org/locationtech/jts/index/strtree/STRtree.html)
  - [KD-tree](javadoc/org/locationtech/jts/index/kdtree/KdTree.html)
  - [Interval
    R-tree](javadoc/org/locationtech/jts/index/intervalrtree/package-summary.html)
  - [Monotone
    Chains](javadoc/org/locationtech/jts/index/chain/package-summary.html)
- [Planar
  graphs](javadoc/org/locationtech/jts/planargraph/PlanarGraph.html) and
  [graph
  algorithms](javadoc/org/locationtech/jts/planargraph/algorithm/package-summary.html)

### Input/Output

- WKT (Well-Known Text)
  [reading](javadoc/org/locationtech/jts/io/WKTReader.html) and
  [writing](javadoc/org/locationtech/jts/io/WKTWriter.html)
- WKB (Well-Known Binary)
  [reading](javadoc/org/locationtech/jts/io/WKBReader.html) and
  [writing](javadoc/org/locationtech/jts/io/WKBWriter.html)
- GML(Geography Markup Language) Version 2
  [reading](javadoc/org/locationtech/jts/io/gml2/GMLReader.html) and
  [writing](javadoc/org/locationtech/jts/io/gml2/GMLWriter.html)
- Java Swing/AWT Shape
  [writing](javadoc/org/locationtech/jts/awt/package-summary.html)

### High-Precision Arithmetic

- [Robust evaluation of 2x2 double-precision
  determinants](javadoc/org/locationtech/jts/algorithm/RobustDeterminant.html)
- [DoubleDouble](javadoc/org/locationtech/jts/math/DD.html)
  extended-precision arithmetic
:::
