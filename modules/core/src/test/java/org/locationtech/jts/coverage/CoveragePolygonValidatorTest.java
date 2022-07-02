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

import org.locationtech.jts.geom.Geometry;

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

  public void testDuplicateGeometry() {
    checkInvalid("POLYGON ((1 3, 3 3, 3 1, 1 1, 1 3))",
        "MULTIPOLYGON (((1 3, 3 3, 3 1, 1 1, 1 3)), ((5 3, 5 1, 3 1, 3 3, 5 3)))",
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

  public void testTargetFullyContained() {
    checkInvalid("POLYGON ((3 7, 7 7, 7 3, 3 3, 3 7))",
        "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        "LINESTRING (3 7, 7 7, 7 3, 3 3, 3 7)");
  }

  //========  Valid cases   =============================
  
  public void testMatchedEdges() {
    checkValid("POLYGON ((3 7, 7 7, 7 3, 3 3, 3 7))",
        "MULTIPOLYGON (((1 7, 3 7, 3 3, 1 3, 1 7)), ((3 9, 7 9, 7 7, 3 7, 3 9)), ((9 7, 9 3, 7 3, 7 7, 9 7)), ((3 1, 3 3, 7 3, 7 1, 3 1)))");
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
    checkResult(wktTarget, wktAdj, 0, wktExpected);
  }
  
  private void checkResult(String wktTarget, String wktAdj, double tolerance, String wktExpected) {
    Geometry target = read(wktTarget);
    Geometry adj = read(wktAdj);
    Geometry actual = CoveragePolygonValidator.validate(target, adj, tolerance);
    //System.out.println(actual);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkValid(String wktTarget, String wktAdj) {
    Geometry target = read(wktTarget);
    Geometry adj = read(wktAdj);
    Geometry actual = CoveragePolygonValidator.validate(target, adj);
    Geometry expected = read("LINESTRING EMPTY");    //TODO: check equals LINESTRING EMPTY
    checkEqual(expected, actual);
  }
}
