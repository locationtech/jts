/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.operation.distance3d;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


public class Distance3DOpTest extends TestCase 
{
	static GeometryFactory geomFact = new GeometryFactory();
	static WKTReader rdr = new WKTReader();
	
	public static void main(String args[]) {
		TestRunner.run(Distance3DOpTest.class);
	}

	public Distance3DOpTest(String name) {
		super(name);
	}

	/*
	public void testTest()
	{
		checkDistance(	"LINESTRING (250 250 0, 260 260 0)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				70.71067811865476);	
		
		testLinePolygonFlat();
	}
	*/
	
	public void testEmpty()
	{
		checkDistance(	"POINT EMPTY", "POINT EMPTY",	0);
		checkDistance(	"LINESTRING EMPTY", "POINT (0 0 0)",	0);
		checkDistance(	"MULTILINESTRING EMPTY", "POLYGON EMPTY",	0);
		checkDistance(	"MULTIPOLYGON EMPTY", "POINT (0 0 0)",	0);
	}
	
	public void testPartiallyEmpty()
	{
		checkDistance(	"GEOMETRYCOLLECTION( MULTIPOINT (0 0 0), POLYGON EMPTY)", "POINT (0 1 0)",	1);
		checkDistance(	"GEOMETRYCOLLECTION( MULTIPOINT (11 11 0), POLYGON EMPTY)", 
				"GEOMETRYCOLLECTION( MULTIPOINT EMPTY, LINESTRING (10 10 0, 10 20 0 ))",	
				1);
	}
	
	public void testPointPointFlat() {
		checkDistance(	"POINT (10 10 0 )",
				"POINT (20 20 0 )",
				14.1421356);
		checkDistance(	"POINT (10 10 0 )",
				"POINT (20 20 0 )",
				14.1421356);
	}
	
	public void testPointPoint() {
		checkDistance(	"POINT (0 0 0 )",
						"POINT (0 0 1 )",
				1);
		checkDistance(	"POINT (10 10 1 )",
				"POINT (11 11 2 )",
				1.7320508075688772);
		checkDistance(	"POINT (10 10 0 )",
				"POINT (10 20 10 )",
				14.142135623730951);
	}
	
	public void testPointSegFlat() {
		checkDistance(	"LINESTRING (10 10 0, 10 20 0 )",
				"POINT (20 15 0 )",
				10);
	}
	
	public void testPointSeg() {
		checkDistance(	"LINESTRING (0 0 0, 10 10 10 )",
				"POINT (5 5 5 )",
				0);
		checkDistance(	"LINESTRING (10 10 10, 20 20 20 )",
				"POINT (11 11 10 )",
				0.816496580927726);
	}
	
	public void testPointSegRobust() {
		checkDistance(	"LINESTRING (0 0 0, 10000000 10000000 1 )",
				"POINT (9999999 9999999 .9999999 )",
				0 );
		checkDistance(	"LINESTRING (0 0 0, 10000000 10000000 1 )",
				"POINT (5000000 5000000 .5 )",
				0 );
	}
	
	public void testCrossSegmentsFlat() {
		checkDistance(	"LINESTRING (0 0 0, 10 10 0 )",
				"LINESTRING (10 0 0, 0 10 0 )",
		0);
		checkDistance(	"LINESTRING (0 0 10, 30 10 10 )",
				"LINESTRING (10 0 10, 0 10 10 )",
		0);
	}
	
	public void testCrossSegments() {
		checkDistance(	"LINESTRING (0 0 0, 10 10 0 )",
				"LINESTRING (10 0 1, 0 10 1 )",
		1);
		checkDistance(	"LINESTRING (0 0 0, 20 20 0 )",
				"LINESTRING (10 0 1, 0 10 1 )",
		1);
		checkDistance(	"LINESTRING (20 10 20, 10 20 10 )",
				"LINESTRING (10 10 20, 20 20 10 )",
		0);
	}
	
