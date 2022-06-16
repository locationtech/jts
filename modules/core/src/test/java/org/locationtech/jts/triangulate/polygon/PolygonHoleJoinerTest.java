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
   * A failing case revealing that joining holes by a zero-length cut
   * was introducing duplicate vertices.
   */
  public void testZeroLenCutDuplicateVertices() {
    checkJoin(
  "POLYGON ((71 12, 0 0, 7 47, 16 94, 71 52, 71 12), (7 38, 25 48, 7 47, 7 38), (13 59, 13 54, 26 53, 13 59))",
  null
        );
  }
  
  /**
   * A failing case for hole joining with two touching holes.
   * Fails due to PolygonHoleJoiner not handling holes which have same leftmost vertex.
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
  
  public void testHoleTouchesEdge() {
    checkJoin(
  "POLYGON ((5 5, 9 5, 9 0, 0 0, 5 5), (3 3, 6 1, 5 3, 3 3))",
  "POLYGON ((5 5, 9 5, 9 0, 0 0, 3 3, 6 1, 5 3, 3 3, 5 5))"
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
