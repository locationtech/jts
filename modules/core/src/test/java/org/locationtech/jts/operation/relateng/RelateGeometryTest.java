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

import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class RelateGeometryTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(RelateGeometryTest.class);
  }
  
  public RelateGeometryTest(String name) {
    super(name);
  }

  public void testUniquePoints() {
    Geometry geom = read("MULTIPOINT ((0 0), (5 5), (5 0), (0 0))");
    RelateGeometry rgeom = new RelateGeometry(geom);
    Set<Coordinate> pts = rgeom.getUniquePoints();
    assertEquals("Unique pts size", 3, pts.size());
  }
  
  public void testBoundary() {
    Geometry geom = read("MULTILINESTRING ((0 0, 9 9), (9 9, 5 1))");
    RelateGeometry rgeom = new RelateGeometry(geom);
    assertTrue("hasBoundary", rgeom.hasBoundary());
  }
  
  public void testHasDimension() {
    Geometry geom = read("GEOMETRYCOLLECTION (POLYGON ((1 9, 5 9, 5 5, 1 5, 1 9)), LINESTRING (1 1, 5 4), POINT (6 5))");
    RelateGeometry rgeom = new RelateGeometry(geom);
    assertTrue("hasDimension 0", rgeom.hasDimension(0));
    assertTrue("hasDimension 1", rgeom.hasDimension(1));
    assertTrue("hasDimension 2", rgeom.hasDimension(2));
  }
  
  public void testDimension() {
    checkDimension("POINT (0 0)", 0, 0);
    checkDimension("LINESTRING (0 0, 0 0)", 1, 0);
    checkDimension("LINESTRING (0 0, 9 9)", 1, 1);
    checkDimension("POLYGON ((1 9, 5 9, 5 5, 1 5, 1 9))", 2, 2);
    checkDimension("GEOMETRYCOLLECTION (POLYGON ((1 9, 5 9, 5 5, 1 5, 1 9)), LINESTRING (1 1, 5 4), POINT (6 5))", 2, 2);
    checkDimension("GEOMETRYCOLLECTION (POLYGON EMPTY, LINESTRING (1 1, 5 4), POINT (6 5))", 2, 1);
  }

  private void checkDimension(String wkt, int expectedDim, int expectedDimReal) {
    Geometry geom = read(wkt);
    RelateGeometry rgeom = new RelateGeometry(geom);
    assertEquals(expectedDim, rgeom.getDimension());
    assertEquals(expectedDimReal, rgeom.getDimensionReal());
  }

}
