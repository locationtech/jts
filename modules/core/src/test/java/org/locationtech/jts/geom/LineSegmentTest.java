/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


/**
 * Test LineSegment methods
 */
public class LineSegmentTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(LineSegmentTest.class);
  }

  public LineSegmentTest(String name) { super(name); }

  private static double ROOT2 = Math.sqrt(2);
  
  /**
   * Test hash code collisions.
   * 
   * See https://github.com/locationtech/jts/issues/871
   */
  public void testHashCode() {
    checkHashcode(new LineSegment(0, 0, 10, 0), new LineSegment(0, 10, 10, 10));
    checkHashcode(new LineSegment(580.0, 1330.0, 590.0, 1330.0), new LineSegment(580.0, 1340.0, 590.0, 1340.));
  }

  private void checkHashcode(LineSegment seg, LineSegment seg2) {
    //System.out.format("Seg 1: %d   Seg 2: %d\n", seg.hashCode(), seg2.hashCode());
    assertTrue(seg.hashCode() != seg2.hashCode());
  }
  
  public void testProjectionFactor()
  {
    // zero-length line
    LineSegment seg = new LineSegment(10, 0, 10, 0);
    assertTrue(Double.isNaN(seg.projectionFactor(new Coordinate(11, 0))));
    
    LineSegment seg2 = new LineSegment(10, 0, 20, 0);
    assertTrue(seg2.projectionFactor(new Coordinate(11, 0)) == 0.1);
  }
  
  public void testProjectPoint() {
    //-- interior point
    checkProjectPoint("LINESTRING (4 0, 8 0)", "POINT (5 2)", 5, 0);
    //-- endpoint
    checkProjectPoint("LINESTRING (4 0, 8 0)", "POINT (8 2)", 8, 0);
    //-- beyond end
    checkProjectPoint("LINESTRING (4 0, 8 0)", "POINT (9 2)", 9, 0);
    //-- before end
    checkProjectPoint("LINESTRING (4 0, 8 0)", "POINT (3 2)", 3, 0);
    //-- collinear
    checkProjectPoint("LINESTRING (4 0, 8 0)", "POINT (2 0)", 2, 0);
  }
  
  private void checkProjectPoint(String wkt1, String wkt2, double x, double y) {
    LineSegment seg1 = readLineSegment(wkt1);
    Point pt = (Point) read(wkt2);
    Coordinate p = pt.getCoordinate();
    Coordinate actual = seg1.project(p);
    
    checkEqualXY(new Coordinate(x, y), actual, 0.0001);
  }

  public void testProjectSegment() {
    //-- project onto interior segment
    checkProjectSegment("LINESTRING (0 0, 8 0)", "LINESTRING (1 2, 2 3)", "LINESTRING(1 0, 2 0)");
    //-- project onto interior point
    checkProjectSegment("LINESTRING (0 0, 8 0)", "LINESTRING (1 2, 1 4)", "LINESTRING(1 0, 1 0)");
    //-- projection includes endpoint
    checkProjectSegment("LINESTRING (0 0, 8 0)", "LINESTRING (0 2, 1 4)", "LINESTRING(0 0, 1 0)");
    //- projection onto endpoint
    checkProjectSegment("LINESTRING (0 0, 8 0)", "LINESTRING (8 2, 8 4)",  "LINESTRING(8 0, 8 0)");
    checkProjectSegment("LINESTRING (0 0, 8 0)", "LINESTRING (0 2, 0 4)",  "LINESTRING(0 0, 0 0)");
    checkProjectSegment("LINESTRING (0 0, 8 0)", "LINESTRING (0 2, -1 4)", "LINESTRING(0 0, 0 0)");
    checkProjectSegment("LINESTRING (0 0, 8 0)", "LINESTRING (9 1, 8 0)",  "LINESTRING(8 0, 8 0)");
    checkProjectSegment("LINESTRING (0 0, 8 0)", "LINESTRING (9 1, 8 1)",  "LINESTRING(8 0, 8 0)");
    //-- no projection
    checkProjectSegment("LINESTRING (0 0, 8 0)", "LINESTRING (9 1, 9 2)", null);
  }

  private void checkProjectSegment(String wkt1, String wkt2, String wktExpected) {
    LineSegment seg1 = readLineSegment(wkt1);
    LineSegment seg2 = readLineSegment(wkt2);
    LineSegment actual = seg1.project(seg2);
    
    LineSegment expected = wktExpected == null ? null : readLineSegment(wktExpected);
    checkEqual(expected, actual, 0.0001);
  }
  
  private LineSegment readLineSegment(String wkt) {
    Geometry g = read(wkt);
    LineString line = (LineString) g;
    return new LineSegment(line.getCoordinateN(0), line.getCoordinateN(1));
  }

  public void testLineIntersection() {
    // simple case
    checkLineIntersection(
        0,0,  10,10,
        0,10, 10,0,
        5,5);

    //Almost collinear - See JTS GitHub issue #464
    checkLineIntersection(
        35613471.6165017, 4257145.306132293, 35613477.7705378, 4257160.528222711,
        35613477.77505724, 4257160.539653536, 35613479.85607389, 4257165.92369170,
        35613477.772841461, 4257160.5339209242 );
  }
  
  private static final double MAX_ABS_ERROR_INTERSECTION = 1e-5;
  
  private void checkLineIntersection(double p1x, double p1y, double p2x, double p2y, 
      double q1x, double q1y, double q2x, double q2y, 
      double expectedx, double expectedy) {
    LineSegment seg1 = new LineSegment(p1x, p1y, p2x, p2y);
    LineSegment seg2 = new LineSegment(q1x, q1y, q2x, q2y);
    
    Coordinate actual = seg1.lineIntersection(seg2);
    Coordinate expected = new Coordinate( expectedx, expectedy );
    double dist = actual.distance(expected);
    //System.out.println("Expected: " + expected + "  Actual: " + actual + "  Dist = " + dist);
    assertTrue(dist <= MAX_ABS_ERROR_INTERSECTION);
  }

  public void testDistancePerpendicular() {
    checkDistancePerpendicular(1,1,  1,3,  2,4, 1);
    checkDistancePerpendicular(1,1,  1,3,  0,4, 1);
    checkDistancePerpendicular(1,1,  1,3,  1,4, 0);
    checkDistancePerpendicular(1,1,  2,2,  4,4, 0);
    //-- zero-length line segment
    checkDistancePerpendicular(1,1,  1,1,  1,2, 1);
  }
  
  public void testDistancePerpendicularOriented() {
    //-- right of line
    checkDistancePerpendicularOriented(1,1,  1,3,  2,4, -1);
    //-- left of line
    checkDistancePerpendicularOriented(1,1,  1,3,  0,4, 1);
    //-- on line
    checkDistancePerpendicularOriented(1,1,  1,3,  1,4, 0);
    checkDistancePerpendicularOriented(1,1,  2,2,  4,4, 0);
    //-- zero-length segment
    checkDistancePerpendicularOriented(1,1,  1,1,  1,2, 1);    
  }
  
  private void checkDistancePerpendicular(double x0, double y0, double x1, double y1, double px, double py, 
      double expected) {
    LineSegment seg = new LineSegment(x0, y0, x1, y1);
    double dist = seg.distancePerpendicular(new Coordinate(px, py));
    assertEquals(expected, dist, 0.000001);
  }
  
  private void checkDistancePerpendicularOriented(double x0, double y0, double x1, double y1, double px, double py, 
      double expected) {
    LineSegment seg = new LineSegment(x0, y0, x1, y1);
    double dist = seg.distancePerpendicularOriented(new Coordinate(px, py));
    assertEquals(expected, dist, 0.000001);
  }
  
  public void testOffsetPoint() throws Exception
  {
    checkOffsetPoint(0, 0, 10, 10, 0.0, ROOT2, -1, 1);
    checkOffsetPoint(0, 0, 10, 10, 0.0, -ROOT2, 1, -1);
    
    checkOffsetPoint(0, 0, 10, 10, 1.0, ROOT2, 9, 11);
    checkOffsetPoint(0, 0, 10, 10, 0.5, ROOT2, 4, 6);
    
    checkOffsetPoint(0, 0, 10, 10, 0.5, -ROOT2, 6, 4);
    checkOffsetPoint(0, 0, 10, 10, 0.5, -ROOT2, 6, 4);
    
    checkOffsetPoint(0, 0, 10, 10, 2.0, ROOT2, 19, 21);
    checkOffsetPoint(0, 0, 10, 10, 2.0, -ROOT2, 21, 19);
    
    checkOffsetPoint(0, 0, 10, 10, 2.0, 5 * ROOT2, 15, 25);
    checkOffsetPoint(0, 0, 10, 10, -2.0, 5 * ROOT2, -25, -15);

  }

  public void testOffsetLine() throws Exception
  {
    checkOffsetLine(0, 0, 10, 10, 0, 0, 0, 10, 10 );
    
    checkOffsetLine(0, 0, 10, 10, ROOT2, -1, 1,  9, 11 );
    checkOffsetLine(0, 0, 10, 10, -ROOT2, 1, -1, 11, 9);
  }
  
  void checkOffsetPoint(double x0, double y0, double x1, double y1, double segFrac, double offset, 
      double expectedX, double expectedY)
  {
    LineSegment seg = new LineSegment(x0, y0, x1, y1);
    Coordinate p = seg.pointAlongOffset(segFrac, offset);
    
    assertTrue(equalsTolerance(new Coordinate(expectedX, expectedY), p, 0.000001));
  }
  
  void checkOffsetLine(double x0, double y0, double x1, double y1, double offset, 
      double expectedX0, double expectedY0, double expectedX1, double expectedY1)
  {
    LineSegment seg = new LineSegment(x0, y0, x1, y1);
    LineSegment actual = seg.offset(offset);
    
    assertTrue(equalsTolerance(new Coordinate(expectedX0, expectedY0), actual.p0, 0.000001));
    assertTrue(equalsTolerance(new Coordinate(expectedX1, expectedY1), actual.p1, 0.000001));
  }
  
  public static boolean equalsTolerance(Coordinate p0, Coordinate p1, double tolerance)
  {
  	if (Math.abs(p0.x - p1.x) > tolerance) return false;
  	if (Math.abs(p0.y - p1.y) > tolerance) return false;
  	return true;
  }
  
  public void testReflect() {
    checkReflect(0, 0, 10, 10, 1,2, 2 ,1 );
    checkReflect(0, 1, 10, 1, 1, 2, 1, 0 );
  }
  
  void checkReflect(double x0, double y0, double x1, double y1, double x, double y, 
      double expectedX, double expectedY)
  {
    LineSegment seg = new LineSegment(x0, y0, x1, y1);
    Coordinate p = seg.reflect(new Coordinate(x, y));
    assertTrue(equalsTolerance(new Coordinate(expectedX, expectedY), p, 0.000001));
  }
  
  public void testOrientationIndexCoordinate()
  {
  	LineSegment seg = new LineSegment(0, 0, 10, 10);
  	checkOrientationIndex(seg, 10, 11, 1);
  	checkOrientationIndex(seg, 10, 9, -1);
  	
  	checkOrientationIndex(seg, 11, 11, 0);
  	
  	checkOrientationIndex(seg, 11, 11.0000001, 1);
  	checkOrientationIndex(seg, 11, 10.9999999, -1);
  	
  	checkOrientationIndex(seg, -2, -1.9999999, 1);
  	checkOrientationIndex(seg, -2, -2.0000001, -1);
  }
  
  public void testOrientationIndexSegment()
  {
  	LineSegment seg = new LineSegment(100, 100, 110, 110);
  	
  	checkOrientationIndex(seg, 100, 101, 105, 106, 1);
  	checkOrientationIndex(seg, 100, 99, 105, 96, -1);
  	
  	checkOrientationIndex(seg, 200, 200, 210, 210, 0);
  	
  	checkOrientationIndex(seg, 105, 105, 110, 100, -1);
  	
  }
  
  void checkOrientationIndex(double x0, double y0, double x1, double y1, double px, double py, 
  		int expectedOrient)
  {
  	LineSegment seg = new LineSegment(x0, y0, x1, y1);
  	checkOrientationIndex(seg, px, py, expectedOrient);
  }
  
  void checkOrientationIndex(LineSegment seg, 
  		double px, double py, 
  		int expectedOrient)
  {
  	Coordinate p = new Coordinate(px, py);
  	int orient = seg.orientationIndex(p);
  	assertTrue(orient == expectedOrient);
  }
  
  void checkOrientationIndex(LineSegment seg, 
  		double s0x, double s0y, 
  		double s1x, double s1y, 
  		int expectedOrient)
  {
  	LineSegment seg2 = new LineSegment(s0x, s0y, s1x, s1y);
  	int orient = seg.orientationIndex(seg2);
  	String msg = "";
  	if (orient != expectedOrient) {
  	  msg = "orientationIndex of " + seg + " and " + seg2;
  	}
  	assertEquals(msg, expectedOrient, orient);
  }
  

}
