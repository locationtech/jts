# JTS Frequently Asked Questions

Last Update: September 8, 2020

<!-- 
\

A.  [General](#general)
    1.  [What Java versions does JTS work with?](#A1)
B.  [Design and Structure](#design)
    1.  [How can I use JTS algorithms with a different geometry
        model?](#B1)
    2.  [Why does JTS allow geometries to be constructed with invalid
        topology?](#B2)
    3.  [What is the difference between a Point and a Coordinate?](#B3)
    4.  [Does JTS support 3D operations?](#B4)
    5.  [What coordinate system and/or units does JTS use?](#B5)
C.  [Geometry Predicates](#predicates)
    1.  [How are spatial predicates computed?](#C1)
    2.  [Why does relate(POINT(20 20), POINT(20 30), \"FF0FFF0F2\") =
        true?](#C2)
    3.  [Why is the result of a predicate different in JTS than in
        another software application/library?](#C3)
D.  [Robustness and Precision](#robustness)
    1.  [Why is a TopologyException thrown?](#D1)
    2.  [Why does the coordinate given in a TopologyException not appear
        in the input data?](#D2)
    3.  [What is a \"robustness failure\"?](#D3)
    4.  [What is a \"topology collapse\"?](#D4)
    5.  [What is the PrecisionModel for?](#D5)
    6.  [Why does JTS not enforce the specified PrecisionModel when
        creating new geometry?](#D6)
    7.  [Why do the overlay operations not obey the axioms of set
        theory?](#D7)
    8.  [Why is the result of `intersects` inconsistent with the result
        of `intersection` ?](#D8)
    9.  [How can I prevent TopologyExceptions or incorrect results in
        overlay operations?](#D9)
E.  [Algorithms](#algorithms)
    1.  [Are there any references which describe the algorithms used in
        JTS?](#E1)
    2.  [Is there a skeletonization algorithm which works with
        JTS?](#E2)
    3.  [How can JTS split a polygon with a linestring?](#E3)
F.  [Geodetic Operations](#geodetic_operations)
    1.  [Does JTS support computation on the geodetic ellipsoid?](#F1)
    2.  [Can JTS be used to compute a geographically accurate range
        circle?](#F2)
G.  [Geometry Cleaning and Conflation](#geometry_cleaning)
    1.  [How can I correct the topology of a Polygon that JTS is
        reporting as invalid?](#G1)

\
 -->

## A. General[]{#general}

### 1. What Java versions does JTS work with?[]{#A1}

JTS is developed using Java 8. It should work with all newer versions.
With a small amount of work the library can be made to work with almost
all previous Java versions as well.\

## B. Design and Structure[]{#design}

### 1. How can I use JTS algorithms with a different geometry model?[]{#B1}

The solution to this is to use Facade objects which wrap the non-JTS
geometry model classes. In order to avoid having to create and copy
large numbers of
[Coordinate](./javadoc/org/locationtech/jts/geom/Coordinate.html){.javadoc}
objects, JTS provides the
[CoordinateSequence](./javadoc/index.html?org/locationtech/jts/geom/CoordinateSequence.html){.javadoc}
interface. A CoordinateSequence-based adapter can be written for
whatever structure the foreign model uses to represent sequences of
points. JTS
[Geometry](./javadoc/org/locationtech/jts/geom/Geometry.html){.javadoc}
objects will still need to be created to represent the structure of the
geometries containing the points, but these are relatively lightweight
in comparison.\

### 2. Why does JTS allow geometries to be constructed with invalid topology?[]{#B2}

JTS intentionally allows topologically invalid geometries to be
constructed for the following reasons:\

1.  It allows a wider set of geometry to be read, stored and written
    from external data sources
2.  It allows geometries to be constructed and then validated
3.  It avoids the costly overhead of validating topology every time a
    geometry is constructed

### 3. What is the difference between a Point and a Coordinate?[]{#B3}

A
[Coordinate](./javadoc/org/locationtech/jts/geom/Coordinate.html){.javadoc}
is a relatively simple class which represents a location on the
Cartesian plane (optionally with an associated height value).
[Coordinate](./javadoc/org/locationtech/jts/geom/Coordinate.html){.javadoc}s
are usually treated as mutable objects, in order to simplify certain
algorithms.\
\
A [Point](./javadoc/org/locationtech/jts/geom/Point.html){.javadoc} is a
subclass of 
[Geometry](./javadoc/org/locationtech/jts/geom/Geometry.html){.javadoc}
that also represents a location on the Cartesian plane. It is a
\"heavy-weight\" object (which for instance may contain an envelope)
which support all methods that apply to
[Geometry](./javadoc/org/locationtech/jts/geom/Geometry.html){.javadoc}s.\

### 4. Does JTS support 3D operations?[]{#B4}

JTS does not provide support for true 3D 
[Geometry](./javadoc/org/locationtech/jts/geom/Geometry.html){.javadoc}
and operations. However, JTS does allow
[Coordinate](./javadoc/org/locationtech/jts/geom/Coordinate.html){.javadoc}s
to carry an elevation or Z value. This does not provide true 3D support,
but does allow \"2.5D\" uses which are required in some geospatial
applications.\

### 5. What coordinate system and/or units does JTS use?

JTS uses the implicit coordinate system of the input data. The only
assumption it makes is that the coordinate system is infinite, planar
and Euclidean (i.e. rectilinear and obeying the standard Euclidean
distance metric). In the same way JTS does not specify any particular
units for coordinates and geometries. Instead, the units are implicitly
defined by the input data provided. This implies that in most cases
input geometries to operations should be defined with the same
coordinate system.\

## C. Geometry Predicates[]{#predicates}

### 1. How are spatial predicates computed?[]{#C1}

The two input geometries are decomposed into labelled topology graphs
([GeometryGraph](./javadoc/org/locationtech/jts/geomgraph/GeometryGraph.html){.javadoc}s).
The labels are on the nodes and edges of the graphs. They contain full
information about the topology of the node/edge in the
points/lines/polygons of the original geometry. The labelled topology
graphs are merged. This includes merging the labels wherever there is
common nodes or edges. For each geometry at each node, the label
information is propagated to all edges incident on that node. The
resulting relationship (Intersection Matrix, or IM) is determined by the
merged label information at the nodes of the merged graph. The labelling
of each node and its incident edges is inspected, and the topological
relationship information it contributes is added to the overall IM. At
the end of this process the IM has been completely determined.\

### 2. Why does relate(POINT(20 20), POINT(20 30), \"FF0FFF0F2\") = true?[]{#C2}

According to the SFS 1.1, section 2.1.3:\
\
        *The boundary of a Point is the empty set*\
\
Since points do not have boundaries, all the intersection matrix entries
relating to the geometry boundaries are \"F\".\
\
In some situations it is desirable to use a different definition for
determining whether geometry endpoints are on their boundary. To support
this, JTS provides the ability to specify a custom
[BoundaryNodeRule](./javadoc/org/locationtech/jts/algorithm/BoundaryNodeRule.html){.javadoc}
to the
[RelateOp](./javadoc/org/locationtech/jts/operation/relate/RelateOp.html){.javadoc}
class.\

### 3. Why is the result of a predicate different in JTS than in another software application/library?[]{#C3}

This is usually due to the fact that JTS predicates are computed
exactly, using the full precision of the double-precision coordinates.
Other geometry engines sometimes compute in lower precision, or round
input coordinates, or use a tolerance when determining whether two lines
intersect or cross.\
\
As a specific example, in the following case:\
\

::: wkt
A: POLYGON ((1368.62186660165 17722.3281808793, -1653 9287.5,
4038.14058906538 8613.02390521266, 1368.62186660165 17722.3281808793))
:::

::: wkt
B: POLYGON ((-5846 9287.5, 7453 8380, 9082 16600, -6326.5 18842, -5846
9287.5))
:::

\
JTS reports `A.overlaps(B) = true`{.wkt}, whereas another application
reports false. The
[Overlaps](./javadoc/org/locationtech/jts/geom/Geometry.html#overlaps%28org.locationtech.jts.geom.Geometry%29){.javadoc}
result is correct - the bottom right point in the triangle B lies
outside the quadrilateral A. This is demonstrated by intersecting the
bottom edge of A\
\

::: wkt
LINESTRING (-5846 9287.5, 7453 8380)
:::

\
with B. The value of the intersection is a line segment:\
\

::: wkt
LINESTRING (4038.140589065375 8613.02390521266, 4038.14058906538
8613.02390521266)
:::

\
which shows that B crosses the boundary of A, and thus overlaps A.\

## D. Robustness & Precision[]{#robustness}

### 1. Why is a TopologyException thrown?[]{#D1}

[TopologyException](./javadoc/org/locationtech/jts/geom/TopologyException.html){.javadoc}s
are thrown when JTS encounters an inconsistency in the internal topology
structures it creates to compute certain spatial operations (in
particular, **spatial predicates** and **overlay operations**). These
inconsistencies can happen for two reasons:\

1.  **Invalid input geometry**. If input geometry is invalid according
    to the JTS (and OGC SFS) model, the results of operations is
    undefined, and may produce exceptions.
    [Geometry](./javadoc/org/locationtech/jts/geom/Geometry.html){.javadoc}
    validity can be checked by using the
    [isValid()](./javadoc/org/locationtech/jts/geom/Geometry.html#isValid--){.javadoc}
    method.
2.  **Robustness failure** due to floating-point roundoff errors.
    Floating-point errors can cause incorrect results to be computed for
    internal operations (such as computing point-line orientation,
    computing the intersection of two line segments, or computing the
    noded arrangement of a set of line segments).

Both of these situations can cause the topological properties of
computed geometries to become inconsistent. When this issue is detected
JTS throws a
[TopologyException](./javadoc/org/locationtech/jts/geom/TopologyException.html){.javadoc}.\
\
In some rare cases, it is not possible to recognize an inconsistent
topological situation. In these cases, no exception will be thrown, but
the returned geometry will not correctly reflect the true result of the
operation. JTS contains special checks to detect and prevent this from
occurring for the overwhelming majority of inputs, however.\
\
Unfortunately there is no guaranteed way of avoiding
[TopologyException](./javadoc/org/locationtech/jts/geom/TopologyException.html){.javadoc}s.
However, a heuristic which often helps is to ensure that input geometry
coordinates do not carry excessive precision. Instead of providing
coordinates with a full 16 digits of precision (which usually far
exceeds the actual accuracy of the input data), try reducing precision
to a few decimal places. Of course, correct geometry topology must still
be maintained. (This is primarily an issue for polygons, and can be
tricky to do in some pathological cases). JTS provides the
[SimpleGeometryPrecisionReducer](./javadoc/org/locationtech/jts/precision/SimpleGeometryPrecisionReducer.html./javadoc/org/locationtech/jts/precision/SimpleGeometryPrecisionReducer.html){.javadoc}
class to do a simple reduction in coordinate precision, although this
class is not guaranteed to maintain correct geometry topology.\

### 2. Why does the coordinate given in a TopologyException not appear in the input data?[ ]{#D2}

In order to reduce robustness problems during overlay operations,
JTS/[GEOS](https://trac.osgeo.org/geos/){.javadoc} sometimes transforms
geometry into a different coordinate system. The coordinates in a
[TopologyException](./javadoc/org/locationtech/jts/geom/TopologyException.html){.javadoc}
message are presented in the working coordinate system, rather than the
input coordinate system. This may not match the input data.\

### 3. What is a \"robustness failure\"?[]{#D3}

A **robustness failure** is a situation in which a JTS operation on
valid inputs either fails to complete (by throwing an exception) or
produces an incorrect answer. This situation is usually caused by the
unavoidable internal finite-precision arithmetic causing round-off
error, which in turn causes invalid geometric topology to be created at
some point during the evaluation of the algorithm.\
\
The operations which are notably susceptible to robustness errors are
the overlay operations (intersection, union, difference and
symDifference). The input geometries which are most likely to trigger
this behavior are ones which contain a lot of precision (e.g. 14-16
significant digits of precision), and/or ones which contain line
segments which are nearly, but not exactly, coincident.\

### 4. What is a \"topology collapse\"?[]{#D4}

A **topology collapse** is a situation in which the finite-precision
numerical representation used in JTS (Java\'s IEEE-754 double-precision
floating point) is unable to accurately represent a particular geometric
configuration exactly. This causes vertices to be slightly shifted from
their mathematically exact position. In certain geometric
configurations, this can result in the computed geometry being
topologically invalid.\
\
Typically this occurs in situations where polygon vertices are very
close to other line segments. If the vertex is shifted slightly it may
cross the line segment, resulting in a ring which self-intersects.\

### 5. What is the PrecisionModel for?[]{#D5}

The PrecisionModel specifies the precision of the coordinates used to
define geometries. Note that JTS expects that coordinates are supplied
already rounded to the desired precision model; it does not perform this
automatically during geometry creation.\
For some operations the Precision Model also specifies the precision in
which computation is performed, and in which computed results are
constructed. However, this is not uniform across all operations. For
instance, the the overlay and buffer operations **do** obey the
precision model, but the spatial predicates do not.\

### 6. Why does JTS not enforce the specified PrecisionModel when creating new geometry?[]{#D6}

The PrecisionModel specified in a GeometryFactory is not applied to the
coordinates supplied when creating geomtries with the factory. This is
because:

1.  Changing the precision of coordinates is in general a non-trivial
    operation, since it can cause topology collapse (see [D4](#D4).
2.  Changing coordinate values adds significant overhead, since
    `CoordinateSequence`s may not be mutable, and thus would require a
    full copy being made
3.  Commonly the input is already precise, and thus changing precision
    is not required

\

### 7. Why do the overlay operations not obey the axioms of set theory?[]{#D7}

This is sometimes posed as:\
\
\"Why is the `intersection` of two geometries not contained in either of
the originals?\"\
\
or: \"Why does the `union` of two geometries not contain either of the
originals?\"\
\
or: \"Why does `A union (B difference A) != A `{.wkt}?\"\
\
The axioms of geometric set theory apply in a theoretical world in which
all arithmetic is carried out exactly with infite precision real
numbers. In this world operations such as union and intersection are
exact, which in turn means that they are commutative and associative.
This allows equations such as `A union (B difference A) = A `{.wkt} to
hold.\
\
JTS only approximates this ideal by simulating it using finite-precision
floating point arithmetic. JTS uses double-precision floating point
numbers to represent the coordinates of geometries (specifically,
IEEE-754 double-precision floating point, which provides 56 bits of
precision). This provides the illusion of computing using real numbers -
but it\'s only an illusion. The finite representation of real numbers
forces rounding to take place during arithmetic computation. This means
that operations are not commutative or associative. This in turn has the
effect that geometric axioms are not maintained. (For the same reason,
as is well known and documented, finite-precision floating-point
computation does not fully obey the axioms of arithmetic.)\
\
Furthermore, JTS contains code which adjusts input geometries in small
ways in order to try and prevent robustness errors from occuring. These
minor perturbations may also result in computed results which do not
necessarily obey the set theory axioms.\
\
However, a major JTS design goal is that the output of geometric
operations is \"close\" to the theoretically correct result (using some
small epsilon of tolerance and a suitable geometric distance metric.)
This is the best that can be achieved under the finite-precision
paradigm. This goal is generally met by the JTS algorithms. Moreover,
the precision of JTS geometric operations is almost always much greater
than the inherent accuracy of the input data.\

### 8. Why is the result of `intersects` inconsistent with the result of `intersection` ?[]{#D8}

The inconsistent case is:

    A = LINESTRING(0.0 0.0, -10.0 1.2246467991473533E-15)
    B = LINESTRING(-9.999143275740073 -0.13089595571333978, -10.0 1.0535676356486768E-13)

This case produces the following inconsistent results:

    A.intersects(B) = false
    A.intersection(B) = POINT (-10 0.0000000000000012)

This is a specific case of D7 above. It is interesting because it shows
how simple geometric cases can reveal the limitations of
finite-precision binary floating-point arithmetic. It also highlights
the impact of design choices made in JTS. Specifically, JTS computes
spatial predicates (including `intersects`) using high-precision
arithemtic. This determines the exact spatial relationship of the input
geometries. In contrast, the overlay operations (including
`intersection`) use standard double-precision arithmetic to compute
intersection points, and the computed point is necessarily represented
in double-precision. This has the effect that there are cases where the
results of spatial predicates is not be consistent with the result of
overlay operations.\

### 9. How can I prevent TopologyExceptions or incorrect results in overlay operations?[]{#D9}

[TopologyException](./javadoc/org/locationtech/jts/geom/TopologyException.html){.javadoc}s
and incorrect results encountered during overlay computations are
symptoms of robustness issues. Robustness issues are caused by the
limitations of using finite-precision numerics in geometric algorithms.\
\
Currently the surest way to prevent robustness issues is to limit the
numerical precision of the input geometries to something less than the
available 16 digits. To be safe, the precision of the input geometry
coordinates should be no more than 14 decimal digits (and possibly as
few as 10 or 12). This is still plenty of precision to represent the
accuracy of real-world data.\
\
Reducing the precision of the input data means that result vertices will
not perfectly match the input ones. Thus this technique is particularly
useful in situations where it is not necessary to perfectly preserve
vertex-to-vertex faithfulness to the source geometry. Example use cases
are:\

- the result is only used to obtain derived quantities such as area or
  length
- the result is only used for visualization purposes
- the result vertices do not need to fully match the input

\
[Coordinate](./javadoc/org/locationtech/jts/geom/Coordinate.html){.javadoc}
precision can be controlled in several ways:\

- the best way is to ensure that the original source of the input
  geometries provides only as much precision as is really required. If
  this is not possible to enforce, then it will be necessary to reduce
  the precision of the geometries once they are created.
- the 
  [SimpleGeometryPrecisionReducer](./javadoc/org/locationtech/jts/precision/SimpleGeometryPrecisionReducer.html./javadoc/org/locationtech/jts/precision/SimpleGeometryPrecisionReducer.html){.javadoc}
  class can be used to reduce the precision of geometry coordinates.
  Note that this class operates in a point-wise fashion, and thus in
  some situations may not maintain correct polygonal topology. If this
  is an issue, see the following item.
- the
  [GeometryPrecisionReducer](./javadoc/org/locationtech/jts/precision/GeometryPrecisionReducer.html){.javadoc}
  reduces geometry coordinate precision, and attempts to detect and
  correct invalid polygonal topology resulting from precision reduction.

## E. Algorithms[]{#algorithms}

### 1. Are there any references which describe the algorithms used in JTS?[]{#E1}

Many of the details of JTS algorithms (particularly in the areas of
performance and robustness) are unique to JTS. However, the general
design of the algorithms for computing spatial predicates and spatial
overlay follow a generally accepted strategy for computing with
2-dimensional planar linear topological structures. Some papers which
present similar approaches are:\

- E. Chan, J. Ng. **A General and Efficient Implementation of Geometric
  Operators and Predicates**; *Proceedings of the 5th International
  Symposium on Advances in Spatial Databases, 1997.*
- Schutte, Klamer. **An edge-labeling approach to concave polygon
  clipping**; *submitted to ACM Transactions on Graphics, 1995.*
- M. V. Leonov and A. G. Nikitin. **An Efficient Algorithm for a Closed
  Set of Boolean Operations on Polygonal Regions in the Plane (draft
  English translation).** *A. P. Ershov Institute of Informatics
  Systems, Preprint 46, 1997.*
- Vatti, B.R. **A Generic Solution to Polygon Clipping**;
  *Communications of the ACM, 35(7), July 1992, pp.56-63.*

### 2. Is there a skeletonization algorithm which works with JTS?[]{#E2}

Yes. See the Refractions Research Skeletonizer\

### 3. How can JTS split a polygon with a linestring?[]{#E3}

Currently JTS does not contain a **Split Polygon By Line** operation.
Thus the only way to do this is to construct a \"splitting\" polygon
which contains the linestring and surrounds one side of the target
polygon without touching it. Then the overlay operations `intersection`
and `difference` can be used to extract the two sides of the target
polygon. Constructing the splitting polygon is obviously easier when the
linestring is a straight line; and simplest if it is horizontal or
vertical.

## F. Geodetic Operations[]{#geodetic_operations}

### 1. Does JTS support computation on the geodetic ellipsoid?[]{#F1}

No. JTS currently assumes that geometries are defined in a Cartesian,
planar, 2-dimensional space. Thus it cannot be used to compute accurate
metrics, predicates or constructions on the geodetic ellipsoid which is
usually used to model the surface of the Earth.\
\
It is hoped to provide geodetic operations in a future version.\

### 2. Can JTS be used to compute a geographically accurate range circle?[]{#F2}

A geographically accurate range circle is a shape on the ellipsoid
modelling the surface of the Earth which represents all points which are
a given distance from a fixed point on the ellipsoid. This is a more
complicated shape than either a circle or even an ellipsoid. In general
JTS cannot compute this shape, since JTS assumes a Cartesian coordinate
system (i.e. a two-dimensional plane extending infinitely in all
directions). This is obviously not a good approximation to the surface
of the ellipsoid, except over very small distances. Computing a true
range circle requires complex spherical mathematics as well as a richer
coordinate system model. This is outside the current scope of JTS.\

## G. Geometry Cleaning and Conflation[]{#geometry_cleaning}

### 1. How can I correct the topology of a Polygon that JTS is reporting as invalid?[]{#G1}

- Compute
  [polygon.buffer(0)](./javadoc/org/locationtech/jts/geom/Geometry.html#buffer-double-){.javadoc}.
  The buffer operation is fairly insensitive to topological invalidity,
  and the act of computing the buffer can often resolve minor issues
  such as self-intersecting rings. However, in some situations the
  computed result may not be what is desired.
- If holes are overlapping the shell or other holes, create individual
  polygons from the shell and all the holes, and then subtract the holes
  from the shell.
:::::::
