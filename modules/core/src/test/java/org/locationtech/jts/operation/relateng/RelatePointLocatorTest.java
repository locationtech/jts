/*
 * Copyright (c) 2023 Martin Davis.
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

public class RelatePointLocatorTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(RelatePointLocatorTest.class);
  }
  
  public RelatePointLocatorTest(String name) {
    super(name);
  }
  
  String gcPLA = "GEOMETRYCOLLECTION (POINT (1 1), POINT (2 1), LINESTRING (3 1, 3 9), LINESTRING (4 1, 5 4, 7 1, 4 1), LINESTRING (12 12, 14 14), POLYGON ((6 5, 6 9, 9 9, 9 5, 6 5)), POLYGON ((10 10, 10 16, 16 16, 16 10, 10 10)), POLYGON ((11 11, 11 17, 17 17, 17 11, 11 11)), POLYGON ((12 12, 12 16, 16 16, 16 12, 12 12)))";
  
  public void testPoint() {
    //String wkt = "GEOMETRYCOLLECTION (POINT(0 0), POINT(1 1))";
    checkLocation(gcPLA, 1, 1, DimensionLocation.POINT_INTERIOR);
    checkLocation(gcPLA, 0, 1, Location.EXTERIOR);
  }

  public void testPointInLine() {
    checkLocation(gcPLA, 3, 8, DimensionLocation.LINE_INTERIOR);
  }
  
  public void testPointInArea() {
    checkLocation(gcPLA, 8, 8, DimensionLocation.AREA_INTERIOR);
  }

  public void testLine() {
    checkLocation(gcPLA, 3, 3, DimensionLocation.LINE_INTERIOR);
    checkLocation(gcPLA, 3, 1, DimensionLocation.LINE_BOUNDARY);
  }

  public void testLineInArea() {
    checkLocation(gcPLA, 11, 11, DimensionLocation.AREA_INTERIOR);
    checkLocation(gcPLA, 14, 14, DimensionLocation.AREA_INTERIOR);
  }

  public void testArea() {
    checkLocation(gcPLA, 8, 8, DimensionLocation.AREA_INTERIOR);
    checkLocation(gcPLA, 9, 9, DimensionLocation.AREA_BOUNDARY);
  }

  public void testAreaInArea() {
    checkLocation(gcPLA, 11, 11, DimensionLocation.AREA_INTERIOR);
    checkLocation(gcPLA, 12, 12, DimensionLocation.AREA_INTERIOR);
    checkLocation(gcPLA, 10, 10, DimensionLocation.AREA_BOUNDARY);
    checkLocation(gcPLA, 16, 16, DimensionLocation.AREA_INTERIOR);
  }

  public void testLineNode() {
    //checkNodeLocation(gcPLA, 12.1, 12.2, Location.INTERIOR);
    checkNodeLocation(gcPLA, 3, 1, Location.BOUNDARY);
  }
  
  private void checkLocation(String wkt, double i, double j, int expected) {
    Geometry geom = read(wkt);
    RelatePointLocator locator = new RelatePointLocator(geom);
    int actual = locator.locateWithDim(new Coordinate(i, j));
    assertEquals(expected, actual);
  }
  
  private void checkNodeLocation(String wkt, double i, double j, int expected) {
    Geometry geom = read(wkt);
    RelatePointLocator locator = new RelatePointLocator(geom);
    int actual = locator.locateNode(new Coordinate(i, j), null);
    assertEquals(expected, actual);
  }
}
