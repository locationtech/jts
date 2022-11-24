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
package org.locationtech.jts.triangulate.polygon;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PolygonHoleJoinerTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(PolygonHoleJoinerTest.class);
  }
  
  public PolygonHoleJoinerTest(String name) {
    super(name);
  }
  
  /**
   * A failing case revealing that joining a hole by a zero-length cut
   * was introducing duplicate vertices.
   */
  public void testZeroLenCutDuplicateVertices() {
    checkJoin(
  "POLYGON ((70 10, 0 0, 7 47, 16 94, 70 60, 70 10), (10 40, 30 49, 7 47, 10 40), (13 59, 13 54, 26 53, 13 59))",
  "POLYGON ((0 0, 7 47, 10 40, 30 49, 7 47, 13 59, 13 54, 26 53, 13 59, 7 47, 16 94, 70 60, 70 10, 0 0))"
        );
  }
  
  /**
   * A failing case for hole joining with two touching holes.
   * Fails due to PolygonHoleJoiner joining holes with same leftmost vertex
   * so that the result linework self-crosses.
   * Note that input is normalized.
   */
  public void testTouchingHoles() {
    checkJoin(
  "POLYGON ((0 0, 0 9, 9 9, 9 0, 0 0), (1 4, 5 1, 5 4, 1 4), (1 4, 5 5, 6 8, 1 4))",
  null
        );
  }
  
  public void testHolesTouchEdgeVertical() {
    checkJoin(
  "POLYGON ((1 9, 9 9, 9 0, 1 0, 1 9), (1 4, 5 1, 5 4, 1 4), (1 5, 5 5, 6 8, 1 5))",
  "POLYGON ((1 9, 9 9, 9 0, 1 0, 1 4, 5 1, 5 4, 1 4, 1 5, 5 5, 6 8, 1 5, 1 9))"
        );
  }
  
  public void testHoleTouchesEdgeVertical() {
    checkJoin(
  "POLYGON ((1 9, 9 9, 9 0, 1 0, 1 9), (1 5, 5 5, 6 8, 1 5))",
  "POLYGON ((1 0, 1 5, 5 5, 6 8, 1 5, 1 9, 9 9, 9 0, 1 0))"
        );
  }
  
  public void testHoleTouchesEdgeWithCloserVertex() {
    checkJoin(
  "POLYGON ((1 9, 9 9, 5 6, 9 6, 19 1, 9 1, 1 9), (8 5, 9 3, 5 5, 8 5))",
  "POLYGON ((1 9, 9 9, 5 6, 9 6, 19 1, 9 1, 5 5, 9 3, 8 5, 5 5, 1 9))"
        );
  }
  
  public void testHoleTouchesEdge() {
    checkJoin(
  "POLYGON ((5 5, 9 5, 9 0, 0 0, 5 5), (3 3, 6 1, 5 3, 3 3))",
  "POLYGON ((5 5, 9 5, 9 0, 0 0, 3 3, 6 1, 5 3, 3 3, 5 5))"
        );
  }
  
  public void testHoleTouchesVertex() {
    checkJoin(
  "POLYGON ((70 10, 0 0, 7 47, 20 90, 70 60, 70 10), (10 40, 30 50, 7 47, 10 40))",
  "POLYGON ((70 10, 0 0, 7 47, 10 40, 30 50, 7 47, 20 90, 70 60, 70 10))"
        );
  }
  
  private void checkJoin(String wkt, String wktExpected) {
    Polygon geom = (Polygon) read(wkt);
    Geometry actual = PolygonHoleJoiner.joinAsPolygon(geom);
    if (wktExpected == null) {
      System.out.println("Actual: " + actual);
      return;
    }
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
}
