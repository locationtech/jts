/*
 * Copyright (c) 2022 Martin Davis.
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

import org.locationtech.jts.geom.IntersectionMatrix;

import junit.textui.TestRunner;

public class RelateNGTest extends RelateNGTestCase {

  public static void main(String args[]) {
    TestRunner.run(RelateNGTest.class);
  }
  
  public RelateNGTest(String name) {
    super(name);
  }

  public void testPointsDisjoint() {
    String a = "POINT (0 0)";
    String b = "POINT (1 1)";
    checkIntersectsDisjoint(a, b, false);
    checkContainsWithin(a, b, false);
    checkEquals(a, b, false);
    checkRelate(a, b, "FF0FFF0F2");
  }

  //======= P/P  =============
  
  public void testPointsContained() {
    String a = "MULTIPOINT (0 0, 1 1, 2 2)";
    String b = "MULTIPOINT (1 1, 2 2)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkEquals(a, b, false);
    checkRelate(a, b, "0F0FFFFF2");
  }
  
  public void testPointsEqual() {
    String a = "MULTIPOINT (0 0, 1 1, 2 2)";
    String b = "MULTIPOINT (0 0, 1 1, 2 2)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkEquals(a, b, true);
  }
  
  public void testValidateRelatePP_13() {
    String a = "MULTIPOINT ((80 70), (140 120), (20 20), (200 170))";
    String b = "MULTIPOINT ((80 70), (140 120), (80 170), (200 80))";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkContainsWithin(b, a, false);
    checkCoversCoveredBy(a, b, false);
    checkOverlaps(a, b, true);
    checkTouches(a, b, false);
  }
  
  //======= L/P  =============
  
  public void testLinePointContains() {
    String a = "LINESTRING (0 0, 1 1, 2 2)";
    String b = "MULTIPOINT (0 0, 1 1, 2 2)";
    checkRelate(a, b, "0F10FFFF2");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkContainsWithin(b, a, false);
    checkCoversCoveredBy(a, b, true);
    checkCoversCoveredBy(b, a, false);
  }
  
  public void testLinePointOverlaps() {
    String a = "LINESTRING (0 0, 1 1)";
    String b = "MULTIPOINT (0 0, 1 1, 2 2)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkContainsWithin(b, a, false);
    checkCoversCoveredBy(a, b, false);
    checkCoversCoveredBy(b, a, false);
  }
  
  public void testZeroLengthLinePoint() {
    String a = "LINESTRING (0 0, 0 0)";
    String b = "POINT (0 0)";
    checkRelate(a, b, "0FFFFFFF2");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkContainsWithin(b, a, true);
    checkCoversCoveredBy(a, b, true);
    checkCoversCoveredBy(b, a, true);
    checkEquals(a, b, true);
  }
  
  public void testZeroLengthLineLine() {
    String a = "LINESTRING (10 10, 10 10, 10 10)";
    String b = "LINESTRING (10 10, 10 10)";
    checkRelate(a, b, "0FFFFFFF2");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkContainsWithin(b, a, true);
    checkCoversCoveredBy(a, b, true);
    checkCoversCoveredBy(b, a, true);
    checkEquals(a, b, true);
  }
  
  // tests bug involving checking for non-zero-length lines
  public void testNonZeroLengthLinePoint() {
    String a = "LINESTRING (0 0, 0 0, 9 9)";
    String b = "POINT (1 1)";
    checkRelate(a, b, "0F1FF0FF2");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkContainsWithin(b, a, false);
    checkCoversCoveredBy(a, b, true);
    checkCoversCoveredBy(b, a, false);
    checkEquals(a, b, false);
  }
  
  public void testLinePointIntAndExt() {
    String a = "MULTIPOINT((60 60), (100 100))";
    String b = "LINESTRING(40 40, 80 80)";
    checkRelate(a, b, "0F0FFF102");
  }
  
  //======= L/L  =============
  
  public void testLinesCrossProper() {
    String a = "LINESTRING (0 0, 9 9)";
    String b = "LINESTRING(0 9, 9 0)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
  }

  public void testLinesOverlap() {
    String a = "LINESTRING (0 0, 5 5)";
    String b = "LINESTRING(3 3, 9 9)";
    checkIntersectsDisjoint(a, b, true);
    checkTouches(a, b, false);
    checkOverlaps(a, b, true);
  }

  public void testLinesCrossVertex() {
    String a = "LINESTRING (0 0, 8 8)";
    String b = "LINESTRING(0 8, 4 4, 8 0)";
    checkIntersectsDisjoint(a, b, true);
  }

  public void testLinesTouchVertex() {
    String a = "LINESTRING (0 0, 8 0)";
    String b = "LINESTRING(0 8, 4 0, 8 8)";
    checkIntersectsDisjoint(a, b, true);
  }

  public void testLinesDisjointByEnvelope() {
    String a = "LINESTRING (0 0, 9 9)";
    String b = "LINESTRING(10 19, 19 10)";
    checkIntersectsDisjoint(a, b, false);
    checkContainsWithin(a, b, false);
  }

  public void testLinesDisjoint() {
    String a = "LINESTRING (0 0, 9 9)";
    String b = "LINESTRING (4 2, 8 6)";
    checkIntersectsDisjoint(a, b, false);
    checkContainsWithin(a, b, false);
  }

  public void testLinesClosedEmpty() {
    String a = "MULTILINESTRING ((0 0, 0 1), (0 1, 1 1, 1 0, 0 0))";
    String b = "LINESTRING EMPTY";
    checkRelate(a, b, "FF1FFFFF2");
    checkIntersectsDisjoint(a, b, false);
    checkContainsWithin(a, b, false);
  }

  public void testLinesRingTouchAtNode() {
    String a = "LINESTRING (5 5, 1 8, 1 1, 5 5)";
    String b = "LINESTRING (5 5, 9 5)";
    checkRelate(a, b, "F01FFF102");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkTouches(a, b, true);
  }
  
  public void testLinesTouchAtBdy() {
    String a = "LINESTRING (5 5, 1 8)";
    String b = "LINESTRING (5 5, 9 5)";
    checkRelate(a, b, "FF1F00102");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkTouches(a, b, true);
  }
  
  public void testLinesOverlapWithDisjointLine() {
    String a = "LINESTRING (1 1, 9 9)";
    String b = "MULTILINESTRING ((2 2, 8 8), (6 2, 8 4))";
    checkRelate(a, b, "101FF0102");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkOverlaps(a, b, true);
  }
  
  public void testLinesDisjointOverlappingEnvelopes() {
    String a = "LINESTRING (60 0, 20 80, 100 80, 80 120, 40 140)";
    String b = "LINESTRING (60 40, 140 40, 140 160, 0 160)";
    checkRelate(a, b, "FF1FF0102");
    checkIntersectsDisjoint(a, b, false);
    checkContainsWithin(a, b, false);
    checkTouches(a, b, false);
  }
  
  /**
   * Case from https://github.com/locationtech/jts/issues/270
   * Strictly, the lines cross, since their interiors intersect
   * according to the Orientation predicate.
   * However, the computation of the intersection point is 
   * non-robust, and reports it as being equal to the endpoint 
   * POINT (-10 0.0000000000000012)
   * For consistency the relate algorithm uses the intersection node topology.
   */
  public void testLinesCross_JTS270() {
    String a = "LINESTRING (0 0, -10 0.0000000000000012)";
    String b = "LINESTRING (-9.999143275740073 -0.1308959557133398, -10 0.0000000000001054)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkCrosses(a, b, false);
    checkOverlaps(a, b, false);
    checkTouches(a, b, true);
  }

  public void testLinesContained_JTS396() {
    String a = "LINESTRING (1 0, 0 2, 0 0, 2 2)";
    String b = "LINESTRING (0 0, 2 2)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkCoversCoveredBy(a, b, true);
    checkCrosses(a, b, false);
    checkOverlaps(a, b, false);
    checkTouches(a, b, false);
  }
  
  
  /**
   * This case shows that lines must be self-noded, 
   * so that node topology is constructed correctly
   * (at least for some predicates).
   */
  public void testLinesContainedWithSelfIntersection() {
    String a = "LINESTRING (2 0, 0 2, 0 0, 2 2)";
    String b = "LINESTRING (0 0, 2 2)";
    //checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkCoversCoveredBy(a, b, true);
    checkCrosses(a, b, false);
    checkOverlaps(a, b, false);
    checkTouches(a, b, false);
  }
  
  public void testLineContainedInRing() {
    String a = "LINESTRING(60 60, 100 100, 140 60)";
    String b = "LINESTRING(100 100, 180 20, 20 20, 100 100)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(b, a, true);
    checkCoversCoveredBy(b, a, true);
    checkCrosses(a, b, false);
    checkOverlaps(a, b, false);
    checkTouches(a, b, false);
  }
  
  // see https://github.com/libgeos/geos/issues/933
  public void testLineLineProperIntersection() {
    String a = "MULTILINESTRING ((0 0, 1 1), (0.5 0.5, 1 0.1, -1 0.1))";
    String b = "LINESTRING (0 0, 1 1)";
    //checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkCoversCoveredBy(a, b, true);
    checkCrosses(a, b, false);
    checkOverlaps(a, b, false);
    checkTouches(a, b, false);
  }
  
  public void testLineSelfIntersectionCollinear() {
    String a = "LINESTRING (9 6, 1 6, 1 0, 5 6, 9 6)";
    String b = "LINESTRING (9 9, 3 1)";
    checkRelate(a, b, "0F1FFF102");
  }
  
  //======= A/P  =============
  
  public void testPolygonPointInside() {
    String a = "POLYGON ((0 10, 10 10, 10 0, 0 0, 0 10))";
    String b = "POINT (1 1)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
  }

  public void testPolygonPointOutside() {
    String a = "POLYGON ((10 0, 0 0, 0 10, 10 0))";
    String b = "POINT (8 8)";
    checkIntersectsDisjoint(a, b, false);
    checkContainsWithin(a, b, false);
  }

  public void testPolygonPointInBoundary() {
    String a = "POLYGON ((10 0, 0 0, 0 10, 10 0))";
    String b = "POINT (1 0)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, true);
  }

  public void testAreaPointInExterior() {
    String a = "POLYGON ((1 5, 5 5, 5 1, 1 1, 1 5))";
    String b = "POINT (7 7)";
    checkRelate(a, b, "FF2FF10F2");
    checkIntersectsDisjoint(a, b, false);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkTouches(a, b, false);
    checkOverlaps(a, b, false);
  }

  //======= A/L  =============
  

  public void testAreaLineContainedAtLineVertex() {
    String a = "POLYGON ((1 5, 5 5, 5 1, 1 1, 1 5))";
    String b = "LINESTRING (2 3, 3 5, 4 3)";
    checkIntersectsDisjoint(a, b, true);
    //checkContainsWithin(a, b, true);
    //checkCoversCoveredBy(a, b, true);
    checkTouches(a, b, false);
    checkOverlaps(a, b, false);
  }

  public void testAreaLineTouchAtLineVertex() {
    String a = "POLYGON ((1 5, 5 5, 5 1, 1 1, 1 5))";
    String b = "LINESTRING (1 8, 3 5, 5 8)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkTouches(a, b, true);
    checkOverlaps(a, b, false);
  }

  public void testPolygonLineInside() {
    String a = "POLYGON ((0 10, 10 10, 10 0, 0 0, 0 10))";
    String b = "LINESTRING (1 8, 3 5, 5 8)";
    checkRelate(a, b, "102FF1FF2");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
  }

  public void testPolygonLineOutside() {
    String a = "POLYGON ((10 0, 0 0, 0 10, 10 0))";
    String b = "LINESTRING (4 8, 9 3)";
    checkIntersectsDisjoint(a, b, false);
    checkContainsWithin(a, b, false);
  }

  public void testPolygonLineInBoundary() {
    String a = "POLYGON ((10 0, 0 0, 0 10, 10 0))";
    String b = "LINESTRING (1 0, 9 0)";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, true);
    checkTouches(a, b, true);
    checkOverlaps(a, b, false);
  }
  
  public void testPolygonLineCrossingContained() {
    String a = "MULTIPOLYGON (((20 80, 180 80, 100 0, 20 80)), ((20 160, 180 160, 100 80, 20 160)))";
    String b = "LINESTRING (100 140, 100 40)";
    checkRelate(a, b, "1020F1FF2");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkCoversCoveredBy(a, b, true);
    checkTouches(a, b, false);
    checkOverlaps(a, b, false);
  }
  
  public void testValidateRelateLA_220() {
    String a = "LINESTRING (90 210, 210 90)";
    String b = "POLYGON ((150 150, 410 150, 280 20, 20 20, 150 150))";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkTouches(a, b, false);
    checkOverlaps(a, b, false);
  }

  /**
   * See RelateLA.xml (line 585)
   */
  public void testLineCrossingPolygonAtShellHolePoint() {
    String a = "LINESTRING (60 160, 150 70)";
    String b = "POLYGON ((190 190, 360 20, 20 20, 190 190), (110 110, 250 100, 140 30, 110 110))";
    checkRelate(a, b, "F01FF0212");
    checkTouches(a, b, true);
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkTouches(a, b, true);
    checkOverlaps(a, b, false);
  }
  
  public void testLineCrossingPolygonAtNonVertex() {
    String a = "LINESTRING (20 60, 150 60)";
    String b = "POLYGON ((150 150, 410 150, 280 20, 20 20, 150 150))";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkTouches(a, b, false);
    checkOverlaps(a, b, false);
  }
  
  public void testPolygonLinesContainedCollinearEdge() {
    String a = "POLYGON ((110 110, 200 20, 20 20, 110 110))";
    String b = "MULTILINESTRING ((110 110, 60 40, 70 20, 150 20, 170 40), (180 30, 40 30, 110 80))";
    checkRelate(a, b, "102101FF2");
  }
  
  //======= A/A  =============

  
  public void testPolygonsEdgeAdjacent() {
    String a = "POLYGON ((1 3, 3 3, 3 1, 1 1, 1 3))";
    String b = "POLYGON ((5 3, 5 1, 3 1, 3 3, 5 3))";
    //checkIntersectsDisjoint(a, b, true);
    checkOverlaps(a, b, false);
    checkTouches(a, b, true);
    checkOverlaps(a, b, false);
  }

  public void testPolygonsEdgeAdjacent2() {
    String a = "POLYGON ((1 3, 4 3, 3 0, 1 1, 1 3))";
    String b = "POLYGON ((5 3, 5 1, 3 0, 4 3, 5 3))";
    //checkIntersectsDisjoint(a, b, true);
    checkOverlaps(a, b, false);
    checkTouches(a, b, true);
    checkOverlaps(a, b, false);
  }

  public void testPolygonsNested() {
    String a = "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))";
    String b = "POLYGON ((2 8, 8 8, 8 2, 2 2, 2 8))";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkCoversCoveredBy(a, b, true);
    checkOverlaps(a, b, false);
    checkTouches(a, b, false);
  }

  public void testPolygonsOverlapProper() {
    String a = "POLYGON ((1 1, 1 7, 7 7, 7 1, 1 1))";
    String b = "POLYGON ((2 8, 8 8, 8 2, 2 2, 2 8))";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkOverlaps(a, b, true);
    checkTouches(a, b, false);
  }
  
  public void testPolygonsOverlapAtNodes() {
    String a = "POLYGON ((1 5, 5 5, 5 1, 1 1, 1 5))";
    String b = "POLYGON ((7 3, 5 1, 3 3, 5 5, 7 3))";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkOverlaps(a, b, true);
    checkTouches(a, b, false);
  }

  public void testPolygonsContainedAtNodes() {
    String a = "POLYGON ((1 5, 5 5, 6 2, 1 1, 1 5))";
    String b = "POLYGON ((1 1, 5 5, 6 2, 1 1))";
    //checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, true);
    checkCoversCoveredBy(a, b, true);
    checkOverlaps(a, b, false);
    checkTouches(a, b, false);
  }
  
  public void testPolygonsNestedWithHole() {
    String a = "POLYGON ((40 60, 420 60, 420 320, 40 320, 40 60), (200 140, 160 220, 260 200, 200 140))";
    String b = "POLYGON ((80 100, 360 100, 360 280, 80 280, 80 100))";
    //checkIntersectsDisjoint(true, a, b);
    checkContainsWithin(a, b, false);
    checkContainsWithin(b, a, false);
    //checkCoversCoveredBy(false, a, b);
    //checkOverlaps(true, a, b);
    checkPredicate(RelatePredicate.contains(), a, b, false);
    //checkTouches(false, a, b);
  }

  public void testPolygonsOverlappingWithBoundaryInside() {
    String a = "POLYGON ((100 60, 140 100, 100 140, 60 100, 100 60))";
    String b = "MULTIPOLYGON (((80 40, 120 40, 120 80, 80 80, 80 40)), ((120 80, 160 80, 160 120, 120 120, 120 80)), ((80 120, 120 120, 120 160, 80 160, 80 120)), ((40 80, 80 80, 80 120, 40 120, 40 80)))";
    checkRelate(a, b, "21210F212");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkContainsWithin(b, a, false);
    checkCoversCoveredBy(a, b, false);
    checkOverlaps(a, b, true);
    checkTouches(a, b, false);
  }

  public void testPolygonsOverlapVeryNarrow() {
    String a = "POLYGON ((120 100, 120 200, 200 200, 200 100, 120 100))";
    String b = "POLYGON ((100 100, 100000 110, 100000 100, 100 100))";
    checkRelate(a, b, "212111212");
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkContainsWithin(b, a, false);
    //checkCoversCoveredBy(false, a, b);
    //checkOverlaps(true, a, b);
    //checkTouches(false, a, b);
  }
  
  public void testValidateRelateAA_86() {
    String a = "POLYGON ((170 120, 300 120, 250 70, 120 70, 170 120))";
    String b = "POLYGON ((150 150, 410 150, 280 20, 20 20, 150 150), (170 120, 330 120, 260 50, 100 50, 170 120))";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkOverlaps(a, b, false);
    checkPredicate(RelatePredicate.within(), a, b, false);
    checkTouches(a, b, true);
  }
  
  public void testValidateRelateAA_97() {
    String a = "POLYGON ((330 150, 200 110, 150 150, 280 190, 330 150))";
    String b = "MULTIPOLYGON (((140 110, 260 110, 170 20, 50 20, 140 110)), ((300 270, 420 270, 340 190, 220 190, 300 270)))";
    checkIntersectsDisjoint(a, b, true);
    checkContainsWithin(a, b, false);
    checkCoversCoveredBy(a, b, false);
    checkOverlaps(a, b, false);
    checkPredicate(RelatePredicate.within(), a, b, false);
    checkTouches(a, b, true);
  }

  public void testAdjacentPolygons() {
    String a = "POLYGON ((1 9, 6 9, 6 1, 1 1, 1 9))";
    String b = "POLYGON ((9 9, 9 4, 6 4, 6 9, 9 9))";
    checkRelateMatches(a, b, IntersectionMatrixPattern.ADJACENT, true);
  }

  public void testAdjacentPolygonsTouchingAtPoint() {
    String a = "POLYGON ((1 9, 6 9, 6 1, 1 1, 1 9))";
    String b = "POLYGON ((9 9, 9 4, 6 4, 7 9, 9 9))";
    checkRelateMatches(a, b, IntersectionMatrixPattern.ADJACENT, false);
  }

  public void testAdjacentPolygonsOverlappping() {
    String a = "POLYGON ((1 9, 6 9, 6 1, 1 1, 1 9))";
    String b = "POLYGON ((9 9, 9 4, 6 4, 5 9, 9 9))";
    checkRelateMatches(a, b, IntersectionMatrixPattern.ADJACENT, false);
  }

  public void testContainsProperlyPolygonContained() {
    String a = "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))";
    String b = "POLYGON ((2 8, 5 8, 5 5, 2 5, 2 8))";
    checkRelateMatches(a, b, IntersectionMatrixPattern.CONTAINS_PROPERLY, true);
  }
  
  public void testContainsProperlyPolygonTouching() {
    String a = "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))";
    String b = "POLYGON ((9 1, 5 1, 5 5, 9 5, 9 1))";
    checkRelateMatches(a, b, IntersectionMatrixPattern.CONTAINS_PROPERLY, false);
  }

  public void testContainsProperlyPolygonsOverlapping() {
    String a = "GEOMETRYCOLLECTION (POLYGON ((1 9, 6 9, 6 4, 1 4, 1 9)), POLYGON ((2 4, 6 7, 9 1, 2 4)))";
    String b = "POLYGON ((5 5, 6 5, 6 4, 5 4, 5 5))";
    checkRelateMatches(a, b, IntersectionMatrixPattern.CONTAINS_PROPERLY, true);
  }

  //================  Repeated Points  ==============
  
  public void testRepeatedPointLL() {
    String a = "LINESTRING(0 0, 5 5, 5 5, 5 5, 9 9)";
    String b = "LINESTRING(0 9, 5 5, 5 5, 5 5, 9 0)";
    checkRelate(a, b, "0F1FF0102");
    checkIntersectsDisjoint(a, b, true);
  }

  public void testRepeatedPointAA() {
    String a = "POLYGON ((1 9, 9 7, 9 1, 1 3, 1 9))";
    String b = "POLYGON ((1 3, 1 3, 1 3, 3 7, 9 7, 9 7, 1 3))";
    checkRelate(a, b, "212F01FF2");
  }
  
  //================  EMPTY geometries  ==============

  String empties[] = {
      "POINT EMPTY",
      "LINESTRING EMPTY",
      "POLYGON EMPTY",
      "MULTIPOINT EMPTY",
      "MULTILINESTRING EMPTY",
      "MULTIPOLYGON EMPTY",
      "GEOMETRYCOLLECTION EMPTY"
  };
  
  public void testEmptyEmpty() {
    for (int i = 0; i < empties.length; i++) {
      String a = empties[i];
      
      for (int j = 0; j < empties.length; j++) {
        String b = empties[j];
        checkRelate(a, b, "FFFFFFFF2");
        //-- empty geometries are all topologically equal
        checkEquals(a, b, true);
        
        checkIntersectsDisjoint(a, b, false);
        checkContainsWithin(a, b, false);
      }
    }  
  }
  
  public void testEmptyNonEmpty() {
    String nonEmptyPoint = "POINT (1 1)";
    String nonEmptyLine = "LINESTRING (1 1, 2 2)";
    String nonEmptyPolygon = "POLYGON ((1 1, 1 2, 2 1, 1 1))";
    
    for (int i = 0; i < empties.length; i++) {
      String empty = empties[i];
      
      checkRelate(empty, nonEmptyPoint, "FFFFFF0F2");
      checkRelate(nonEmptyPoint, empty, "FF0FFFFF2");
      
      checkRelate(empty, nonEmptyLine, "FFFFFF102");
      checkRelate(nonEmptyLine, empty, "FF1FF0FF2");
      
      checkRelate(empty, nonEmptyPolygon, "FFFFFF212");
      checkRelate(nonEmptyPolygon, empty, "FF2FF1FF2");
      
      checkEquals(empty, nonEmptyPoint, false);
      checkEquals(empty, nonEmptyLine, false);
      checkEquals(empty, nonEmptyPolygon, false);
      
      checkIntersectsDisjoint(empty, nonEmptyPoint, false);
      checkIntersectsDisjoint(empty, nonEmptyLine, false);
      checkIntersectsDisjoint(empty, nonEmptyPolygon, false);
      
      checkContainsWithin(empty, nonEmptyPoint, false);
      checkContainsWithin(empty, nonEmptyLine, false);
      checkContainsWithin(empty, nonEmptyPolygon, false);
      
      checkContainsWithin(nonEmptyPoint, empty, false);
      checkContainsWithin(nonEmptyLine, empty, false);
      checkContainsWithin(nonEmptyPolygon, empty, false);
    }  
  }
  
  //================  Prepared Relate  ==============
  
  public void testPreparedAA() {
    String a = "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))";
    String b = "POLYGON((0.5 0.5, 1.5 0.5, 1.5 1.5, 0.5 1.5, 0.5 0.5))";
    checkPrepared(a, b);
  }

  public void testPreparedPA() {
    String a = "POINT (5 5)";
    String b = "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))";
    checkPrepared(a, b);
    checkPrepared(b, a);
    
    //-- see https://github.com/libgeos/geos/issues/1275 (not a bug, but a good test to have)
    String pattern = "T*****FF*";
    String patternTrans = IntersectionMatrix.transpose(pattern);  // T*F**F***
    checkPreparedMatches(a, b, pattern);
    checkPreparedMatches(b, a, patternTrans); //
  }

}
