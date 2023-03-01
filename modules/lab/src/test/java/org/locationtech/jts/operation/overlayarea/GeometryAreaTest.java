/*
 * Copyright (c) 2020 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayarea;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class GeometryAreaTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(GeometryAreaTest.class);
  }
  
  public GeometryAreaTest(String name) {
    super(name);
  }

  public void testRectangle() {
    checkArea(
        "POLYGON ((100 300, 300 300, 300 100, 100 100, 100 300))");
  }

  public void testPolygon() {
    checkArea(
        "POLYGON ((40 110, 97 295, 176 184, 240 300, 440 100, 244 164, 194 74, 110 30, 130 140, 40 110))");
  }

  public void testPolygonWithHoles() {
    checkArea(
        "POLYGON ((40 110, 97 295, 176 184, 240 300, 440 100, 244 164, 194 74, 110 30, 130 140, 40 110), (230 230, 280 230, 280 190, 230 190, 230 230), (100 220, 90 160, 130 190, 100 220))");
  }

  public void testMultiPolygonWithHoles() {
    checkArea(
        "MULTIPOLYGON (((40 110, 97 295, 176 184, 240 300, 440 100, 244 164, 194 74, 110 30, 130 140, 40 110), (230 230, 280 230, 280 190, 230 190, 230 230), (100 220, 90 160, 130 190, 100 220)), ((120 350, 170 280, 223 355, 370 280, 415 399, 150 430, 120 350)))");
  }

  public void testLineString() {
    checkArea(
        "LINESTRING (120 120, 290 140, 130 240, 280 320)");
  }

  private void checkArea(String wkt) {
    Geometry geom = read(wkt);
    
    double ovArea = GeometryArea.area(geom);
    double area = geom.getArea();
    
    assertEquals(area, ovArea, 0.00001);
  }
}
