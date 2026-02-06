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

  public void testPointPoint()
  {
    checkHD("POINT (0 0)", "POINT (1 1)", 
        0.01,
        "LINESTRING (0 0, 1 1)");
  }
  
  public void testPointsPoints()
  {
    String a = "MULTIPOINT ((0 1), (2 3), (4 5), (6 6))";
    String b = "MULTIPOINT ((0.1 0), (1 0), (2 0), (3 0), (4 0), (5 0))";
    checkDistance(a, b, 0.01, "LINESTRING (6 6, 5 0)");
    checkDistance(b, a, 0.01, "LINESTRING (5 0, 2 3)");
    checkHD(a, b, 0.01, "LINESTRING (6 6, 5 0)");
  }
  
  public void testPointPolygonInterior()
  {
    checkDistance("POINT (3 4)", "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        0.01,
        0);
  }
  
  public void testPointsPolygon()
  {
    checkDistance("MULTIPOINT ((4 3), (2 8), (8 5))", "POLYGON ((6 9, 6 4, 9 1, 1 1, 6 9))",
        0.01,
        "LINESTRING (2 8, 4.426966292134832 6.48314606741573)");
  }
  
  public void testLineSegments()
  {
    checkHD("LINESTRING (0 0, 2 0)", "LINESTRING (0 0, 2 1)",
        0.01,
        "LINESTRING (2 0, 2 1)");
  }
  
  public void testLineSegments2()
  {
    checkHD("LINESTRING (0 0, 2 0)", "LINESTRING (0 1, 1 2, 2 1)", 
        0.01,
        "LINESTRING (1 0, 1 2)");
  }
  
  public void testLinePoints()
  {
    checkHD("LINESTRING (0 0, 2 0)", "MULTIPOINT (0 2, 1 0, 2 1)", 
        0.01,
        "LINESTRING (0 0, 0 2)");
  }
  
  public void testLinesPolygon()
  {
    checkHD("MULTILINESTRING ((1 1, 2 7), (7 1, 9 9))", 
        "POLYGON ((3 7, 6 7, 6 4, 3 4, 3 7))", 
        0.01,
        "LINESTRING (9 9, 6 7)");
  }
  
  public void testLinesPolygon2()
  {
    String a = "MULTILINESTRING ((2 3, 2 7), (9 1, 9 8, 4 9))";
    String b = "POLYGON ((3 7, 6 8, 8 2, 3 4, 3 7))";
    checkDistance(a, b, 0.01, "LINESTRING (9 8, 6.3 7.1)");
    checkHD(a, b, 0.01, "LINESTRING (2 3, 5.5 3)");
  }
  
  public void testPolygonLineCrossingBoundaryResult()
  {
    checkDistance("POLYGON ((2 8, 8 2, 2 1, 2 8))", 
        "LINESTRING (6 5, 4 7, 0 0, 8 4)", 
        0.001,
        "LINESTRING (2 8, 3.9384615384615387 6.892307692307693)");
  }
  
  public void testPolygonLineCrossingInteriorPoint()
  {
    checkDistanceStartPtLen("POLYGON ((2 8, 8 2, 2 1, 2 8))", 
        "LINESTRING (6 5, 4 7, 0 0, 9 1)", 
        0.001,
        "LINESTRING (4.557 2.9937, 2.4 4.2)", 0.01);
  }
  
  public void testPolygonPolygon()
  {
    String a = "POLYGON ((2 18, 18 18, 17 3, 2 2, 2 18))";
    String b = "POLYGON ((1 19, 5 12, 5 3, 14 10, 11 19, 19 19, 20 0, 1 1, 1 19))";
    checkDistance(b, a, 0.01, "LINESTRING (20 0, 17 3)");
    checkDistance(a, b, 0.01, "LINESTRING (6.6796875 18, 11 19)");
    checkHD(a, b, 0.01, "LINESTRING (6.6796875 18, 11 19)");
  }
  
  public void testPolygonPolygonHolesNested()
  {
    // B is contained in A
    String a = "POLYGON ((1 19, 19 19, 19 1, 1 1, 1 19), (6 8, 11 14, 15 7, 6 8))";
    String b = "POLYGON ((2 18, 18 18, 18 2, 2 2, 2 18), (10 17, 3 7, 17 5, 10 17))";
    checkDistance(a, b, 0.01, "LINESTRING (9.8134765625 12.576171875, 7.8603515625 13.943359375)");
    checkDistance(b, a, 0.01, 0.0);
  }
  
  public void testMultiPolygons()
  {
    String a = "MULTIPOLYGON (((1 1, 1 10, 5 1, 1 1)), ((4 17, 9 15, 9 6, 4 17)))";
    String b = "MULTIPOLYGON (((1 12, 4 13, 8 10, 1 12)), ((3 8, 7 7, 6 2, 3 8)))";
    checkDistance(a, b, 0.01, "LINESTRING (1 1, 5.4 3.2)");
    checkDistance(b, a, 0.001, "LINESTRING (2.669921875 12.556640625, 5.446115154109589 13.818546660958905)");
  }
  
  // Tests that target area interiors have distance = 0
  public void testLinePolygonCrossing() throws Exception
  {
    String wkt1 = "LINESTRING (2 5, 5 10, 6 4)";
    String wkt2 = "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))";
    checkDistance(wkt1, wkt2, 0.01, "LINESTRING (5 10, 5 9)");
  } 
  
  public void testNonVertexResult()
  {
    String wkt1 = "LINESTRING (1 1, 5 10, 9 1)";
    String wkt2 = "LINESTRING (0 10, 0 0, 10 0)";
    
    checkHD(wkt1, wkt2, 0.01, "LINESTRING (6.5390625 6.537109375, 6.5390625 0)");
    checkDistance(wkt1, wkt2, 0.01, "LINESTRING (6.5390625 6.537109375, 6.5390625 0)");
  }
  
  public void testDirectedLines()
  {
    String wkt1 = "LINESTRING (1 6, 3 5, 1 4)";
    String wkt2 = "LINESTRING (1 10, 9 5, 1 2)";
    checkDistance(wkt1, wkt2, 0.01, "LINESTRING (1 6, 2.797752808988764 8.876404494382022)");
    checkDistance(wkt2, wkt1, 0.01, "LINESTRING (9 5, 3 5)");
  }

  public void testDirectedLines2()
  {
    String wkt1 = "LINESTRING (1 6, 3 5, 1 4)";
    String wkt2 = "LINESTRING (1 3, 1 9, 9 5, 1 1)";
    checkDistance(wkt1, wkt2, 0.01, "LINESTRING (3 5, 1 5)");
    checkDistance(wkt2, wkt1, 0.01, "LINESTRING (9 5, 3 5)");
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
    checkDistance(a, b, 1.0, 0.0);
  }
  
  //-----------------------------------------------------
  
  //-- shows withinDistance envelope check not triggering for disconnected A
  public void testFullyWithinDistancePoints()
  {
    String a = "MULTIPOINT ((1 9), (9 1))";
    String b = "MULTIPOINT ((1 1), (9 9))";
    checkFullyWithinDistance(a, b, 1, 0.01, false);
    checkFullyWithinDistance(a, b, 8.1, 0.01, true);
  }

  public void testFullyWithinDistanceDisconnectedLines()
  {
    String a = "MULTILINESTRING ((1 9, 2 9), (8 1, 9 1))";
    String b = "LINESTRING (9 9, 1 1)";
    checkFullyWithinDistance(a, b, 1, 0.01, false);
    checkFullyWithinDistance(a, b, 6, 0.01, true);
    checkFullyWithinDistance(b, a, 1, 0.01, false);
    checkFullyWithinDistance(b, a, 7.1, 0.01, true);
  }

  public void testFullyWithinDistanceDisconnectedPolygons()
  {
    String a = "MULTIPOLYGON (((1 9, 2 9, 2 8, 1 8, 1 9)), ((8 2, 9 2, 9 1, 8 1, 8 2)))";
    String b = "POLYGON ((1 2, 9 9, 2 1, 1 2))";
    checkFullyWithinDistance(a, b, 1, 0.01, false);
    checkFullyWithinDistance(a, b, 5.3, 0.01, true);
    checkFullyWithinDistance(b, a, 1, 0.01, false);
    checkFullyWithinDistance(b, a, 7.1, 0.01, true);
  }

  public void testFullyWithinDistanceLines()
  {
    String a = "MULTILINESTRING ((1 1, 3 3), (7 7, 9 9))";
    String b = "MULTILINESTRING ((1 9, 1 5), (6 4, 8 2))";
    checkFullyWithinDistance(a, b, 1, 0.01, false);
    checkFullyWithinDistance(a, b, 4, 0.01, false);
    checkFullyWithinDistance(a, b, 6, 0.01, true);
  }

  public void testFullyWithinDistancePolygons()
  {
    String a = "POLYGON ((1 4, 4 4, 4 1, 1 1, 1 4))";
    String b = "POLYGON ((10 10, 10 15, 15 15, 15 10, 10 10))";
    checkFullyWithinDistance(a, b, 5, 0.01, false);
    checkFullyWithinDistance(a, b, 10, 0.01, false);
    checkFullyWithinDistance(a, b, 20, 0.01, true);
  }

  public void testFullyWithinDistancePolygonsNestedWithHole()
  {
    String a = "POLYGON ((2 8, 8 8, 8 2, 2 2, 2 8))";
    String b = "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (3 7, 7 7, 7 3, 3 3, 3 7))";
    checkFullyWithinDistance(a, b, 1, 0.01, false);
    checkFullyWithinDistance(a, b, 2, 0.01, true);
    checkFullyWithinDistance(a, b, 3, 0.01, true);
  }

  //======================================================================
  
  private static final double TOLERANCE = 0.001;
  
  private void checkHD(String wkt1, String wkt2, double tolerance, String wktExpected) 
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Geometry result = DirectedHausdorffDistance.hausdorffDistanceLine(g1, g2, tolerance);
    Geometry expected = read(wktExpected);
    checkEqualExact(expected, result, TOLERANCE);
    
    /*
    double resultDistance = DiscreteHausdorffDistance.distance(g1, g2);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
    */
  }

  private void checkDistance(String wkt1, String wkt2, double tolerance, String wktExpected) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Geometry result = DirectedHausdorffDistance.distanceLine(g1, g2, tolerance);
    Geometry expected = read(wktExpected);
    checkEqualExact(expected, result, 100 * tolerance);
  }

  private void checkDistanceStartPtLen(String wkt1, String wkt2, double tolerance, 
      String wktExpected, double resultTolerance) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Geometry result = DirectedHausdorffDistance.distanceLine(g1, g2, tolerance);
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
    
    Geometry result = DirectedHausdorffDistance.distanceLine(g1, g2, tolerance);
    
    double distResult = result.getLength();
    assertEquals(expectedDistance, distResult, TOLERANCE);
  }

  
  private void checkFullyWithinDistance(String a, String b, double distance, double tolerance, boolean expected) {
    Geometry g1 = read(a);
    Geometry g2 = read(b);
    
    boolean result = DirectedHausdorffDistance.isFullyWithinDistance(g1, g2, distance, tolerance);
    
    assertEquals(expected, result);
  }

  
}
