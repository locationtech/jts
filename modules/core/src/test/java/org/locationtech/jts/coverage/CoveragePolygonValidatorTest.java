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
package org.locationtech.jts.coverage;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.PolygonExtracter;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class CoveragePolygonValidatorTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(CoveragePolygonValidatorTest.class);
  }
  
  public CoveragePolygonValidatorTest(String name) {
    super(name);
  }
  
  //========  Invalid cases   =============================

  public void testCollinearUnmatchedEdge() {
    checkInvalid("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((100 300, 180 300, 180 200, 100 200, 100 300))",
        "LINESTRING (100 200, 200 200)");
  }

  public void testDuplicate() {
    checkInvalid("POLYGON ((1 3, 3 3, 3 1, 1 1, 1 3))",
        "MULTIPOLYGON (((1 3, 3 3, 3 1, 1 1, 1 3)), ((5 3, 5 1, 3 1, 3 3, 5 3)))",
        "LINEARRING (1 3, 1 1, 3 1, 3 3, 1 3)");
  }

  public void testDuplicateReversed() {
    checkInvalid("POLYGON ((1 3, 3 3, 3 1, 1 1, 1 3))",
        "MULTIPOLYGON (((1 3, 1 1, 3 1, 3 3, 1 3))), ((5 3, 5 1, 3 1, 3 3, 5 3)))",
        "LINEARRING (1 3, 1 1, 3 1, 3 3, 1 3)");
  }

  public void testCrossingSegment() {
    checkInvalid("POLYGON ((1 9, 9 9, 9 3, 1 3, 1 9))",
        "POLYGON ((1 1, 5 6, 9 1, 1 1))",
        "LINESTRING (1 3, 9 3)");
  }

  public void testCrossingAndInteriorSegments() {
    checkInvalid("POLYGON ((1 1, 3 4, 7 4, 9 1, 1 1))",
        "POLYGON ((1 9, 9 9, 9 3, 1 3, 1 9))",
        "LINESTRING (1 1, 3 4, 7 4, 9 1)");
  }

  public void testTargetVertexTouchesSegment() {
    checkInvalid("POLYGON ((1 9, 9 9, 9 5, 1 5, 1 9))",
        "POLYGON ((1 1, 5 5, 9 1, 1 1))",
        "LINESTRING (9 5, 1 5)");
  }

  public void testAdjVertexTouchesSegment() {
    checkInvalid("POLYGON ((1 1, 5 5, 9 1, 1 1))",
        "POLYGON ((1 9, 9 9, 9 5, 1 5, 1 9))",
        "LINESTRING (1 1, 5 5, 9 1)");
  }

  public void testInteriorSegmentTouchingEdge() {
    checkInvalid("POLYGON ((4 3, 4 7, 8 9, 8 1, 4 3))",
        "POLYGON ((1 7, 6 7, 6 3, 1 3, 1 7))",
        "LINESTRING (8 1, 4 3, 4 7, 8 9)");
  }

  public void testInteriorSegmentTouchingNodes() {
    checkInvalid("POLYGON ((4 2, 4 8, 8 9, 8 1, 4 2))",
        "POLYGON ((1 5, 4 8, 7 5, 4 2, 1 5))",
        "LINESTRING (4 2, 4 8)");
  }

  public void testInteriorSegmentsTouching() {
    checkInvalid("POLYGON ((1 9, 5 9, 8 7, 5 7, 3 5, 8 2, 1 2, 1 9))",
        "POLYGON ((5 9, 9 9, 9 1, 5 1, 5 9))",
        "LINESTRING (5 9, 8 7, 5 7, 3 5, 8 2, 1 2)");
  }

  public void testTargetMultiPolygon() {
    checkInvalid("MULTIPOLYGON (((4 8, 9 9, 9 7, 4 8)), ((3 5, 9 6, 9 4, 3 5)), ((2 2, 9 3, 9 1, 2 2)))",
        "POLYGON ((1 1, 1 9, 5 9, 6 7, 5 5, 6 3, 5 1, 1 1))",
        "MULTILINESTRING ((9 7, 4 8, 9 9), (9 4, 3 5, 9 6), (9 1, 2 2, 9 3))");
  }

  public void testBothMultiPolygon() {
    checkInvalid("MULTIPOLYGON (((4 8, 9 9, 9 7, 4 8)), ((3 5, 9 6, 9 4, 3 5)), ((2 2, 9 3, 9 1, 2 2)))",
        "MULTIPOLYGON (((1 6, 1 9, 5 9, 6 7, 5 5, 1 6)), ((1 4, 5 5, 6 3, 5 1, 1 1, 1 4)))",
        "MULTILINESTRING ((9 7, 4 8, 9 9), (9 4, 3 5, 9 6), (9 1, 2 2, 9 3))");
  }

  /**
   * Shows need to evaluate both start and end point of intersecting segments
   * in InvalidSegmentDetector,
   * since matched segments are not tested
   */
  public void testInteriorSegmentsWithMatch() {
    checkInvalid("POLYGON ((7 6, 1 1, 3 6, 7 6))",
        "MULTIPOLYGON (((1 9, 9 9, 9 1, 1 1, 3 6, 1 9)), ((0 1, 0 9, 1 9, 3 6, 1 1, 0 1)))",
        "LINESTRING (7 6, 1 1, 3 6, 7 6)");
  }

  public void testAdjacentHoleOverlap() {
    checkInvalid("POLYGON ((3 3, 3 7, 6 8, 7 3, 3 3))",
        "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (3 7, 7 7, 7 3, 3 3, 3 7))",
        "LINESTRING (3 7, 6 8, 7 3)");
  }
  
  public void testTargetHoleOverlap() {
    checkInvalid("POLYGON ((1 1, 1 9, 9 9, 9 1, 1 1), (2 2, 8 2, 8 8, 5 4, 3 5, 2 5, 2 2))",
        "POLYGON ((2 2, 2 5, 3 5, 8 6.7, 8 2, 2 2))",
        "LINESTRING (8 2, 8 8, 5 4, 3 5)");
  }
  
  public void testFullyContained() {
    checkInvalid("POLYGON ((3 7, 7 7, 7 3, 3 3, 3 7))",
        "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        "LINESTRING (3 7, 7 7, 7 3, 3 3, 3 7)");
  }
  
  public void testFullyCoveredAndMatched() {
    checkInvalid("POLYGON ((1 3, 2 3, 2 2, 1 2, 1 3))",
        "MULTIPOLYGON (((1 1, 1 2, 2 2, 2 1, 1 1)), ((3 1, 2 1, 2 2, 3 2, 3 1)), ((3 3, 3 2, 2 2, 2 3, 3 3)), ((2 3, 3 3, 3 2, 3 1, 2 1, 1 1, 1 2, 1 3, 2 3)))",
        "LINESTRING (1 2, 1 3, 2 3)");
  }

  public void testTargetCoveredAndMatching() {
    checkInvalid("POLYGON ((1 7, 5 7, 9 7, 9 3, 5 3, 1 3, 1 7))",
        "MULTIPOLYGON (((5 9, 9 7, 5 7, 1 7, 5 9)), ((1 7, 5 7, 5 3, 1 3, 1 7)), ((9 3, 5 3, 5 7, 9 7, 9 3)), ((1 3, 5 3, 9 3, 5 1, 1 3)))",
        "LINESTRING (1 7, 5 7, 9 7, 9 3, 5 3, 1 3, 1 7))");
  }
  
  public void testCoveredBy2AndMatching() {
    checkInvalid("POLYGON ((1 9, 9 9, 9 5, 1 5, 1 9))",
        "MULTIPOLYGON (((1 5, 9 5, 9 1, 1 1, 1 5)), ((1 9, 5 9, 5 1, 1 1, 1 9)), ((9 9, 9 1, 5 1, 5 9, 9 9)))",
        "LINESTRING (1 5, 1 9, 9 9, 9 5)");
  }
  
  //========  Gap cases   =============================
  
  public void testGap() {
    checkInvalidGap("POLYGON ((1 5, 9 5, 9 1, 1 1, 1 5))",
        "POLYGON ((1 9, 5 9, 5 5.1, 1 5, 1 9))",
        0.5,
        "LINESTRING (1 5, 9 5)");
  }

  //========  Valid cases   =============================
  
  public void testMatchedEdges() {
    checkValid("POLYGON ((3 7, 7 7, 7 3, 3 3, 3 7))",
        "MULTIPOLYGON (((1 7, 3 7, 3 3, 1 3, 1 7)), ((3 9, 7 9, 7 7, 3 7, 3 9)), ((9 7, 9 3, 7 3, 7 7, 9 7)), ((3 1, 3 3, 7 3, 7 1, 3 1)))");
  }

  public void testRingsCCW() {
    checkValid("POLYGON ((1 1, 6 5, 4 9, 1 9, 1 1))",
        "POLYGON ((1 1, 9 1, 9 4, 6 5, 1 1))");
  }
  
  //-- confirms zero-length segments are skipped in processing
  public void testRepeatedCommonVertexInTarget() {
    checkValid("POLYGON ((1 1, 1 3, 5 3, 5 3, 9 1, 1 1))",
        "POLYGON ((1 9, 9 9, 9 5, 5 3, 1 3, 1 9))");
  }

  //-- confirms zero-length segments are skipped in processing
  public void testRepeatedCommonVertexInAdjacent() {
    checkValid("POLYGON ((1 1, 1 3, 5 3, 9 1, 1 1))",
        "POLYGON ((1 9, 9 9, 9 5, 5 3, 5 3, 1 3, 1 9))");
  }

  //----------------------------------------------------------------------
  
  private void checkInvalid(String wktTarget, String wktAdj, String wktExpected) {
    Geometry target = read(wktTarget);
    Geometry adj = read(wktAdj);
    Geometry[] adjPolygons = extractPolygons(adj);
    Geometry actual = CoveragePolygonValidator.validate(target, adjPolygons);
    //System.out.println(actual);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkInvalidGap(String wktTarget, String wktAdj, 
      double gapWidth, String wktExpected) {
    Geometry target = read(wktTarget);
    Geometry adj = read(wktAdj);
    Geometry[] adjPolygons = extractPolygons(adj);
    Geometry actual = CoveragePolygonValidator.validate(target, adjPolygons, gapWidth);
    //System.out.println(actual);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkValid(String wktTarget, String wktAdj) {
    Geometry target = read(wktTarget);
    Geometry adj = read(wktAdj);
    Geometry[] adjPolygons = extractPolygons(adj);
    Geometry actual = CoveragePolygonValidator.validate(target, adjPolygons);
    Geometry expected = read("LINESTRING EMPTY");    //TODO: check equals LINESTRING EMPTY
    checkEqual(expected, actual);
  }

  private Geometry[] extractPolygons(Geometry geom) {
    List<Polygon> polygons = PolygonExtracter.getPolygons(geom);
    return GeometryFactory.toPolygonArray(polygons);
  }
}
