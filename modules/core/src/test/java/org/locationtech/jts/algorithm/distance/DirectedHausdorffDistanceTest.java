/*
 * Copyright (c) 2026 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class DirectedHausdorffDistanceTest 
extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(DirectedHausdorffDistanceTest.class);
  }

  public DirectedHausdorffDistanceTest(String name) { super(name); }

  //-- empty inputs
  
  public void testEmptyPoint()
  {
    checkDistanceEmpty("POINT EMPTY", "POINT (1 1)");
  }
  
  public void testEmptyLine()
  {
    checkDistanceEmpty("LINESTRING EMPTY", "LINESTRING (0 0, 2 1)");
  }
  
  public void testEmptyPolygon()
  {
    checkDistanceEmpty("POLYGON EMPTY", "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))");
  }
  
  //--------------------------------------
  //-- extreme and invalid inputs
  
  public void testZeroTolerancePoint()
  {
    checkDistance("POINT (5 5)", "LINESTRING (5 1, 9 5)",
        0,
        "LINESTRING (5 5, 7 3)");
  }
  
  public void testZeroToleranceLine()
  {
    checkDistance("LINESTRING (1 5, 5 5)", "LINESTRING (5 1, 9 5)",
        0,
        "LINESTRING (1 5, 5 1)");
  }
  
  public void testZeroToleranceZeroLengthLineQuery()
  {
    checkDistance("LINESTRING (5 5, 5 5)", "LINESTRING (5 1, 9 5)",
        0,
        "LINESTRING (5 5, 7 3)");
  }
  
  public void testZeroLengthLineQuery()
  {
    checkDistance("LINESTRING (5 5, 5 5)", "LINESTRING (5 1, 9 5)",
        "LINESTRING (5 5, 7 3)");
  }
  
  public void testZeroLengthPolygonQuery()
  {
    checkDistance("POLYGON ((5 5, 5 5, 5 5, 5 5))", "LINESTRING (5 1, 9 5)",
        "LINESTRING (5 5, 7 3)");
  }
  
  public void testZeroLengthLineTarget()
  {
    checkDistance("POINT (5 5)", "LINESTRING (5 1, 5 1)",
        "LINESTRING (5 5, 5 1)");
  }
  
  public void testNegativeTolerancePoint()
  {
    try {
      checkDistance("POINT (5 5)", "LINESTRING (5 1, 9 5)",
          -1,
          "LINESTRING (5 5, 7 3)");
      fail();
    }
    catch (IllegalArgumentException expected) {
      
    }
  }
  
  public void testNegativeToleranceLine()
  {
    try {
      checkDistance("LINESTRING (1 5, 5 5)", "LINESTRING (5 1, 9 5)",
        -1,
        "LINESTRING (1 5, 5 1)");
      fail();
    }
    catch (IllegalArgumentException expected) {
    
    }
  }
  
  //--------------------------------------
  
  
  public void testPointPoint()
  {
    checkHausdorff("POINT (0 0)", "POINT (1 1)", 
        "LINESTRING (0 0, 1 1)");
  }
  
  public void testPointsPoints()
  {
    String a = "MULTIPOINT ((0 1), (2 3), (4 5), (6 6))";
    String b = "MULTIPOINT ((0.1 0), (1 0), (2 0), (3 0), (4 0), (5 0))";
    checkDistance(a, b, "LINESTRING (6 6, 5 0)");
    checkDistance(b, a, "LINESTRING (5 0, 2 3)");
    checkHausdorff(a, b, "LINESTRING (6 6, 5 0)");
  }
  
  public void testPointPolygonInterior()
  {
    checkDistance("POINT (3 4)", "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        0);
  }
  
  public void testPointsPolygon()
  {
    checkDistance("MULTIPOINT ((4 3), (2 8), (8 5))", "POLYGON ((6 9, 6 4, 9 1, 1 1, 6 9))",
        "LINESTRING (2 8, 4.426966292134832 6.48314606741573)");
  }
  
  public void testLineSegments()
  {
    checkHausdorff("LINESTRING (0 0, 2 0)", "LINESTRING (0 0, 2 1)",
        "LINESTRING (2 0, 2 1)");
  }
  
  public void testLineSegments2()
  {
    checkHausdorff("LINESTRING (0 0, 2 0)", "LINESTRING (0 1, 1 2, 2 1)", 
        "LINESTRING (1 0, 1 2)");
  }
  
  public void testLinePoints()
  {
    checkHausdorff("LINESTRING (0 0, 2 0)", "MULTIPOINT (0 2, 1 0, 2 1)", 
        "LINESTRING (0 0, 0 2)");
  }
  
  public void testLinesTopoEqual()
  {
    checkDistance("MULTILINESTRING ((10 10, 10 90, 40 30), (40 30, 60 80, 90 30, 40 10))", 
        "LINESTRING (10 10, 10 90, 40 30, 60 80, 90 30, 40 10)", 
        0.0);
  }
  
  public void testLinesPolygon()
  {
    checkHausdorff("MULTILINESTRING ((1 1, 2 7), (7 1, 9 9))", 
        "POLYGON ((3 7, 6 7, 6 4, 3 4, 3 7))", 
        "LINESTRING (9 9, 6 7)");
  }
  
  public void testLinesPolygon2()
  {
    String a = "MULTILINESTRING ((2 3, 2 7), (9 1, 9 8, 4 9))";
    String b = "POLYGON ((3 7, 6 8, 8 2, 3 4, 3 7))";
    checkDistance(a, b, "LINESTRING (9 8, 6.3 7.1)");
    checkHausdorff(a, b, "LINESTRING (2 3, 5.5 3)");
  }
  
  public void testPolygonLineCrossingBoundaryResult()
  {
    checkDistance("POLYGON ((2 8, 8 2, 2 1, 2 8))", 
        "LINESTRING (6 5, 4 7, 0 0, 8 4)", 
        "LINESTRING (2 8, 3.9384615384615387 6.892307692307693)");
  }
  
  public void testPolygonLineCrossingInteriorPoint()
  {
    checkDistanceStartPtLen("POLYGON ((2 8, 8 2, 2 1, 2 8))", 
        "LINESTRING (6 5, 4 7, 0 0, 9 1)", 
        "LINESTRING (4.555 2.989, 4.828 0.536)", 0.01);
  }
  
  public void testPolygonPolygon()
  {
    String a = "POLYGON ((2 18, 18 18, 17 3, 2 2, 2 18))";
    String b = "POLYGON ((1 19, 5 12, 5 3, 14 10, 11 19, 19 19, 20 0, 1 1, 1 19))";
    checkDistance(b, a, "LINESTRING (20 0, 17 3)");
    checkDistance(a, b, "LINESTRING (6.6796875 18, 11 19)");
    checkHausdorff(a, b, "LINESTRING (6.6796875 18, 11 19)");
  }
  
  public void testPolygonPolygonHolesNested()
  {
    // B is contained in A
    String a = "POLYGON ((1 19, 19 19, 19 1, 1 1, 1 19), (6 8, 11 14, 15 7, 6 8))";
    String b = "POLYGON ((2 18, 18 18, 18 2, 2 2, 2 18), (10 17, 3 7, 17 5, 10 17))";
    checkDistance(a, b, "LINESTRING (9.817138671875 12.58056640625, 7.863620425230705 13.948029178901006)");
    checkDistance(b, a, 0.0);
  }
  
  public void testMultiPolygons()
  {
    String a = "MULTIPOLYGON (((1 1, 1 10, 5 1, 1 1)), ((4 17, 9 15, 9 6, 4 17)))";
    String b = "MULTIPOLYGON (((1 12, 4 13, 8 10, 1 12)), ((3 8, 7 7, 6 2, 3 8)))";
    checkDistance(a, b, "LINESTRING (1 1, 5.4 3.2)");
    checkDistanceStartPtLen(b, a, 
        "LINESTRING (2.669921875 12.556640625, 5.446115154109589 13.818546660958905)",
        0.01);
  }
  
  // Tests that target area interiors have distance = 0
  public void testLinePolygonCrossing() throws Exception
  {
    String wkt1 = "LINESTRING (2 5, 5 10, 6 4)";
    String wkt2 = "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))";
    checkDistance(wkt1, wkt2, "LINESTRING (5 10, 5 9)");
  } 
  
  public void testNonVertexResult()
  {
    String wkt1 = "LINESTRING (1 1, 5 10, 9 1)";
    String wkt2 = "LINESTRING (0 10, 0 0, 10 0)";
    
    checkHausdorff(wkt1, wkt2, "LINESTRING (6.53857421875 6.5382080078125, 6.53857421875 0)");
    checkDistance(wkt1, wkt2, "LINESTRING (6.53857421875 6.5382080078125, 6.53857421875 0)");
  }
  
  public void testDirectedLines()
  {
    String wkt1 = "LINESTRING (1 6, 3 5, 1 4)";
    String wkt2 = "LINESTRING (1 10, 9 5, 1 2)";
    checkDistance(wkt1, wkt2, "LINESTRING (1 6, 2.797752808988764 8.876404494382022)");
    checkDistance(wkt2, wkt1, "LINESTRING (9 5, 3 5)");
  }

  public void testDirectedLines2()
  {
    String wkt1 = "LINESTRING (1 6, 3 5, 1 4)";
    String wkt2 = "LINESTRING (1 3, 1 9, 9 5, 1 1)";
    checkDistance(wkt1, wkt2, "LINESTRING (3 5, 1 5)");
    checkDistance(wkt2, wkt1, "LINESTRING (9 5, 3 5)");
  }

  /**
   * Tests that segments are detected as interior even for a large tolerance.
   */
  public void testInteriorSegmentsLargeTol() 
  {
    String a = "POLYGON ((4 6, 5 6, 5 5, 4 5, 4 6))";
    String b = "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))";
    checkDistance(a, b, 2.0, 0.0);
  }
  
  /**
   * Tests that segment endpoint nearest points 
   * which are interior to B have distance 0
   */
  public void testInteriorSegmentsSameExterior() 
  {
    String a = "POLYGON ((1 9, 3 9, 4 5, 5.05 9, 9 9, 9 1, 1 1, 1 9))";
    String b = "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))";
    checkDistance(a, b, 0.0);
  }
  
  //-----------------------------------------------------
  
  public void testFullyWithinDistanceEmptyPoints()
  {
    String a = "POINT EMPTY";
    String b = "MULTIPOINT ((1 1), (9 9))";
    checkFullyWithinDistanceEmpty(a, b);
  }

  public void testFullyWithinDistanceEmptyLine()
  {
    String a = "LINESTRING EMPTY";
    String b = "LINESTRING (9 9, 1 1)";
    checkFullyWithinDistanceEmpty(a, b);
  }
  
  //-- shows withinDistance envelope check not triggering for disconnected A
  public void testFullyWithinDistancePoints()
  {
    String a = "MULTIPOINT ((1 9), (9 1))";
    String b = "MULTIPOINT ((1 1), (9 9))";
    checkFullyWithinDistance(a, b, 1, false);
    checkFullyWithinDistance(a, b, 8.1, true);
  }

  public void testFullyWithinDistanceDisconnectedLines()
  {
    String a = "MULTILINESTRING ((1 9, 2 9), (8 1, 9 1))";
    String b = "LINESTRING (9 9, 1 1)";
    checkFullyWithinDistance(a, b, 1, false);
    checkFullyWithinDistance(a, b, 6, true);
    checkFullyWithinDistance(b, a, 1, false);
    checkFullyWithinDistance(b, a, 7.1, true);
  }

  public void testFullyWithinDistanceDisconnectedPolygons()
  {
    String a = "MULTIPOLYGON (((1 9, 2 9, 2 8, 1 8, 1 9)), ((8 2, 9 2, 9 1, 8 1, 8 2)))";
    String b = "POLYGON ((1 2, 9 9, 2 1, 1 2))";
    checkFullyWithinDistance(a, b, 1, false);
    checkFullyWithinDistance(a, b, 5.3, true);
    checkFullyWithinDistance(b, a, 1, false);
    checkFullyWithinDistance(b, a, 7.1, true);
  }

  public void testFullyWithinDistanceLines()
  {
    String a = "MULTILINESTRING ((1 1, 3 3), (7 7, 9 9))";
    String b = "MULTILINESTRING ((1 9, 1 5), (6 4, 8 2))";
    checkFullyWithinDistance(a, b, 1, false);
    checkFullyWithinDistance(a, b, 4, false);
    checkFullyWithinDistance(a, b, 6, true);
  }

  public void testFullyWithinDistancePolygons()
  {
    String a = "POLYGON ((1 4, 4 4, 4 1, 1 1, 1 4))";
    String b = "POLYGON ((10 10, 10 15, 15 15, 15 10, 10 10))";
    checkFullyWithinDistance(a, b, 5, false);
    checkFullyWithinDistance(a, b, 10, false);
    checkFullyWithinDistance(a, b, 20, true);
  }

  public void testFullyWithinDistancePolygonsNestedWithHole()
  {
    String a = "POLYGON ((2 8, 8 8, 8 2, 2 2, 2 8))";
    String b = "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (3 7, 7 7, 7 3, 3 3, 3 7))";
    checkFullyWithinDistance(a, b, 1, false);
    checkFullyWithinDistance(a, b, 2, true);
    checkFullyWithinDistance(a, b, 3, true);
  }

  //======================================================================
  
  private static final double TOLERANCE = 0.001;
  
  private void checkHausdorff(String wkt1, String wkt2, String wktExpected) 
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Coordinate[] pts = DirectedHausdorffDistance.hausdorffDistancePoints(g1, g2);
    Geometry result = g1.getFactory().createLineString(pts);
    Geometry expected = read(wktExpected);
    checkEqualExact(expected, result, TOLERANCE);
  }

  private void checkDistance(String wkt1, String wkt2, String wktExpected) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Coordinate[] pts = DirectedHausdorffDistance.distancePoints(g1, g2);
    Geometry result = g1.getFactory().createLineString(pts);
    Geometry expected = read(wktExpected);
    checkEqualExact(expected, result, TOLERANCE);
  }
  
  private void checkDistance(String wkt1, String wkt2, double tolerance, String wktExpected) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Coordinate[] pts = DirectedHausdorffDistance.distancePoints(g1, g2, tolerance);
    Geometry result = g1.getFactory().createLineString(pts);
    Geometry expected = read(wktExpected);
    checkEqualExact(expected, result, TOLERANCE);
  }
  
  private void checkDistanceStartPtLen(String wkt1, String wkt2, 
      String wktExpected, double resultTolerance) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Coordinate[] pts = DirectedHausdorffDistance.distancePoints(g1, g2);
    Geometry result = g1.getFactory().createLineString(pts);
    Geometry expected = read(wktExpected);
    
    Coordinate resultPt = result.getCoordinates()[0];
    Coordinate expectedPt = expected.getCoordinates()[0];
    checkEqualXY(expectedPt, resultPt, resultTolerance);
    
    double distResult = result.getLength();
    double distExpected = expected.getLength();
    assertEquals(distExpected, distResult, resultTolerance);
  }
  
  private void checkDistance(String wkt1, String wkt2, double tolerance, 
      double expectedDistance) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    double distResult = DirectedHausdorffDistance.distance(g1, g2, tolerance);
    assertEquals(expectedDistance, distResult, TOLERANCE);
  }

  private void checkDistance(String wkt1, String wkt2, 
      double expectedDistance) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    double distResult = DirectedHausdorffDistance.distance(g1, g2);
    assertEquals(expectedDistance, distResult, TOLERANCE);
  }

  private void checkFullyWithinDistance(String a, String b, double distance, boolean expected) {
    Geometry g1 = read(a);
    Geometry g2 = read(b);
    
    boolean result = DirectedHausdorffDistance.isFullyWithinDistance(g1, g2, distance);
    assertEquals(expected, result);
  }
  
  private void checkFullyWithinDistanceEmpty(String a, String b) {
    checkFullyWithinDistance(a, b, 0, false);
    checkFullyWithinDistance(b, a, 0, false);
    checkFullyWithinDistance(a, b, 1, false);
    checkFullyWithinDistance(b, a, 1, false);
    checkFullyWithinDistance(a, b, 1000, false);
    checkFullyWithinDistance(b, a, 1000, false);
  }
  
  private void checkDistanceEmpty(String a, String b) {
    Geometry g1 = read(a);
    Geometry g2 = read(b);
    
    Coordinate[] resultPts = DirectedHausdorffDistance.distancePoints(g1, g2);
    assert(resultPts == null);
    
    double resultDist = DirectedHausdorffDistance.distance(g1, g2);
    assert(Double.isNaN(resultDist));
    
    double hausdorffDist = DirectedHausdorffDistance.hausdorffDistance(g1, g2);
    assert(Double.isNaN(hausdorffDist));
  }
}
