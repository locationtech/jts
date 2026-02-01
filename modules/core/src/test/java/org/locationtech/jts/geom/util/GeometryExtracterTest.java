package org.locationtech.jts.geom.util;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;

public class GeometryExtracterTest extends TestCase {

	static WKTReader reader = new WKTReader();
	
	public GeometryExtracterTest(String name) {
		super(name);
	}
	
	public void testExtract() throws ParseException {
		Geometry gc = reader.read("GEOMETRYCOLLECTION ( POINT (1 1), LINESTRING (0 0, 10 10), LINESTRING (10 10, 20 20), LINEARRING (10 10, 20 20, 15 15, 10 10), POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0)), GEOMETRYCOLLECTION ( POINT (1 1) ) )");
		
		// verify that LinearRings are included when extracting LineStrings
		List lineStringsAndLinearRings = GeometryExtracter.extract(gc, Geometry.TYPENAME_LINESTRING);
		assertEquals(3, lineStringsAndLinearRings.size());
		
		// verify that only LinearRings are extracted
		List linearRings = GeometryExtracter.extract(gc, Geometry.TYPENAME_LINEARRING);
		assertEquals(1, linearRings.size());
		
		// verify that nested geometries are extracted
		List points = GeometryExtracter.extract(gc, Geometry.TYPENAME_POINT);
		assertEquals(2, points.size());

		List<LineString> byClassLines = GeometryExtracter.extractByClass(gc, LineString.class);
		assertEquals(3, byClassLines.size());

		// verify LinearRing.class only extracts rings
		List<LinearRing> byClassRings = GeometryExtracter.extractByClass(gc, LinearRing.class);
		assertEquals(1, byClassRings.size());

		// verify Point.class extracts nested points
		List<Point> byClassPoints = GeometryExtracter.extractByClass(gc, Point.class);
		assertEquals(2, byClassPoints.size());

		// verify "write into user-provided collection" works with a supertype
		List<Geometry> out = new ArrayList<Geometry>();
		List<Point> returned = GeometryExtracter.extractByClass(gc, Point.class, out);
		assertEquals(2, returned.size());
		assertEquals(2, out.size());
	}

}
