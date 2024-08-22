/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import junit.textui.TestRunner;

public class RelateNGGCTest extends RelateNGTestCase {

  public static void main(String args[]) {
    TestRunner.run(RelateNGGCTest.class);
  }
  
  public RelateNGGCTest(String name) {
    super(name);
  }

  public void testDimensionWithEmpty() {
    String a = "LINESTRING(0 0, 1 1)";
    String b = "GEOMETRYCOLLECTION(POLYGON EMPTY,LINESTRING(0 0, 1 1))";
    checkCoversCoveredBy(a, b, true);
    checkEquals(a, b, true);
  }
  
  // see https://github.com/libgeos/geos/issues/1027
  public void testMP_GLP_GEOS1027() {
    String a = "MULTIPOLYGON (((0 0, 3 0, 3 3, 0 3, 0 0)))";
    String b = "GEOMETRYCOLLECTION ( LINESTRING (1 2, 1 1), POINT (0 0))";
    checkRelate(a, b, "1020F1FF2");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkCrosses(a, b, false);
    checkEquals(a, b, false);
  }
  
  // see https://github.com/libgeos/geos/issues/1022
  public void testGPL_A() {
    String a = "GEOMETRYCOLLECTION (POINT (7 1), LINESTRING (6 5, 6 4))";
    String b = "POLYGON ((7 1, 1 3, 3 9, 7 1))";
    checkRelate(a, b, "F01FF0212");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCrosses(a, b, false);
    checkTouches(a, b, true);
    checkEquals(a, b, false);
  }
  
  // see https://github.com/libgeos/geos/issues/982
  public void testP_GPL() {
    String a = "POINT(0 0)";
    String b = "GEOMETRYCOLLECTION(POINT(0 0), LINESTRING(0 0, 1 0))";
    checkRelate(a, b, "F0FFFF102");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCrosses(a, b, false);
    checkTouches(a, b, true);
    checkEquals(a, b, false);
  }
  
  public void testLineInOverlappingPolygonsTouchingInteriorEdge() {
    String a = "LINESTRING (3 7, 7 3)";
    String b = "GEOMETRYCOLLECTION (POLYGON ((1 9, 7 9, 7 3, 1 3, 1 9)), POLYGON ((9 1, 3 1, 3 7, 9 7, 9 1)))";
    checkRelate(a, b, "1FF0FF212");
    checkContainsWithin(b, a, true);
  }
  
  public void testLineInOverlappingPolygonsCrossingInteriorEdgeAtVertex() {
    String a = "LINESTRING (2 2, 8 8)";
    String b = "GEOMETRYCOLLECTION (POLYGON ((1 1, 1 7, 7 7, 7 1, 1 1)), POLYGON ((9 9, 9 3, 3 3, 3 9, 9 9)))";
    checkRelate(a, b, "1FF0FF212");
    checkContainsWithin(b, a, true);
  }
  
  public void testLineInOverlappingPolygonsCrossingInteriorEdgeProper() {
    String a = "LINESTRING (2 4, 6 8)";
    String b = "GEOMETRYCOLLECTION (POLYGON ((1 1, 1 7, 7 7, 7 1, 1 1)), POLYGON ((9 9, 9 3, 3 3, 3 9, 9 9)))";
    checkRelate(a, b, "1FF0FF212");
    checkContainsWithin(b, a, true);
  }
  
  public void testPolygonInOverlappingPolygonsTouchingBoundaries() {
    String a = "GEOMETRYCOLLECTION (POLYGON ((1 9, 6 9, 6 4, 1 4, 1 9)), POLYGON ((9 1, 4 1, 4 6, 9 6, 9 1)) )";
    String b = "POLYGON ((2 6, 6 2, 8 4, 4 8, 2 6))";
    checkRelate(a, b, "212F01FF2");
    checkContainsWithin(a, b, true);
  }
  