	/**
	 * Many of these tests exhibit robustness errors 
	 * due to numerical roundoff in the distance algorithm mathematics.
	 * This happens when computing nearly-coincident lines 
	 * with very large ordinate values
	 */
	public void testCrossSegmentsRobust() {
		checkDistance(	"LINESTRING (0 0 0, 10000000 10000000 1 )",
				"LINESTRING (0 0 1, 10000000 10000000 0 )",
				0, 0.001);  // expected is 0, but actual is larger
		
		checkDistance(	"LINESTRING (-10000 -10000 0, 10000 10000 1 )",
				"LINESTRING (-10000 -10000 1, 10000 10000 0 )",
				0);
		
		// previous case with X,Y scaled by 1000 - exposes robustness issue
		checkDistance(	"LINESTRING (-10000000 -10000000 0, 10000000 10000000 1 )",
				"LINESTRING (-10000000 -10000000 1, 10000000 10000000 0 )",
				0, 0.02);  // expected is 0, but actual is larger
		
		// works because lines are orthogonal, so doesn't hit roundoff problems
		checkDistance(	"LINESTRING (20000000 10000000 20, 10000000 20000000 10 )",
				"LINESTRING (10000000 10000000 20, 20000000 20000000 10 )",
				0);
	}
	
	public void testTSegmentsFlat() {
		checkDistance(	"LINESTRING (10 10 0, 10 20 0 )",
						"LINESTRING (20 15 0, 25 15 0 )",
				10);
	}
	
	public void testParallelSegmentsFlat() {
		checkDistance(	"LINESTRING (10 10 0, 20 20 0 )",
						"LINESTRING (10 20 0, 20 30 0 )",
						7.0710678118654755);
	}
	
	public void testParallelSegments() {
		checkDistance(	"LINESTRING (0 0 0, 1 0 0 )",
						"LINESTRING (0 0 1, 1 0 1 )",
						1);
		checkDistance(	"LINESTRING (10 10 0, 20 10 0 )",
				"LINESTRING (10 20 10, 20 20 10 )",
				14.142135623730951);
		checkDistance(	"LINESTRING (10 10 0, 20 20 0 )",
				"LINESTRING (10 20 10, 20 30 10 )",
				12.24744871391589 );
				// = distance from LINESTRING (10 10 0, 20 20 0 ) to POINT(10 20 10)
				// = hypotenuse(7.0710678118654755, 10)
	}
	
	public void testLineLine()
	{
		checkDistance(	"LINESTRING (0 1 2, 1 1 1, 1 0 2 )",
				"LINESTRING (0 0 0.1, .5 .5 0, 1 1 0, 1.5 1.5 0, 2 2 0 )",
				1);		
		checkDistance(	"LINESTRING (10 10 20, 20 20 30, 20 20 1, 30 30 5 )",
				"LINESTRING (1 80 10, 0 39 5, 39 0 5, 80 1 20)",
				0.7071067811865476);		
	}
	
