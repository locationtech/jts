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
package org.locationtech.jts.geom;

import test.jts.GeometryTestCase;

public class GeometryCoordinateTest extends GeometryTestCase {

  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(GeometryCoordinateTest.class);
  }
  
  public GeometryCoordinateTest(String name) {
    super(name);
  }
  
  public void testPoint() {
    checkCoordinate( "POINT (1 1)", 1, 1);
  }  
  
  public void testLineString() {
    checkCoordinate( "LINESTRING (1 1, 2 2)", 1, 1);
  }  
  
  public void testPolygon() {
    checkCoordinate( "POLYGON ((1 1, 1 2, 2 1, 1 1))", 1, 1);
  }  
  
  public void testEmptyElementsAll() {
    checkCoordinate( "GEOMETRYCOLLECTION ( LINESTRING EMPTY, POINT EMPTY )");
  }

  public void testEmptyFirstElementPolygonal() {
    checkCoordinate( "MULTIPOLYGON ( EMPTY, ((1 1, 1 2, 2 1, 1 1)) )", 1, 1);
  }
  
  public void testEmptyFirstElement() {
    checkCoordinate( "GEOMETRYCOLLECTION ( LINESTRING EMPTY, POINT(1 1) )", 1, 1);
  }
  
  public void testEmptySecondElement() {
    checkCoordinate( "GEOMETRYCOLLECTION ( POINT(1 1), LINESTRING EMPTY )", 1, 1);
  }

  private void checkCoordinate(String wkt, int x, int y) {
    checkCoordinate(read(wkt), new Coordinate(x, y));
  }
  
  private void checkCoordinate(final Geometry g, Coordinate expected) {
    Coordinate actual = g.getCoordinate();
    checkEqualXY( expected, actual );
  }
  
  private void checkCoordinate(String wkt) {
    Geometry g = read(wkt);
    Coordinate actual = g.getCoordinate();
    assertNull(actual);   
  }
}
