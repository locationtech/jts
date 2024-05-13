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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class AdjacentEdgeLocatorTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(AdjacentEdgeLocatorTest.class);
  }
  
  public AdjacentEdgeLocatorTest(String name) {
    super(name);
  }

  public void testAdjacent2() {
    checkLocation(
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 5 9, 5 1, 1 1, 1 9)), POLYGON ((9 9, 9 1, 5 1, 5 9, 9 9)))",
        5, 5, Location.INTERIOR
        );
  }
  
  public void testNonAdjacent() {
    checkLocation(
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 4 9, 5 1, 1 1, 1 9)), POLYGON ((9 9, 9 1, 5 1, 5 9, 9 9)))",
        5, 5, Location.BOUNDARY
        );
  }

  public void testAdjacent6WithFilledHoles() {
    checkLocation(
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 5 9, 6 6, 1 5, 1 9), (2 6, 4 8, 6 6, 2 6)), POLYGON ((2 6, 4 8, 6 6, 2 6)), POLYGON ((9 9, 9 5, 6 6, 5 9, 9 9)), POLYGON ((9 1, 5 1, 6 6, 9 5, 9 1), (7 2, 6 6, 8 3, 7 2)), POLYGON ((7 2, 6 6, 8 3, 7 2)), POLYGON ((1 1, 1 5, 6 6, 5 1, 1 1)))",
        6, 6, Location.INTERIOR
        );
  }

  public void testAdjacent5WithEmptyHole() {
    checkLocation(
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 5 9, 6 6, 1 5, 1 9), (2 6, 4 8, 6 6, 2 6)), POLYGON ((2 6, 4 8, 6 6, 2 6)), POLYGON ((9 9, 9 5, 6 6, 5 9, 9 9)), POLYGON ((9 1, 5 1, 6 6, 9 5, 9 1), (7 2, 6 6, 8 3, 7 2)), POLYGON ((1 1, 1 5, 6 6, 5 1, 1 1)))",
        6, 6, Location.BOUNDARY
        );
  }

  public void testContainedAndAdjacent() {
    String wkt = "GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9)), POLYGON ((9 2, 2 2, 2 8, 9 8, 9 2)))";
    checkLocation(wkt,
        9, 5, Location.BOUNDARY
        );
    checkLocation(wkt,
        9, 8, Location.BOUNDARY
        );
  }

  /**
   * Tests a bug caused by incorrect point-on-segment logic.
   */
  public void testDisjointCollinear() {
    checkLocation(
        "GEOMETRYCOLLECTION (MULTIPOLYGON (((1 4, 4 4, 4 1, 1 1, 1 4)), ((5 4, 8 4, 8 1, 5 1, 5 4))))",
        2, 4, Location.BOUNDARY
        );
  }

  private void checkLocation(String wkt, int x, int y, int expectedLoc) {
    Geometry geom = read(wkt);
    AdjacentEdgeLocator ael = new AdjacentEdgeLocator(geom);
    int loc = ael.locate(new Coordinate(x, y));
    assertEquals("Locations are not equal: ", expectedLoc, loc);
  }
}