	public void testPointPolygon()
	{
		// point above poly
		checkDistance(	"POINT (150 150 10)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				10);	
		// point below poly
		checkDistance(	"POINT (150 150 -10)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				10);				
		// point right of poly in YZ plane
		checkDistance(	"POINT (10 150 150)",
				"POLYGON ((0 100 200, 0 200 200, 0 200 100, 0 100 100, 0 100 200))",
				10);				
	}
	
	public void testPointPolygonFlat()
	{
		// inside
		checkDistance(	"POINT (150 150 0)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				0);	
		// outside
		checkDistance(	"POINT (250 250 0)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				70.71067811865476);				
		// on
		checkDistance(	"POINT (200 200 0)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				0);				
	}
	
	public void testLinePolygonFlat()
	{
		// line inside
		checkDistance(	"LINESTRING (150 150 0, 160 160 0)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				0);	
		// line outside
		checkDistance(	"LINESTRING (200 250 0, 260 260 0)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				50);	
		// line touching
		checkDistance(	"LINESTRING (200 200 0, 260 260 0)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				0);				
	}
	
	public void testLinePolygonSimple()
	{
		// line crossing inside
		checkDistance(	"LINESTRING (150 150 10, 150 150 -10)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				0);	
		// vertical line above
		checkDistance(	"LINESTRING (200 200 10, 260 260 100)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				10);	
		// vertical line touching
		checkDistance(	"LINESTRING (200 200 0, 260 260 100)",
				"POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0))",
				0);				
	}
	
	String polyHoleFlat = "POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0), (120 180 0, 180 180 0, 180 120 0, 120 120 0, 120 180 0))";

	public void testLinePolygonHoleFlat()
	{
		// line crossing hole
		checkDistance(	"LINESTRING (150 150 10, 150 150 -10)", 	polyHoleFlat, 30);	
		// line crossing interior
		checkDistance(	"LINESTRING (110 110 10, 110 110 -10)",		polyHoleFlat, 0);	
		// vertical line above hole
		checkDistance(	"LINESTRING (130 130 10, 150 150 100)",		polyHoleFlat, 14.14213562373095);	
		// vertical line touching hole
		checkDistance(	"LINESTRING (120 180 0, 120 180 100)",		polyHoleFlat, 0);				
	}
	
	public void testPointPolygonHoleFlat()
	{
		// point above poly hole
		checkDistance(	"POINT (130 130 10)", 	polyHoleFlat, 14.14213562373095);	
		// point below poly hole
		checkDistance(	"POINT (130 130 -10)", 	polyHoleFlat, 14.14213562373095);
		// point above poly
		checkDistance(	"POINT (110 110 100)", 	polyHoleFlat, 100);
	}
	
	String poly2HoleFlat = "POLYGON ((100 200 0, 200 200 0, 200 100 0, 100 100 0, 100 200 0), (110 110 0, 110 130 0, 130 130 0, 130 110 0, 110 110 0), (190 110 0, 170 110 0, 170 130 0, 190 130 0, 190 110 0))";	

	/**
	 * A case proving that polygon/polygon distance requires 
	 * computing distance between all rings, not just the shells.
	 */
	public void testPolygonPolygonLinkedThruHoles()
	{
		// note distance is zero!
		checkDistance(	
				// polygon with two holes
				poly2HoleFlat,
				// polygon parallel to XZ plane with shell passing through holes in other polygon
				"POLYGON ((120 120 -10, 120 120 100, 180 120 100, 180 120 -10, 120 120 -10))", 
				0);	
		
		// confirm that distance of simple poly boundary is non-zero
		checkDistance(	
				// polygon with two holes
				poly2HoleFlat,
				// boundary of polygon parallel to XZ plane with shell passing through holes
				"LINESTRING (120 120 -10, 120 120 100, 180 120 100, 180 120 -10, 120 120 -10)", 
				10);	
	}

	
	public void testMultiPoint()
	{
		checkDistance(
				"MULTIPOINT ((0 0 0), (0 0 100), (100 100 100))",
				"MULTIPOINT ((100 100 99), (50 50 50), (25 100 33))",
				1
				);
	}
	
	public void testMultiLineString()
	{
		checkDistance(
				"MULTILINESTRING ((0 0 0, 10 10 10), (0 0 100, 25 25 25, 40 40 50), (100 100 100, 100 101 102))",
				"MULTILINESTRING ((100 100 99, 100 100 99), (100 102 102, 200 200 20), (25 100 33, 25 100 35))",
				1
				);
	}
	
	public void testMultiPolygon()
	{
		checkDistance(
				// Polygons parallel to XZ plane
				"MULTIPOLYGON ( ((120 120 -10, 120 120 100, 180 120 100, 180 120 -10, 120 120 -10)), ((120 200 -10, 120 200 190, 180 200 190, 180 200 -10, 120 200 -10)) )",
				// Polygons parallel to XY plane
				"MULTIPOLYGON ( ((100 200 200, 200 200 200, 200 100 200, 100 100 200, 100 200 200)), ((100 200 210, 200 200 210, 200 100 210, 100 100 210, 100 200 210)) )",
				10
				);
	}
	
	public void testMultiMixed()
	{
		checkDistance(
				"MULTILINESTRING ((0 0 0, 10 10 10), (0 0 100, 25 25 25, 40 40 50), (100 100 100, 100 101 101))",
				"MULTIPOINT ((100 100 99), (50 50 50), (25 100 33))",
				1
				);
	}
	
	//==========================================================
	// Convenience methods
	//==========================================================
	
	private static final double DIST_TOLERANCE = 0.00001;
	
	private void checkDistance(String wkt1, String wkt2, double expectedDistance)
	{
		checkDistance(wkt1, wkt2, expectedDistance, DIST_TOLERANCE);
	}

	private void checkDistance(String wkt1, String wkt2, double expectedDistance, double tolerance)
	{
		Geometry g1;
		Geometry g2;
		try {
			g1 = rdr.read(wkt1);
		} catch (ParseException e) {
			throw new RuntimeException(e.toString());
		}
		try {
			g2 = rdr.read(wkt2);
		} catch (ParseException e) {
			throw new RuntimeException(e.toString());
		}
		// check both orders for arguments
		checkDistance(g1, g2, expectedDistance, tolerance);
		checkDistance(g2, g1, expectedDistance, tolerance);
	}

	private void checkDistance(Geometry g1, Geometry g2, double expectedDistance, double tolerance)
	{
		Distance3DOp distOp = new Distance3DOp(g1, g2);
		double dist = distOp.distance();
		assertEquals(expectedDistance, dist, tolerance);
	}
	
}
