
JTS TOPOLOGY SUITE
==================

*Version History*

This document lists the change history of release versions of the
**JTS Topology Suite**.

* Project site: [LocationTech JTS](http://locationtech.org/projects/technology.jts)
* Code repo: [Github JTS](https://github.com/locationtech/jts)
* Distribution: [Github JTS Releases](https://github.com/locationtech/jts/releases)


Distributions for older JTS versions can be obtained at the
[SourceForge JTS project](http://sourceforge.net/projects/jts-topo-suite/)

<!-- ================================================================ -->

# Version 1.18.0

*Release Date:  TBD*

### Functionality Improvements

* Improve Orientation.isCCW to handle flat topology collapse (#588)
* Add `KMLReader` (#593)
* Add `Densifier.setValidated` method to allow disabling expensive polygon validation (#595)
* Add `OverlayNG` codebase (#599)
* Add Z support in OverlayNG (#645)
* Add system property `jts.overlay=ng` to enable use of OverlayNG in `Geometry` methods (#615)
* Add `SnapRoundingNoder` (#599)
* Add `SnappingNoder` (#599)
* Change `GeometryPrecisionReducer` to use OverlayNG with Snap-Rounding
* Change `GeometryNoder` to use `SnapRoundingNoder`
* Add `KdTree` `size` and `depth` methods (#603)
* Improve `WKBWriter` to write empty Polygons using a more compact representation (#623)
* Support read and initialize internal structure of `STRtree` and `Quadtree` (#634)
* Improve `GeometryPrecisionReducer` to handle GeometryCollections (#648)

### Performance Improvements

* Improve performance of `UnaryUnionOp` by removing OverlayUnion optimization (#644)

### Bug Fixes

* Fix `RayCrossingCounter` to handle XYZM coordinates (#589)
* Fix `PackedCoordinateSequence` to always use XYZM coordinates when dimension is 4 (#591)
* Fix `OrdinateFormat` to work around a JDK issue with the minus sign character in `Locale.NO` (#596)
* Fix `GeoJsonReader` to throw a `ParseException` for empty arrays (#600)
* Fix `WKTFileReader` handling of files with large amount of whitespace (#616)
* Fix `WKBWriter` to output 3D empty Points with 3 ordinates (#622)
* Fix `Geometry.reverse` to handle all geometry structures (#628)

## JTS TestBuilder

### Functionality Improvements

* Add Geometry Inspector sorting by Area or Length

<!-- ================================================================ -->

# Version 1.17.1

*Release Date:  August 27, 2020*

*Java Version: 1.8*

### Functionality Improvements

* Add `WKBReader` and `WKBWriter` support for `POINT EMPTY` (#567)

### Performance Improvements

* Improve performance of `PreparedPolygon` `covers` and `contains` for point inputs (#577)

### Bug Fixes

* Fix `IndexedPointInAreaLocator` thread-safety (#572)
* Fix `WKTReader` to handle MultiPoints containing `EMPTY` (#575)
* Fix API compile regression by removing deprecation on geometry `reverse` methods (#582)

## JTS TestBuilder

### Functionality Improvements

* Add per-Layer palette control for Strokes and Fills

## JTS TestRunner

### Functionality Improvements

* Enhance `-geomfunc` to load multiple function classes
* Fix function registry to replace matching loaded functions (#569)

## JtsOp 

* Added `-limit` and `-offset` options for reading from file inputs (#617)


<!-- ================================================================ -->

# Version 1.17.0

*Release Date:  July 1, 2020*

*Java Version: 1.8*

### API Changes

* Change `Polygon` `getExteriorRing` and `getInteriorRingN` accessors to return `LinearRing`. 
  * *This is a binary incompatible change to the method signature.  Recompilation is necessary. No source code changes are required.*

### Functionality Improvements

* Added `IndexedFacetDistance.isWithinDistance`
* Added `OrdinateFormat` to ensure that ordinate text output is accurate and consistent
* Added `Triangle.circumcentreDD`
* Added `DD.determinant` methods
* Added `Envelope` methods `getDiameter`, `copy`, `disjoint` (#483)
* Added `Intersection` class, refactored library to use it (#468)
* Added `CascadedPolygonUnion` union-by-buffer on error capability (#470)
* Added `HalfEdge` support for direction points (#479)
* Added `CoordinateList.toCoordinateArray(isForward)` (#482)
* Addded `HPRtree` Hilbert Packed R-tree (#494)
* Added `VariableBuffer` class for computing varying-distance buffers (#495)
* Added `LineSegment.reflect` method (#495)
* Added `MaximumInscribedCircle` algorithm (#530)
* Added `LargestEmptyCircle` algorithm (#530)

### Performance Improvements

* Improve performance of `UniqueCoordinateFilter` (#422)
* Improve performance of `Polygonizer` (#431)
* Avoid use of `ArrayList` in MonotoneChain builders
* Add DistanceOp line-line envelope short-circuit optimizations (#534)

### Bug Fixes

* Fix `PackedCoordinateSequence.Float` construction methods (#379, #381)
* Fix bug in `Quadtree.ensureExtent` (#416)
* Fix bugs in `LinearLocation` endpoint handling (#421)
* Fix bug in `MinimumBoundingCircle` maximum diameter algorithm, and provide method for it
* Improve robustness of `CascadedPolygonUnion` by adding `OverlapUnion`
* Fix bug in `HalfEdge.insert` method which caused CCW order not to be preserved in some cases
* Fix generation of Voronoi diagrams for cases with sites in a square (#447)
* Fix use of clipping envelope in `VoronoiDiagramBuilder`
* Fix infinite loop on empty input in `IndexedPointInAreaLocator` and `SortedPackedIntervalRTree` (#462) 
* Fix WKT parsing in Turkish locale (#456)
* Improve accuracy of `LineSegment.lineIntersection` (#468)
* Fix `Distance3DOp` coordinate ordering (#480) 
* Fix `Geometry.reverse()` to have consistent behaviour and to copy all fields (#513)
* Fix `MinimumBoundingCircle.farthestPoints` to work correctly (#522 and #533)
* Fix `DistanceOp` handling of geometry collections with empty components (#524)
* Fix GML parsing of coordinates and SRS name (#553)

## JTS TestBuilder

### Functionality Improvements

* Add a UI to run external commands
* Allow creating additional view layers
* Add map view title, legend and border options
* Support points in Reveal Topology mode
* Add WKT panel **Copy as WKB** via Ctl-click

## JTS TestRunner

### Functionality Improvements

* Allow test files/dirs to be specified as free args
* Only load `.xml` files from directories

## JtsOp 

* Added command-line utility to run JTS operations



<!-- ================================================================ -->

# Version 1.16.1

*Release Date:  Febuary 19, 2019*

### Functionality Improvements

* Added `HilbertCode` and `HilbertCurveBuilder`.
* Added `MortonCode` and `MortonCurveBuilder`.
* Improved `InteriorPointArea` algorithm performance and robustness
* Add `IndexedFacetDistance` methods `nearestLocations` and `nearestPoints`
* Make `IndexedFacetDistance` thread-safe
* Add `SimplePointInAreaLocator` envelope check

### Bug Fixes

* Fix `IsValidOp` to handle empty components.
* Fix `ShapeWriter` to handle Polygons with empty holes.
* Fix `CoordinateArraySequence` to duplicate coordinate array if needed to make coordinates consistent (rather than fix in place)
* Fix `AffineTransformation.rotate`

## JTS TestBuilder

### Functionality Improvements

* Added per-layer style control
* Change UI terminology to **Reveal Topology**
* Add undo capability
* Add more functions to **Delete Components** tool
* Add **Save Image to PNG** button
* Add **Scalar Function** panel logging
* Add Pan capability to **Zoom Tool**
* Improve arrow styling
* Add meta-function options **Each** and **Repeat**

<!-- ================================================================ -->

# Version 1.16

*Release Date:  September 10, 2018*

### API Changes

* Added XYZM support to `CoordinateSequence` and `CoordinateSequenceFactory` with `getDimension()` and `getMeasures()` information.
* Added `Coordinate` methods for `getX()`, `getY()`, `getZ()`, and `getM()`.
* Deprecated `Coordinate.z` field.  Use `Coordinate.getZ()`.
* `Coordinate` subclasses introduced for XY, XYM, XYZM representations.


### Functionality Improvements

* Removed `PackedCoordinateSequenceFactory` constructor used to supply a default dimension.  Use appropriate `create( size, dimension )` instead.
* Added `WKTReader` and `WKTWriter` support for measures


<!-- ================================================================ -->

# Version 1.15

*Release Date:  November 30, 2017*

### Project Changes

* Changed licensing to dual-licensed EDL or EPL
* Changed source hosting to GitHub
* Changed distro hosting to GitHub
* Changed build chain to use Maven
* Changed code module structure


### API Changes

* Changed Java package root to `org.locationtech.jts`
* Refactored `CGAlgorithms` into function-specific classes
    `Orientation`, `PointLocation`, `Distance`, `Length`, `Area`
* Deprecated `NonRobustCGAlgorithms`; use function-specific classes refactored from `CGAlgorithms`
* Deprecated `RobustDeterminant`; use `CGAlgorithmsDD`
* Deprecated `Geometry.clone`; use `Geometry.copy`
* Deprecated `GeometryFactory.createX(null)` methods; Use no-argument `GeometryFactory.createX` methods
* Deprecated overloaded `GeometryFactory.createMultiPoint`; use `GeometryFactory.createMultiPointFromCoords`


### Functionality Improvements

* Improve `Quadtree` to handle queries with null envelopes
* Add `STRtree` K-Nearest Neighbours query
* Add `Serializable` to ``PackedCoordinateSequence`
* Add `Envelope.intersects`
* Add `Geometry.intersects` for `GeometryCollection`
* Improve `WKBReader` to handle the OGC 06-103r4 specification.
* Improve `WKTReader` to handle `Z`, `M`, `ZM` modifiers.


### Performance & Robustness Improvements

* Add optimization for `Geometry.contains` and `Geometry.covers`
* Improve robustness of `RayCrossingCounter` and `Geometry.contains`
* Improve robustness of `MultiPolygon` centroid computation


### Bug Fixes

* Eliminated `LineString.normalize` side-effects


<!------- TestBuilder -------------------->

## JTS TestBuilder

### Functionality Improvements

* Added function `Writer.writeGeoJSON`
* Added tree view of scalar functions
* Added ability to repeat functions
* Added custom fill styling
* Added `SelectionFunctions` for metrics (area, length)
* Added function documentation driven by annotations
* Some changes to layout of GUI elements (such as toolbar)


<!-- ================================================================ -->

# Version 1.14

*Release Date:  September 23, 2015*

### Functionality Improvements

* Added `Envelope.compareTo` method
* Fixed `SegmentSetMutualIntersector` classes to be thread-safe
* Fixed `PreparedGeometry` classes to be thread-safe
* Added `LineDissolver` class
* Added `edgegraph` package
* Added `CoordinateSequences.isEqual` method
* Extended `CoordinateSequences.copy` and `CoordinateSequences.copy` to handle inputs of different dimension
* Added `CoordinateArrays.envelope()` function
* Added `CoordinateArrays.intersection()` function
* Added `Polygonizer.setCheckValidRings()` method for optimizing performance in some situations
* Added `KMLWriter` to convert Geometry to KML representation
* Added `Coordinate` `equals3D`, `equals2D(Coordinate, tolerance)`, and `equalInZ` methods.
* Added `VWSimplifier` to perform Visvalingam-Whyatt simplification
* Enhanced `WKBReader` to handle Spatialite WKB format
* Added `DD.setValue()` methods
* Added `getGeometry()` methods to `LinearComponentExtracter` and `LineStringExtracter`
* Added `BufferParameters` `simplifyFactor` setting
* Added node counting and ability to not keep intersection points to `InteriorIntersectionFinder`
* Added `Polygonizer` functionality to extract only polygons forming a valid polygonal geometry
* Added `KdTree.toCoordinates`
* Added `MinimumBoundingCircle.getFarthestPoints` method


### Performance Improvements

* Performance & memory improvement in `PreparedPolygonIntersects` by short-circuiting point input case
* Allow for memory usage optimization in `CascadedPolygonUnion`, by avoiding retaining input collection
* Fix `Point.isEmpty()` to avoid allocating a coordinate
* Fix `Geometry.equalsExact()` to short-circuit when the inputs are identical objects
* Add short-circuit to `PointExtracter` to improve performance for Points


### Bug Fixes

* Fixed `RobustLineIntersector` heuristic for handling invalid intersection points
(computed as outside envelope of input segments due to numeric precision issues)
* Fixed `CGAlgorithmsDD.intersection` to compute intersection correctly
* Fixed `Geometry.interiorPoint()` to compute a true interior point in certain cases
* Fixed `Geometry.equals` to handle null argument
* Fixed `DistanceToPoint` to be thread-safe
* Fixed `Geometry.interiorPoint()` and `InteriorPointArea` to handle zero-area geometries
* Fixed `STRtree` classes to be thread-safe (by synchronizing the `AbstractSTRtree.build()` method)
* Fixed `STRtree.remove()` method to avoid a NPE
* Fixed `DouglasPeuckerSimplifier` and `TopologyPreservingSimplifier` to handle empty geometry components in the input
* Fixed `ConvexHull` to handle input of array of identical coordinates
* Fixed `GeometryTransformer.transformLinearRing()` to handle null inputs
* Fixed the `extractPoint(distance, offset)` methods in
  `LocationIndexedLine` and `LengthIndexedLine` to handle offsets from endpoints correctly.
* Fixed `GeometryCollectionIterator` to correctly handle atomic geometries
* Fixed `InteriorIntersectionFinder` to not short-circuit when finding all nodes
* Fixed `SubgraphDepthLocator` to work with Java 7+ by avoiding sorting with an inconsistent ordering
* Fixed `FontGlyphReader` to use correct Java font value of "SansSerif", and added new constant to match.
* Fixed `KdTree` to correctly implement distance tolerance-based coordinate matching
* Fixed `LineString.normalize()` to correctly handle CoordinateSequences
* Fixed `CommonBitsRemover` to correctly handle CoordinateSequences
* Fixed bug in `CoordinateArraySequence.clone()`


### API Changes

* Changed interface of `SegmentSetMutualIntersector` to support thread-safety by removing state
* Provided `InteriorIntersectionFinderAdder` to replace the poorly named `IntersectionFinderAdder`
* Changed some classes in `com.vividsolutions.jts.operation.buffer` to be package-private


### Testing Improvements

* Added `GeometryTestCase` base class to simplify unit tests


<!------- JTS-IO -------------------->

## JTS I/O

### Functionality Improvements

* Added `GeoJsonReader` and `GeoJsonWriter` classes


### API Changes

* In `OraWriter` the connection is now provided to the `write()` method, not the constructors


### Bug Fixes

* Fixed `OraReader` to handle reading `GeometryCollection` with `Point` as second element
* Many improvements to Oracle API code and performance
* Added `OraWriter.setOptimizePoint` and `OraWriter.setOptimizeRectangle` methods


<!------- TestBuilder -------------------->

## JTS TestBuilder

### Functionality Improvements

* Added ability to read multiple WKBHex geometries from a text file
* Added AutoZoom to input
* Added Oracle text output
* Added more function sets
* Removed dependency on Acme GIF encoder library
* Improved zooming logic
* Added mouse wheel zooming
* Improved UI for editing A/B geometries
* Added **Compute New** button to Geometry Functions panel
* Added SpatialIndex functions
* Added User Data labelling


### UI Changes

* Moved Options menu items to View menu
* Removed Tools menu items (they are available as functions)


### Bug Fixes

* Fixed to correctly handle changing Swing Look&Feel



<!-- ================================================================ -->

# Version 1.13

*Release Date:  December 13, 2012*

### Functionality Improvements

* Changed `GeometryFactory.createGeometry()` to make a deep copy of the argument Geometry,
using the CoordinateSequenceFactory of the factory
* Added ability to specify a dimension in `CoordinateArraySequence`
* Changed `Geometry.getEnvelopeInternal()` to return a copy of the cached envelope, to prevent modification
* Added `GeometryEditor.CoordinateSequenceOperation` to allow easy editing of constituent CoordinateSequences
* Added `GeometryFactory.createPolygon` convenience methods which do not require holes to be specified
* `Geometry` overlay methods now return empty results as atomic types of appropriate dimension
* Added `RectangleLineIntersector` to provide efficient rectangle-line intersection testing
* Added `getOrdinate` and `setOrdinate` to `Coordinate`
* `Quadtree` is `Serializable`
* `STRtree` is `Serializable`
* Added `max`, `average` and `wrap` functions to `MathUtil`
* Improved `WKTReader` parse error reporting to report input line of error
* Improved `WKBReader` to repair structurally-invalid input
* Made `TopologyPreservingSimplifier` thread-safe
* Added `AbstractSTRtree.isEmpty()` method
* Added `QuadTree.isEmpty()` method
* Added `KdTree.isEmpty()` method
* Added decimation and duplicate point removal to `ShapeWriter`.
* `ScaledNoder` now preserves Z values of input
* Added instance methods for all <t>Triangle` static methods
* Added `CGAlgorithmsDD` containing high-precision versions of some basic CG algorithms
* Added `IntersectionMatrix.isTrue()` method for testing IM pattern matches
* Added `getRawCoordinates` methods to `PackedCoordinateSequence` concrete classes
* Modified `Geometry.isSimple()` to explicity check for simplicity for all types,
and support `GeometryCollections`
* Improved `MCIndexSnapRounder` to add nodes only where they are necessary
* Added `CoordinateArrays.removeNull()` method
* Enhanced `GeometryEditor` to handle null geometries returned from operation
* Added `WKBHExFileReader`
* Added `Distance3D` operation


### Performance Improvements

* Simplified & improved performance of `RectangleIntersects` by using new `RectangleLineIntersector`
* In `RandomPointsInGridBuilder` eliminated redundant `ArrayList` usage
* In `PreparedPolygonIntersects` and `PreparedLineStringIntersects` added check
to avoid creating segment index if all test inputs are points
* In `AbstractSTRtree` switched to using indexed list access for better performance than using iterators
* In `AbstractSTRtree` freed inserted item array after index is built
* Improved performance of `Polygonizer` for cases with many potential holes
* Improved performance for some `DD` methods by making them `final`
* Added fast filter for `CGAlgorithmsDD.orientationIndex`, and switched to self-operations for DD determinant
* Changed `STRtree.createNode()` to use a static class for nodes
* Changed `QuadTree Node` to use scalar x and y variables rather than a `Coordinate` to reduce memory allocation
* Fixed `PreparedGeometry` concrete classes to be thread-safe.
* Fixed `SortedPackedIntervalRTree` so that it is thread-safe.


### Robustness Improvements

* Switched to using DD extended-precision arithmetic to compute orientation predicate
* `CGAlgorithms.distanceLineLine()` improved to be more robust and performant
* Fixed robustness issue causing `Empty Stack` failure
in `ConvexHull` for some nearly collinear inputs
* `CGAlgorithms.signedArea()` uses a more accurate algorithm


### Bug Fixes

* Fixed `Geometry.equalsExact()` to avoid NPE when comparing empty and non-empty `Point`s
* Fixed `CascadedPolygonUnion` to discard non-polygonal components created during unioning,
to avoid failures and provide more desirable behaviour
* Fixed `CentralEndpointIntersector` to initialize result correctly
* Fixed `DelaunayTriangulationBuilder.extractUniqueCoordinates(Geometry)`
to avoid mutating the vertex order of the input Geometry
* Fixed `ConformingDelaunayTriangulationBuilder` to allow
non-disjoint site and constraint vertex sets
* Fixed `RandomPointsInGridBuilder` point generation to use circle constraint correctly
* Fixed Linear Referencing API to handle MultiLineStrings consistently,
by always using the lowest possible index value, and by trimming zero-length components from results
* Fixed bug in `LocationIndexedLine` and `LengthIndexLine` which was
causing an assertion failure when the indexOfAfter() method was called with a constraint location
which is at the end of the line
* Fixed bug in `STRtree.query(Envelope, ItemVisitor)` causing an NPE when tree is empty
* Fixed issue with creating zero-length edges during buffer topology building under fixed precision, by:
adding filter to remove zero-length edges;
using a better estimate of scale factor for reducing to fixed precision after initial failure.
* Fixed `TopologyPreservingSimplifier` to return a valid result
for closed `LineString`s with large distance tolerances
* Fixed `TopologyPreservingSimplifier` to return an empty result for an empty input
* Fixed `DouglasPeuckerSimplifier` to return an empty result for an empty input
* Fixed `MinimumBoundingCircle` to correctly compute circle for obtuse triangles.
* Fixd `GeometryPrecisionReducer` to use input GeometryFactory when polygon topology is fixed
* Fixed `GeometryNoder` bug that was failing to snap to end vertices of lines
* Fixed `Geometry.getCentroid()` and `Geometry.getInteriorPoint()` to return `POINT EMPTY` for empty inputs
* Fixed `DelaunayTriangulationBuilder` to correctly extract unique points
* Fixed `KdTree` to correctly handle inserting duplicate points into an empty tree
* Fixed `LineSegment.projectionFactor()` to handle zero-length lines (by returning Double.POSITIVE_INFINITY)
* Fixed `LocationIndexedLine` to handle locations on zero-length lines
* Fixed `LengthIndexedLine` and `LocationIndexedLine` to handle `indexOfAfter()` correctly
* Fixed `WKBReader` to handle successive geometrys with different endianness
* Fixed `GeometricShapeFactory` to correctly handle setting the centre point
* Fixed `GeometryFactory.createMultiPoint(CoordinateSequence)` to handle sequences of dimension &gt; 3


### API Changes

* Changed visibility of `TaggedLineStringSimplifier` back to `public` due to user demand


### Testing

* Added Performance Testing framework (`PerformanceTestRunner`
and `PerformanceTestCase`)
* Added named predicate tests to all Relate test cases


### Contributors

* Peter Hopfgartner - improved area computation
* Michael Michaud - snap-rounding improvements



<!------- TestBuilder -------------------->

## JTS TestBuilder

### Functionality Improvements

* Added segment index visualization styling
* Improved **Geometry Inspector**
* Added stream digitizing for Polygon and LineString tools
* Added output of Test Case XML with WKB
* Added Extract Component tool
* Added Delete Vertices Or Components tool
* Added Geometry Edit Panel pop-up menu, with operations
* Added Halton sequence functions
* Added sorting functions
* Added function for selection of first N components
* Added CGAlgorithms functions
* Added ability to paste and load multiple WKBHex geometries


### Performance Improvements

* Using decimation substantially improves rendering time for large geometries.


### Bug Fixes

* Fixed bug in saving XML test files


<!-- ================================================================ -->

# Version 1.12

*Release Date:  June 30, 2011*

### Functionality Improvements

* Added new names for methods for computing `Geometry` equality:
* `equals(Object)` is a synonym for `equalsExact(Geometry)`
* `equalsNorm(Geometry)` automatically normalizes the operands
* `equalsTopo(Geometry)` computes topological equality,
and is a synonym for the original `equals(Geometry)`

* Added `Geometry.norm()` to provide non-mutating normalization
* Added `Geometry.hashCode()` to fulfill Java conventions
* Added `LineIntersector.getEndpoint()` method
* Added methods to `CoordinateSequences` to test for and create valid rings
* Added `minExtent` and `maxExtent` to `Envelope`
* Added ability to compute Single-Sided Buffers (invoked via `BufferOp` and `BufferParameters`)
* Added `GeometryPrecisionReducer`
* Added ExtendedWKB SRID support to `WKBWriter` (thanks to Justin Deoliviera)
* Improved `PolygonShape` to support floating-point coordinates
* Added `GeometryShapeFactory.setRotation(double radians)` method
* Added `GeometricShapeBuilder` API to support shape builder development
* Added `RandomPointsBuilder` to allow generating various random point sets
* Added `RandomPointsInGridBuilder` to allow generating various random point sets constrained to a grid
* Added `KochSnowflakeBuilder`
* Added `SierpinskiCarpetBuilder`
* Added `MathUtil` containing mathematics and numerical utility functions
* Added `Vector2D` class providing vector operations
* Added `DirectedEdgeStar.getNextCWEdge()` method to `planargraph` API
* `AffineTransformation` enhanced to avoid numeric precision issues in case of reflection in X=Y (coordinate flipping)
* Added `LineSequencer.sequence()` static convenience method
* Added error indicators to `BufferDistanceValidator` and `BufferResultValidator`
* Added `MinimumClearance` class
* Added `nearestNeighbours` methods to `STRtree`


### Performance Improvements

* Improved memory performance of `ShapeWriter` conversions (by tuning coordinate and polygon conversion)
* Improved performance of `RectangleIntersects` by refining `SegmentIntersectionTester`


### Robustness Improvements

* Delaunay triangulation uses more robust formulation for the inCircle test
* Voronoi computation now uses more robust formulation for the circumcentre computation
* Force `RectangleIntersects` to always use segment-scanning to improve robustness


### API Changes

* Reduced visibility of internal classes in `com.vividsolutions.jts.geom.prep`
* Reduced visibility of internal classes in `com.vividsolutions.jts.simplify`
* Moved `Matrix` class into `jts.math` package
* Refactored internal offset curve generation classes
in `com.vividsolutions.jts.operation.buffer` package


### Bug Fixes

* Fixed `CoordinateArraySequence` and `PackedCoordinateSequence`
to correctly handle Z ordinate in `getCoordinate(int, Coordinate)`
* Fixed `LinearRing` to have `isClosed()` return `true` for empty rings
* Fixed `Geometry.union()` to use more robust union algorithm.
    This provides behaviour consistent with `union(Geometry)`.
* Fixed `Point.isValid()` to validate POINT EMPTY correctly
* Fixed `SnapIfNeededOverlayOp` to throw the originating exception,which contains meaningful coordinates
* Fixed `GeometrySnapper` to allow final vertices of LineStrings to snap correctly
* Fixed buffer (`OffsetCurveSetBuilder`) to handle "flat" rings correctly
* Fixed `IsValidOp` to handle reporting "Not Closed" errors on empty rings correctly
* Fixed `NestedRingTester` (used by `IsValidOp`)
to correctly handle the case where a hole touches all the vertices of another hole (which is invalid)
* Fixed `ConvexHull` to handle large geometries with fewer than 3 unique points
* Fixed `GeometryGraph` to ignore empty hole rings when building graph
* Fixed `LineMerger` to skip lines with only a single unique coordinate
* Fixed `ByteArrayInStream` to pad byte buffers with zeroes
* Corrected spelling of `SquarePointShapeFactory`
* Fixed tolerance check in `KdTree`
* Updated `MasterTester` to include more unit tests


<!------- TestBuilder -------------------->

## JTS TestBuilder

### Functionality Improvements

* Added `[Zoom To Result]` button
* Improved mark display, with floating point label
* Added more random geometry creation functions
* Added fractal geometry creation functions
* Improved threaded rendering handling to ensure only one frame drawn
* Added Magnify Topology capability
* Added Geometry Inspector dialog
* Better startup script, with auto-home directory detection
and JTS_LIB_DIR environment variable (thanks to strk)
* Added logging Info window behaviour
* Improved saving PNG to allow specifying file name


### Bug Fixes

* Fixed Stats panel to update when current test changes
* Fixed deleting single test case


<!-- ================================================================ -->

# Version 1.11

*Release Date:  March 1, 2010*

### Functionality Improvements

* Added `CoordinateArrays.isRing`
* Added `CGAlgorithms.signedArea(CoordinateSequence)`
* Added `CoordinateArrays.copyDeep(...)` method to copy sections of arrays
* Added `CoordinateList.add(Coordinate[], boolean, int, int)` method to add sections of arrays
* Added `LineSegment.toGeometry()`, `LineSegment.lineIntersection()()`
* Added `LineSegment.hashCode()`
* Added geometric similarity classes (`HausdorffSimilarityMeasure`, `AreaSimilarityMeasure`)
* Added `MinimumDiameter.getMinimumRectangle()`
* Added `MinimumBoundingCircle` class
* Added `Densifier` class
* Added triangulation API, including `QuadEdgeSubdivision`, `IncrementalDelaunayTriangulator`,
	`ConformingDelaunayTriangulator` and supporting classes
* Added `VoronoiDiagramBuilder` to perform Voronoi diagram generation
* Added `scaleInstance(scaleX, scaleY, x, y)` to `AffineTransformation`
* Added `AffineTransformationFactory` to allow generating transformations from various kinds of control inputs
* Added `BinTree.remove()` method
* Fixed `BinTree.query()` to allow null interval arguments
* Added `ShapeReader` API to convert Java2D Shapes into JTS Geometry
* Added `ShapeWriter` API to convert JTS geometry into Java2D Shapes
* Added `FontGlyphReader` API to render Java2D text font glyphs into geometry
* Added `SdeReader` to **jtsio** library
* Added `Debug` break methods
* Added `Memory` utility for reporting memory statistics
* Added `ObjectCounter` utility for counting objects
* Added `createSquircle` and `createSuperCircle` to `GeometricShapeFactory`


### Performance Improvements

* Improved performance of `Geometry.getArea()` and `Geometry.getLength()` when used with custom `CoordinateSequence`s


### API Changes

* Deprecated `WKBWriter.bytesToHex` in favour of `WKBWriter.toHex` to regularize and simplify method name


### Bug Fixes

* Fixed Point.isValid() to check for invalid coordinates (ie with Nan ordinates)
* Fixed `Geometry.distance()` and `DistanceOp` to return 0.0 for empty inputs
* Fixed `Buffer` to handle degenerate polygons with too few distinct points correctly
* Added illegal state check in `LineSegment.pointAlongOffset()`
* Fixed exception strategy in `BufferSubgraph` to handle certain robustness failures correctly
* Fixed robustness problem in `OffsetCurveBuilder` in computing mitred joins for nearly parallel segments
* Fixed minor bug in `BufferInputLineSimplifier` which prevented simplification of some situations
* Fixed bug in `BufferInputLineSimplifier` which caused over-simplification for large tolerances
* Fixed bug in `Angle.normalizePositive` to handle values > 2PI correctly
* Fixed `WKTWriter` to emit correct syntax for MULTIPOINTs
* Fixed `WKTReader` to accept correct syntax for MULTIPOINTs
* `CGAlgorithms.isCCW` now checks for too few points in the ring and throws an `IllegalArgumentException`
* Fixed bug in `AffineTransformation#eqals` (logic bug)
* Fixed bug in `CoordinateList#closeRing` (cloning closing Coordinate)


<!------- TestBuilder -------------------->

## JTS TestBuilder

### Functionality Improvements

* WKT input is cleaned automatically when loaded (illegal chars are removed)
* Added WKT-Formatted option to Test Case View dialog
* Many new geometry functions added
* Geometry functions are displayed in tree
* Geometry functions can be implemented as Java static class methods.
* Geometry function classes can be loaded dynamically from command-line
* Improved handling of very large geometry inputs and results
* Threaded rendering allows display of very large geometries without limiting usability
* Added Draw Rectangle tool
* Added Drag-n-drop loading of .SHP files
* Added Info tool to provide persistent display of geometry point/segment information
* Added display of memory usage



<!-- ================================================================ -->

# Version 1.10

*Release Date:  December 31, 2008*

### Functionality Improvements

* Added `Geometry.reverse()` method for all geometry types
* Added `setSrsName`, `setNamespace`, `setCustomRootElements` methods to `GMLWriter`
* Added `Envelope.getArea` method
* Added `copy`, `copyCoord` methods to `CoordinateSequences`
* Added `area` method to `Envelope`
* Added `extractPoint(pt, offset)` methods to `LengthIndexedLine` and `LocationIndexedLine`
* Added `CoordinatePrecisionReducerFilter`
* Added `UnaryUnionOp(Collection, GeometryFactory)` constructor to handle empty inputs more automatically
* Added `DiscreteHausdorffDistance` class
* Made `LineMerger` able to be called incrementally
* Added `GeometricShapeFactory.createArcPolygon` to create a polygonal arc
* Enhanced `Geometry.buffer()` to preserve SRID


### Performance Improvements

* Improved performance for `EdgeList` (by using a more efficient technique for detecting duplicate edges)
* Improved performance for `ByteArrayInStream` (by avoiding use of `java.io.ByteArrayInputStream`)
* Unrolled intersection computation in `HCoordinate` to avoid object allocation
* Improved performance for buffering via better offset curve generation and simplification.
* Improved performance for `IsValidOp` by switching to use `STRtree` for nested hole checking


### Bug Fixes

* Fixed `Geometry.getClassSortIndex()` to lazily initialize the sorted class list.  This fixes a threading bug.
* Fixed `RectangleContains` to return correct result for points on the boundary of the rectangle
* Fixed error in `com.vividsolutions.jts.simplify.LineSegmentIndex` which caused polygons
simplified using `TopologyPreservingSimplifier` to be invalid in certain situations
* Fixed error in `DouglasPeuckerSimplifier` which caused empty polygons to be returned when they contained a very small hole
* Fixed `PackedCoordinateSequence` to return `NaN` for null ordinate values
* Fixed `Geometry.centroid()` (`CentroidArea`) so that it handles degenerate (zero-area) polygons
* Fixed `Geometry.buffer()` (`OffsetCurveBuilder`) so that it handles JOIN_MITRE cases with nearly collinear lines correctly
* Fixed `GeometryFactory.toGeometry(Envelope)` to return a CW polygon
* Fixed `UnaryUnionOp` to correctly handle heterogeneous inputs with P/L/A components
* Fixed `UnaryUnionOp` to accept `LINEARRING`s
* Fixed `CentroidArea` to handle zero-area polygons correctly
* Fixed `WKBWriter` to always output 3D when requested, and to handle 2D `PackedCoordinateSequences` correctly in this case
* Fixed `NodedSegmentString` to handle zero-length line segments correctly (via `safeOctant`)
* Cleaned up code to remove unneeded `CGAlgorithms` objects
* Fixed `GeometricShapeFactory.createArc` to ensure arc has requested number of vertices


### API Changes

* Moved GML I/O classes into core JTS codebase
* Changed `GMLWriter` to not write the `srsName` attribute by default
* In `DistanceOp` switched to using `nearestPoints` method names
* Exposed `STRtree.getRoot()` method



<!------- TestBuilder -------------------->

## JTS TestBuilder

### UI Improvements

* Added ability to read GML from input panel
* Added GML output to View dialog
* Added file drag'n'drop to Geometry Input text areas
* Add display of computation time
* Added Stats panel
* Added Scalar functions panel, with extensible function list
* Added **Save as PNG...**
* Added stream digitizing to Polygon and Line Draw tools


<!------- TestRunner -------------------->

## JTS TestRunner

### Functionality Improvements

* Added `-testCaseIndex` command-line option


<!-- ================================================================ -->

# Version 1.9

*Release Date: January 2, 2008*

### Functionality Improvements

* Added `Polygonal`, `Lineal`, `Puntal` tag interfaces to better categorize geometry classes
* Added `Geometry.union()` method, `UnaryUnionOp` class for efficient unioning of geometrys
* Added `Triangle.area3D` method
* Added `LineSegment.pointAlongOffset` method
* Added `LineSegment.orientationIndex(Coordinate)` method
* Added `PreparedGeometry` classes and methods to optimize some geometry functions in batch situations
* Added `Envelope.covers` methods, for preciseness
* Added `OctagonalEnvelope` class
* Improved `CGAlgorithms.isPointInRing` method to handle case where point lies on the ring
* Added `CGAlgorithms.locatePointInRing` method
* Added `PointInAreaLocator` interface, enhanced `SimplePointInAreaLocator` to extend this
* Added `RayCrossingCounter`,  `IndexedPointInAreaLocator` classes for more efficient Point-In-Polygon testing
* Added `GeometryCombiner` class
* Enhanced `BufferOp` with join styles
* Enhanced `WKTReader` to accept any case for `NaN` number symbols
* Added `WKTFileReader` class
* Improved performance of `CoordinateList` constructors
* Added `CascadedPolygonUnion` class
* Added `LinearLocation.isOnSameSegment` method
* Added `LinearLocation.getSegment` method
* Added `LocationIndexedLine.indexOfAfter` method
* Added interpolation of Z value to linear referencing methods
* Added methods to rotate around a given point to `AffineTransformation`
* Allowed `GeometricShapeFactory` to be subclassed to add new shapes
* Added `SineStarFactory` (primarily to support testing)
* Added `SortedPackedIntervalRTree` class
* Added `SegmentSetMutualIntersector` interface and implementations
* Added `Node.remove(DirectedEdge)` method in `planargraph` package


### Performance Improvements

* Improved performance for `SimplePointInAreaLocator` (by checking ring envelopes as a filter)


### Bug Fixes

* `Geometry.buffer` operation fixed to always return polygonal geometries
* Fixed bug in `Geometry.buffer(distance, quadrantSegs)`
causing failure for some cases with `quadrantSegs = 1`
* Fixed bug in `GeometryFactory.toGeometry(Envelope)`
which was returning invalid Polygons for "linear" envelopes
* Fixed bug in `MonotoneChainBuilder` which caused failures in situations with segments of zero length
* Fixed `PointLocator` to handle locating in Point geometries
* Fixed `GeometricShapeFactory` to always use provided PrecisionModel
* Fixed `LinearLocation.clone` method
* Fixed `LinearLocation.isValid` method
* Fixed `Polygonizer` to accept single-point linestrings (which are ignored)


### API Changes

* Deprecated `RobustCGAlgorithms`
* Deprecated `BufferOp` cap style constants (these are now provided in `BufferParameters`)
* Removed `SIRPointInRing`


<!------- TestRunner -------------------->

## JTS TestRunner

### New Features

* Added ability to specify GeometryOperation in XML file
* Added `BufferValidatedGeometryOperation`
* Added ability to specify custom result matching via `resultMatcher` parameter
in XML files
* Added `BufferResultMatcher`



<!------- TestBuilder -------------------->

## JTS TestBuilder

### UI Improvements

* Improved rendering of geometries
* Improved symbology for orientation
* Simplified Geometry creation
* Improved vertex move/add
* Added tooltip for coordinate location
* Added more geometry functions
* Added Copy Result to Test button


### Code Improvements

* Restructured code for more flexibility, better Swing functionality
* Made it easier to add geometry functions



<!-- ================================================================ -->

# Version 1.8

*Release Date: December 19, 2006*

### Functionality Improvements

* Improved robustness for overlay operations, via using geometry snapping
* Added `Angle` class
* Added methods to `Triangle` class
* Added `LineSegment.midPoint` method
* Added ability to specify output of Z values to `WKTWriter`
* Added `setFormatted`, `setTab`, `setMaxCoordinatesPerLine` methods to `WKTWriter`
* Added `BoundaryNodeRule` classes, and ability to specify a Boundary Node Rule
in `RelateOp`, `IsSimpleOp`, `BoundaryOp`
* Added ability to get the failure location to `IsSimpleOp`
* Added `BoundaryOp` with improved algorithm for lineal geometries.  Changed lineal Geometry classes to use it.
* Enhanced `Geometry` overlay methods to accept empty `GeometryCollections.`
* Enhanced Error Handling for `WKTReader`
* Added `CoordinateSequenceFilter`
* Added `AffineTransformation` and `AffineTransformationBuilder`


### API Changes

* Changed API for `IsSimpleOp` (required to support returning failure location)


### Bug Fixes

* Fixed bug in `GeometryFactory.buildGeometry`
causing failure if input contained all GeometryCollections of the same subclass
* Changed AssertFailure to TopologyException in `PolygonBuilder`
* Improved correctness of `RobustLineIntersector` in certain cases where segment endpoints intersect
* Eliminated duplicate method execution in `TestRunner`



## JTS TestBuilder

### New Features

* Zoom tool can now draw zoom box as well as click
* Cut, Copy and Paste buttons for Input WKT
* added ability to specify custom Geometry Operations



## JTS TestRunner

### New Features

* added ability to specify custom Geometry Operations


### Bug Fixes

* Eliminated duplicate method execution




<!----------------------------------------------------------------------->

# Version 1.7.2

*Release Date: June 22, 2006*

### Functionality Improvements

* Added support for EWKB (SRIDs only) to `WKBReader`


### Bug Fixes

* Fixed bug in `CoordinateArrays.ptNotInList`.
Changed `polygonize.EdgeRing` to used corrected code.
* Fixed bug causing duplicate points in `ScaledNoder`
* Fixed bug causing Null Pointer for empty geometries in `OraWriter`
* Changed AssertFailure to TopologyException in `EdgeNode`


<!----------------------------------------------------------------------->

# Version 1.7.1

*Release Date: March 20, 2006*

### Functionality Improvements

* Added Hex string conversion to `WKBReader` and `WKBWriter`


### Bug Fixes

* Fixed null point cloning bug in `TopologyValidationError` (*thanks to Markus Gebhard*)
* Fixed bug in `PointLocator` fix for LinearRings
* Fixed bug in `Geometry.isValid` and `IsValidOp` causing some valid polygons
to be reported as having a Disconnected Interior (specifically, polygons containing
holes touching at a single point, where the point is the highest point in the hole rings, and where the holes
have a specific orientation)
* Fixed bug in `Polygon.isRectangle`, which reported some valid rectangles as false


### Performance Improvements

* Improved performance for `Geometry#withinDistance` (via short-circuiting)


### Contributors

* Dave Blasby
* Koen van Dijken



<!----------------------------------------------------------------------->

# Version 1.7

*Release Date: December 7, 2005*

### Functionality Improvements

* Added `JTSVersion` class to provide access to the API version information
* Added `covers` and `coveredBy` predicates to `Geometry`
* Added `Geometry#buffer(distance, quadSegs, endCapStyle)` method to expose buffer end cap styles
* Added `LineString#reverse` and `MultiLineString#reverse` methods
* Added `centre`, `intersection`, `translate`,
`expandBy(distance)`, `expandBy(dx, dy)`
methods to `Envelope`
* Added `CollectionUtil` class for performing operations over collections
* Added `CoordinateArrays` comparators
* Added `CoordinateSequence#getDimension`
* Added convenience methods `toPoint` and `toLineString` to `WKTWriter`
* Added WKB API (`WKBReader` and `WKBWriter` classes in `com.vividsolutions.jts.io`
* `WKTReader` has better handling of numeric parsing, including support for scientific notation
* Added `IsValidOp#setSelfTouchingRingFormingHoleValid` to allow validating SDE-style polygons
* Added check for non-closed rings in `IsValidOp`
* Added Linear Referencing API (`com.vividsolutions.jts.linearref`
* Added `LineSequencer` class to `com.vividsolutions.jts.operation.linemerge`
* Added `Subgraph` class to `com.vividsolutions.jts.planargraph`
* Added `isRemoved` method to `GraphComponent`
* Added `ConnectedSubgraphFinder` class to `com.vividsolutions.jts.planargraph.algorithm`
* Added `setVisited`, `getComponentWithVisitedState`,
`setVisited`, `getComponentWithVisitedState`
methods to `com.vividsolutions.jts.planargraph.GraphComponent`
* Added classes to perform Snap Rounding (in `com.vividsolutions.jts.noding.snapround`>
* Improved buffering speed and robustness by using Snap Rounding
* Added buffer optimization for results with large numbers of polygons and/or holes
* Added `STRtree#query(Envelope, ItemVisitor)` method
* Added `Debug#toLine` methods
* Added `ConvexHull(Coordinate[])` constructor


### Bug Fixes

* Fixed decimal formatting in `WKTWriter` to force a leading 0 in decimal numbers
* Fixed bug in `CoordinateArraySequence#setOrdinate`
* Fixed bug when checking validity of polygon with hole (`IsValidOp#checkHolesInShell`)
* Improved correctness of computated intersections in `RobustLineIntersector`
* Fixed bugs in `CoordinateList.clone` (thanks to Matthias Bobzien)
* Fixed bug in `Envelope.equals` (thanks to John Cartwright)
* Fixed `PointLocator` for LinearRings


### Performance Improvements

* Improved performance for overlay operations (point inclusion, identical edge detection)
* Improved Convex Hull performance


### API Changes

* Added `SpatiaIndex#query(Envelope, ItemVisitor)` method signature
* Added `CoordinateSequence#getDimension()` method signature
* Marked `GeometryEditor.CoordinateOperation#edit(Geometry, GeometryFactory)` method
as `final`, to prevent incorrect use


### Semantics Changes

* CoordinateArraySequence#setOrdinate now checks that the ordinate index is in range




<!----------------------------------------------------------------------->

# Version 1.6

*Release Date: February 3, 2005*

### API Changes

* Changed to using `CoordinateArraySequence` instead of `DefaultCoordinateSequence`
(to provide a more descriptive name).


### Semantics Changes

* PrecisionModel#makePrecise changed to use Symmetric Arithmetic Rounding rather than Banker's Rounding


### Functionality Improvements

* Added ability to enable `Debug` methods by setting a system property
* Added `getNumGeometries` and `getGeometryN` methods to Geometry class, to make API more uniform
* Improved API for `CoordinateSequence` allows more options for improving memory usage and handling custom coordinate storage methods
* Added `PackedCoordinateSequence` to provide reduced memory footprint for geometry objects if desired
* Added optimized spatial predicates for rectangles
* Added Debug#isDebugging method


### Bug Fixes

* Fixed bug in `Geometry#within()` short circuiting
* Fixed bug causing `Geometry#isValid` to throw IllegalArgumentException for certain kinds of holes with invalid rings
* Fixed bug causing redundant linestrings to be returned in the result of overlaying polygons containing holes touching their shell.
* `Polygon#getBoundary` now returns a `LinearRing` if the polygon does not have holes


### Architecture Changes

* Removed a proliferation of references to the default `CoordinateSequenceFactory`


### Contributors

* Andrea Aime


<!----------------------------------------------------------------------->

# Version 1.5
*Release Date: September 22, 2004*

This version is upwards compatible with Version 1.4

### API Changes

* None


### Semantics Changes

* None


### Functionality Improvements

* `CGAlgorithms#isCCW` now handles coordinate lists with repeated points.  Also throws an IllegalArgumentException if the input ring does not have 3 distinct points
* `isValid` now checks for invalid coordinates (e.g. ones with Nan or infinite numbers)
* added copyDeep() method to `CoordinateArrays`
* added geometry simplification operations `DouglasPeuckerSimplifier` and `TopologyPreservingSimplifier`
* added methods to `Quadtree` and `STRtree` to remove items and query using the Visitor pattern


### Performance Improvements

* Added short-circuit tests in geometry named predicates based on envelope tests

### Bug Fixes

* Fixed bugs in `Geometry` serialization
* Fixed bug in `ValidOp` which reported some MultiPolygons with shells nested inside a hole as invalid
* Fixed bug in buffer which caused buffers of some polygons with small & large holes to not contain any holes
* Fixed bug in `Polygonizer` which caused exception if no lines were supplied

### Architecture Changes

* Basic CG algorithm methods made static in the `CGAlgorithms` class
* Various utility methods made public in `CoordinateArrays` class

### Documentation

* More examples provided in `com.vividsolutions.jtsexamples package`


<!----------------------------------------------------------------------->

# Version 1.4

*Release Date: November 4, 2003*
### Semantics Changes

* none

### Functionality Improvements

* Added "LINEARRING" tag to WKT syntax
* Added GeometryEditor class to allow easy copy/modify of Geometrys
* Added GeometricShapeFactory class to easily create standard geometric shapes
* Geometries can now carry arbitrary user-defined data objects (via Geometry#get/setUserData(Object) method)
* Added CoordinateSequence and CoordinateSequenceFactory interfaces, and default implementations (BasicCoordinateSequence, BasicCoordinateSequenceFactory)
* Added Geometry#getFactory
* Added new PrecisionModel type of FLOATING_SINGLE, for rounding to single precision floating point
* Added DistanceOp#getClosestPoints method, which returns the closest points between two Geometries
* Added com.vividsolutions.jts.noding package containing classes to perform fast indexed noding of linestrings
* Added com.vividsolutions.jts.operation.polygonize package containing classes to perform polygonization
* Added com.vividsolutions.jts.operation.linemerge package containing classes to perform line merging
* Added SimpleGeometryPrecisionReducer to allow reducing precision of coordinates of a Geometry
* Added LineSegment#closestPoints method to compute the closest points between two line segments
* Added MinimumDiameter class to compute minimum diameters of Geometries
* Added geom.Triangle class to contain algorithms for Triangles
* BufferOp now allows end cap styles to be specified.  Three types are supported: round, butt and square.

### Performance Improvements

* EdgeList now provides a findEqualEdge method which is substantially faster than findEdgeIndex, for large lists
* Buffering is now faster and much more robust
* Overlap operations are now more robust

### Bug Fixes

* Envelope#init(Envelope) now handles null Envelopes correctly
* CoordinateList#add() now correctly ignores the z-value of Coordinates in determining equality
* Geometry#isValid now correctly handles checking validity of LinearRings
* Fixed infinite loop bug causing Out Of Memory errors during polygon intersection
* Geometry#clone now correctly clones the Geometry's Envelope
* LineIntersector#computeEdgeDistance now correctly computes a non-zero edge distance in certain situations when a fixed precision model was being used and the line segment was a single unit in length
* Fixed incorrect calculation of depths in DirectedEdgeStar#computeDepths
* Fixed BufferSubgraph#addReachable to use explicit stack to avoid stack overflow problems
* Fixed various bugs causing some kinds of buffers to be computed incorrectly

### API Changes

* WKTReader/Writer: changed protected members to private
* PrecisionModel type is now an object rather than an int
* ConvexHull API changed to remove requirement to pass in CGAlgorithms object

### Code Architecture Changes

* geom.util package added for utility classes which parse and modify geometries

### Documentation

* More examples provided in com.vividsolutions.jtsexamples package
* Added JTS Developers Guide


<!----------------------------------------------------------------------->

# Version 1.3
*Release Date: April 4, 2003*
### Semantics Changes

* all Geometry methods are now reentrant (thread-safe)
* Fixed-precision coordinates are now stored in a rounded but non-scaled form.  This makes them compatible with non-precise (Floating) coordinates, and simplifies working with precise coordinates directly.  Mixed precision models are now supported in Geometry methods; method results are in the more precise of the input precision models.
* Offsets are no longer supported in the Fixed precision model.  This is necessary to allow storing fixed precision coordinates in a non-scaled form.  This does not reduce the total precision available, since coordinates are stored in a floating-point format.
* SRID and Precision Model are no longer checked for equality during Geometry operations.  This removes a limitation which provided little semantic benefit.


### Functionality Improvements

* added Geometry.isWithinDistance(Geometry g, double distance) method, to provide optimized proximity queries
* added Geometry.buffer(double distance, int quadrantSegments) method, allowing control over accuracy of buffer approximation
* added Geometry.getCentroid() method
* added Geometry.getInteriorPoint() method, which uses heuristic methods to return a point in the interior of a Geometry
* GeometryFactory.toGeometryArray now returns null if the argument is null

### Performance Improvements

* Removed unnecessary string construction in EdgeEndStar.propagateSideLabels()
* Eliminated unnecessary computation of self-intersections in rings during relate and spatial functions.  This provides a large increase in speed when working with large rings and polygons.  (Note that IsValid still checks for these self-intersections, which are illegal in LinearRings)
* Add short-circuit code to RobustLineIntersector to detect non-intersections more efficiently

### Bug Fixes

* Fixed ClassCastException occurring in GeometryCollection.getLength()
* Fixed bug in Edge Intersection insertion (replaced Coordinate#equals with equals2D to ensure that intersection creation is not sensitive to Z-value).
* Fixed handling LineStrings with too few points in GeometryGraph.addLineString
* Fixed: was not checking that MultiPolygons don't contain components with too few points.
* Fixed Envelope.distance() to return correct distance for all envelopes.
* Fixed a few Geometry methods to make them re-entrant.
* Fixed CoordinateList.closeRing() to ensure endpoints are not duplicated
* Fixed CGAlgorithms.signedArea() to use a simpler algorithm which is more robust and faster.
* Fixed bug preventing validating Rings containing an initial repeated point.

### API Changes

* Added default constructor to WKTReader.  It uses the default GeometryFactory
* Add two static intersects() methods to Envelope, to allow computing intersections with envelopes defined by points only.
* Dropped BinaryPower; its functionality is provided by DoubleBits in a more robust fashion.
* Removed a couple of redundant private static methods from Geometry; they have been replaced by methods in CoordinateArrays
* The Geometry class is now marked as Serializable


<!----------------------------------------------------------------------->

# Version 1.2

*Release Date: 7 October 2002*
### Semantics Changes

* JTS now allows Geometrys to have repeated points.  All operations will continue to perform as before.  This removes a significant incompatibility with the OGC spatial data model.
* TopologyExceptions may now be thrown by spatial overlay methods.  This helps to distinguish between code bugs and known robustness problems.  It also provides a machine-readable coordinate for the error location.

### Functionality Improvements

* RobustLineIntersector now uses "normalized" coordinates to maximize the accuracy of intersection computation.
* Upgraded Quadtree with more robust implementation
* Replaced IntervalTree with a more robust implementation of BinTree
* Added STRTree 2-D spatial index, which exhibits better performance than QuadTrees in many situations.
* Added EnhancePrecisionOp, which uses precisioning enhancing techniques to reduce the number of failure cases due to robustness problems.

### Bug Fixes

* fixed ConvexHull to use TreeSet instead of HashSet for coordinates
* Fixed isValid for GeometryCollections containing Polygons, which were sometimes erroneously returning a validity failure for correct Geometrys.
* Fixed bug in LineSegment.distancePointLine() which would return the incorrect distance for a LineSegment with two identical points
* Improved error handling in CGAlgorithms.isCCW()
* IsValid now checks for too few points in a geometry component (e.g. due to repeated points in a ring)

### API Changes

* added Stopwatch class
* added Geometry.getArea() and Geometry.getLength() methods
* added CGAlgorithms.signedArea() method
* added methods to LineSegment - closestPoint(), getLength()
* added CoordinateArrrays and CoordinateLists utility classes
* Added TopologyValidationError.getErrorType() method
* Added Envelope#intersects; deprecated Envelope#overlaps.
* Added Geometry#geometryChanged() method to allow signaling when Geometry coordinates have been mutated by a client class
* Added STRTree class implementing a Sort-Tile-Recursive spatial index (a variant of a packed R-tree)
* Deleted IntervalTree 1-D spatial index (replaced by BinTree)
* Add BinTree 1-D spatial index


<!----------------------------------------------------------------------->

# Version 1.1.1

*Release Date: 9 April 2002*
### Bug Fixes

* fixed decimal-point symbol localization bug in WKTWriter
* fixed bug in Envelope.int(Envelope env)
* fixed filename case of SFSMultiLineString.java and IntervalTree.java

### API Changes

* deleted TopologyException class
* renamed CGAlgorithms.isPointInPolygon to isPointInRing (a more accurate description of what the method computes)

### API Additions

* added Geometry.getCoordinate() method
* added Geometry.distance() method
* added GeometryComponentFilter interface and Geometry.apply(GeometryComponentFilter) method


<!----------------------------------------------------------------------->

# Version 1.1

*Release Date: 28 March 2002*
### New Features

* added Geometry.isSimple() and Geometry.isValid() methods
* improved design of topological data structures
* added Geometry.setSRID() method
* improved functionality of the Envelope class
* added ability to write to an arbitrary java.io.Writer object to WKTWriter
* added Validate and Mark Location functionality to TestBuilder


<!----------------------------------------------------------------------->

# Version 1.0
*Release Date: 1 February 2002*

* Removed some non-compatibilities with Java 1.1
* Fixed bug in constructing buffer outline around inside of angles
* In TestBuilder vertices are now displayed with fixed size in view units
* Improved code for WKTWriter.writeFormatted()
* Fixed bug in constructor for LinearRing
* Improved implementation of sweepline intersection algorithm to avoid use of dynamic set.
* Fixed bug in ConvexHull.cleanRing()
* Refactored RobustLineIntersector and NonRobustLineIntersector


<!----------------------------------------------------------------------->

# Version 0.0
*Release Date: 30 May 2001*

**Baseline version**