  public void testLineInOverlappingPolygonsBoundaries() {
    String a = "LINESTRING (1 6, 9 6, 9 1, 1 1, 1 6)";
    String b = "GEOMETRYCOLLECTION (POLYGON ((1 1, 1 6, 6 6, 6 1, 1 1)), POLYGON ((9 1, 4 1, 4 6, 9 6, 9 1)))";
    checkRelate(a, b, "F1FFFF2F2");
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkCoversCoveredBy(b, a, true);
  }
  
  public void testLineCoversOverlappingPolygonsBoundaries() {
    String a = "LINESTRING (1 6, 9 6, 9 1, 1 1, 1 6)";
    String b = "GEOMETRYCOLLECTION (POLYGON ((1 1, 1 6, 6 6, 6 1, 1 1)), POLYGON ((9 1, 4 1, 4 6, 9 6, 9 1)))";
    checkRelate(a, b, "F1FFFF2F2");
    checkContainsWithin(b, a, false);
    checkCoversCoveredBy(b, a, true);
  }
  
  public void testAdjacentPolygonsContainedInAdjacentPolygons() {
    String a = "GEOMETRYCOLLECTION (POLYGON ((2 2, 2 5, 4 5, 4 2, 2 2)), POLYGON ((8 2, 4 3, 4 4, 8 5, 8 2)))";
    String b = "GEOMETRYCOLLECTION (POLYGON ((1 1, 1 6, 4 6, 4 1, 1 1)), POLYGON ((9 1, 4 1, 4 6, 9 6, 9 1)))";
    checkRelate(a, b, "2FF1FF212");
    checkContainsWithin(b, a, true);
    checkCoversCoveredBy(b, a, true);
  }
  
  public void testGCMultiPolygonIntersectsPolygon() {
    String a = "POLYGON ((2 5, 3 5, 3 3, 2 3, 2 5))";
    String b = "GEOMETRYCOLLECTION (MULTIPOLYGON (((1 4, 4 4, 4 1, 1 1, 1 4)), ((5 4, 8 4, 8 1, 5 1, 5 4))))";
    checkRelate(a, b, "212101212");
    checkIntersectsDisjoint(a, b, true);
    checkCoversCoveredBy(b, a, false);
  }
  
  public void testPolygonContainsGCMultiPolygonElement() {
    String a = "POLYGON ((0 5, 4 5, 4 1, 0 1, 0 5))";
    String b = "GEOMETRYCOLLECTION (MULTIPOLYGON (((1 4, 3 4, 3 2, 1 2, 1 4)), ((6 4, 8 4, 8 2, 6 2, 6 4))))";
    checkRelate(a, b, "212FF1212");
    checkIntersectsDisjoint(a, b, true);
    checkCoversCoveredBy(b, a, false);
  }

  /**
   * Demonstrates the need for assigning computed nodes to their rings,
   * so that subsequent PIP testing can report node as being on ring boundary.
   */
  public void testPolygonOverlappingGCPolygon() {
    String a = "GEOMETRYCOLLECTION (POLYGON ((18.6 40.8, 16.8825 39.618567, 16.9319 39.5461, 17.10985 39.485133, 16.6143 38.4302, 16.43145 38.313267, 16.2 37.5, 14.8 37.8, 14.96475 40.474933, 18.6 40.8)))";
    String b = "POLYGON ((16.3649953125 38.37219358064516, 16.3649953125 39.545924774193544, 17.949465625000002 39.545924774193544, 17.949465625000002 38.37219358064516, 16.3649953125 38.37219358064516))";
    checkRelate(b, a, "212101212");
    checkRelate(a, b, "212101212");
    checkIntersectsDisjoint(a, b, true);
    checkCoversCoveredBy(a, b, false);
  }
  
  static final String wktAdjacentPolys = "GEOMETRYCOLLECTION (POLYGON ((5 5, 2 9, 9 9, 9 5, 5 5)), POLYGON ((3 1, 5 5, 9 5, 9 1, 3 1)), POLYGON ((1 9, 2 9, 5 5, 3 1, 1 1, 1 9)))";

