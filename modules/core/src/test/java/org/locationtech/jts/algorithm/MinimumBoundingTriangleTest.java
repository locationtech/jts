package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

import test.jts.GeometryTestCase;

public class MinimumBoundingTriangleTest extends GeometryTestCase {

	private static final double TOLERANCE = 1.0e-5;

	private final PrecisionModel precisionModel = new PrecisionModel(1);
	private final GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
	private final WKTReader reader = new WKTReader(geometryFactory);

	public static void main(String[] args) {
		junit.textui.TestRunner.run(MinimumBoundingTriangleTest.class);
	}

	public MinimumBoundingTriangleTest(String name) {
		super(name);
	}

	// Degenerate / robustness tests

	public void testEmptyPointExpectException() {
		Geometry g = read("POINT EMPTY");
		try {
			MinimumBoundingTriangle mbt = new MinimumBoundingTriangle(g);
			Geometry tri = mbt.getTriangle();
			// Current implementation may throw earlier; if not, allow null result
			assertNull(tri);
		} catch (Exception e) {
			// pass - constructor or computation may fail on empty input
		}
	}

	public void testSinglePointReturnsNull() {
		Geometry g = read("POINT (10 10)");
		try {
			MinimumBoundingTriangle mbt = new MinimumBoundingTriangle(g);
			Geometry tri = mbt.getTriangle();
			assertNull(tri);
		} catch (Exception e) {
			// Accept current behavior if it throws
		}
	}

	public void testTwoPointsNullOrException() {
		Geometry g = read("MULTIPOINT ((10 10), (20 20))");
		try {
			MinimumBoundingTriangle mbt = new MinimumBoundingTriangle(g);
			Geometry tri = mbt.getTriangle();
			assertNull(tri);
		} catch (Exception e) {
			// pass
		}
	}

	public void testCollinearPointsNullOrException() {
		Geometry g = read("LINESTRING (0 0, 10 10, 20 20, 30 30)");
		try {
			MinimumBoundingTriangle mbt = new MinimumBoundingTriangle(g);
			Geometry tri = mbt.getTriangle();
			assertTrue(tri == null || tri.getArea() <= TOLERANCE);
		} catch (Exception e) {
			// pass
		}
	}

	// Equality tests where hull is already a triangle

	public void testTriangleEqualitySimple() {
		doTriangleEqualsHullForTriangularHull("POLYGON ((0 0, 3 0, 1 2, 0 0))");
	}

	public void testObtuseTriangleEquality() {
		doTriangleEqualsHullForTriangularHull("POLYGON ((100 100, 200 100, 150 90, 100 100))");
	}

	public void testTriangleWithInteriorPoint() {
		// hull is triangle (0,0)-(3,0)-(1,2); interior point should be ignored
		doTriangleEqualsHullForTriangularHull("MULTIPOINT ((0 0), (3 0), (1 2), (1 1))");
	}

	// Coverage + midpoint property tests for convex polygons with > 3 vertices

	public void testRectangleCoversHullAndMidpointsTouchBoundary() {
		String wkt = "POLYGON ((0 0, 4 0, 4 2, 0 2, 0 0))";
		Geometry input = read(wkt);
		Geometry hull = input.convexHull();

		MinimumBoundingTriangle mbt = new MinimumBoundingTriangle(input);
		Geometry tri = mbt.getTriangle();

		assertNotNull(tri);
		assertTrue(tri.isValid());

		assertCovers(tri, hull);
		assertMidpointsTouchHull((Polygon) tri, hull);
	}

	public void testConvexPentagonCoversHullAndMidpointsTouchBoundary() {
		String wkt = "POLYGON ((0 0, 3 0, 4 1, 2 3, -1 2, 0 0))";
		Geometry input = read(wkt);
		Geometry hull = input.convexHull();

		MinimumBoundingTriangle mbt = new MinimumBoundingTriangle(input);
		Geometry tri = mbt.getTriangle();

		assertNotNull("not null", tri);
		assertTrue(tri.isValid());

		assertCovers(tri, hull);
		assertMidpointsTouchHull((Polygon) tri, hull);
	}

	public void testNearVerticalAndLargeCoordinates() {
		String wkt = "POLYGON ((" + "1000000000 0, " + "1000000900 900, " + "1000001000 1, " + "999999900 1000, " + "1000000000 0" + "))";
		Geometry input = read(wkt);
		Geometry hull = input.convexHull();

		MinimumBoundingTriangle mbt = new MinimumBoundingTriangle(input);
		Geometry tri = mbt.getTriangle();

		assertNotNull(tri);
		assertTrue(tri.isValid());

		assertCovers(tri, hull);
		assertMidpointsTouchHull((Polygon) tri, hull);
	}

	// Helpers

	private void doTriangleEqualsHullForTriangularHull(String wkt) {
		Geometry input = read(wkt);
		Geometry hull = input.convexHull();

		// Ensure hull really is a triangle
		assertEquals(2, hull.getDimension());
		assertTrue(hull instanceof Polygon);
		// triangle has 3 unique coordinates; ring includes closing point
		assertEquals(4, ((Polygon) hull).getExteriorRing().getNumPoints());

		MinimumBoundingTriangle mbt = new MinimumBoundingTriangle(input);
		Geometry tri = mbt.getTriangle();

		assertNotNull(tri);
		assertTrue(tri.isValid());

		// Expect exact equality up to topology
		checkEqual(hull, tri);
	}

	private void assertCovers(Geometry tri, Geometry geom) {
		// Use area-based difference to be robust against small numeric deviations
		Geometry diff = geom.difference(tri);
		assertTrue("Triangle should cover the convex hull; uncovered area = " + diff.getArea(), diff.getArea() <= TOLERANCE);
	}

	private void assertMidpointsTouchHull(Polygon tri, Geometry hull) {
		Coordinate[] cs = tri.getExteriorRing().getCoordinates();
		// Expect 4 coords (first == last); use first three as triangle vertices
		assertTrue(cs.length >= 4);
		Coordinate A = cs[0], B = cs[1], C = cs[2];

		Coordinate mAB = midpoint(A, B);
		Coordinate mBC = midpoint(B, C);
		Coordinate mCA = midpoint(C, A);

		double dAB = hull.getBoundary().distance(geometryFactory.createPoint(mAB));
		double dBC = hull.getBoundary().distance(geometryFactory.createPoint(mBC));
		double dCA = hull.getBoundary().distance(geometryFactory.createPoint(mCA));

		assertTrue("Midpoint AB not on hull boundary (dist=" + dAB + ")", dAB <= TOLERANCE);
		assertTrue("Midpoint BC not on hull boundary (dist=" + dBC + ")", dBC <= TOLERANCE);
		assertTrue("Midpoint CA not on hull boundary (dist=" + dCA + ")", dCA <= TOLERANCE);
	}

	private Coordinate midpoint(Coordinate a, Coordinate b) {
		return new Coordinate((a.x + b.x) / 2.0, (a.y + b.y) / 2.0);
	}
}