  public void testAdjPolygonsCoverPolygonWithEndpointInside() {
    String a = wktAdjacentPolys;
    String b = "POLYGON ((3 7, 7 7, 7 3, 3 3, 3 7))";
    checkRelate(b, a, "2FF1FF212");
    checkRelate(a, b, "212FF1FF2");
    checkIntersectsDisjoint(a, b, true);
    checkCoversCoveredBy(a, b, true);
  }
  
  public void testAdjPolygonsCoverPointAtNode() {
    String a = wktAdjacentPolys;
    String b = "POINT (5 5)";
    checkRelate(b, a, "0FFFFF212");
    checkRelate(a, b, "0F2FF1FF2");
    checkIntersectsDisjoint(a, b, true);
    checkCoversCoveredBy(a, b, true);
  }
  
  public void testAdjPolygonsCoverPointOnEdge() {
    String a = wktAdjacentPolys;
    String b = "POINT (7 5)";
    checkRelate(b, a, "0FFFFF212");
    checkRelate(a, b, "0F2FF1FF2");
    checkIntersectsDisjoint(a, b, true);
    checkCoversCoveredBy(a, b, true);
  }
  
  public void testAdjPolygonsContainingPolygonTouchingInteriorEndpoint() {
    String a = wktAdjacentPolys;
    String b = "POLYGON ((5 5, 7 5, 7 3, 5 3, 5 5))";
    checkRelate(a, b, "212FF1FF2");
    checkIntersectsDisjoint(a, b, true);
    checkCoversCoveredBy(a, b, true);
  }
  
  public void testAdjPolygonsOverlappedByPolygonWithHole() {
    String a = wktAdjacentPolys;
    String b = "POLYGON ((0 10, 10 10, 10 0, 0 0, 0 10), (2 8, 8 8, 8 2, 2 2, 2 8))";
    checkRelate(a, b, "2121FF212");
    checkIntersectsDisjoint(a, b, true);
    checkCoversCoveredBy(a, b, false);
  }
  
  public void testAdjPolygonsContainingLine() {
    String a = wktAdjacentPolys;
    String b = "LINESTRING (5 5, 7 7)";
    checkRelate(a, b, "102FF1FF2");
    checkIntersectsDisjoint(a, b, true);
    checkCoversCoveredBy(a, b, true);
  }
  
  public void testAdjPolygonsContainingLineAndPoint() {
    String a = wktAdjacentPolys;
    String b = "GEOMETRYCOLLECTION (POINT (5 5), LINESTRING (5 7, 7 7))";
    checkRelate(a, b, "102FF1FF2");
    checkIntersectsDisjoint(a, b, true);
    checkCoversCoveredBy(a, b, true);
  }
  
  public void testEmptyMultiPointElements() {
    String a = "POLYGON ((3 7, 7 7, 7 3, 3 3, 3 7))";
    String b = "GEOMETRYCOLLECTION (MULTIPOINT (EMPTY, (5 5)), LINESTRING (1 9, 4 9))";
    checkIntersectsDisjoint(a, b, true);
  }
  
  public void testPolygonContainingPointsInBoundary() {
    String a = "POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))";
    String b = "GEOMETRYCOLLECTION (POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0)), MULTIPOINT ((0 2), (0 5)))";
    checkEquals(a, b, true);
  }
  
  public void testPolygonContainingLineInBoundary() {
    String a = "POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))";
    String b = "GEOMETRYCOLLECTION (POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0)), LINESTRING (0 2, 0 5))";
    checkEquals(a, b, true);
  }

  public void testPolygonContainingLineInBoundaryAndInterior() {
    String a = "POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))";
    String b = "GEOMETRYCOLLECTION (POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0)), LINESTRING (0 2, 0 5, 5 5))";
    checkEquals(a, b, true);
  }


}
