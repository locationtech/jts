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

public class OverlayAreaTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlayAreaTest.class);
  }
  
  public OverlayAreaTest(String name) {
    super(name);
  }

  public void testRectangleAContainsB() {
    checkIntersectionArea(
        "POLYGON ((100 300, 300 300, 300 100, 100 100, 100 300))",
        "POLYGON ((150 250, 250 250, 250 150, 150 150, 150 250))");
  }

  public void testTriangleAContainsB() {
    checkIntersectionArea(
        "POLYGON ((60 170, 270 370, 380 60, 60 170))",
        "POLYGON ((200 250, 245 155, 291 195, 200 250))");
  }

  public void testRectangleOverlap() {
    checkIntersectionArea(
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((250 250, 250 150, 150 150, 150 250, 250 250))");
  }

  public void testRectangleTriangleOverlap() {
    checkIntersectionArea(
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((300 200, 150 150, 300 100, 300 200))");
  }

  public void testSawOverlap() {
    checkIntersectionArea(
        "POLYGON ((100 300, 305 299, 150 200, 300 150, 150 100, 300 50, 100 50, 100 300))",
        "POLYGON ((400 350, 150 250, 350 200, 200 150, 350 100, 180 50, 400 50, 400 350))");
  }

  public void testAOverlapBWithHole() {
    checkIntersectionArea(
        "POLYGON ((100 300, 305 299, 150 200, 300 150, 150 100, 300 50, 100 50, 100 300))",
        "POLYGON ((185 206, 350 206, 350 100, 185 100, 185 206), (230 190, 310 190, 310 120, 230 120, 230 190))");
  }

  public void testAOverlapBMulti() {
    checkIntersectionArea(
        "POLYGON ((50 250, 250 250, 250 50, 50 50, 50 250))",
        "MULTIPOLYGON (((100 200, 100 100, 0 100, 0 200, 100 200)), ((200 200, 300 200, 300 100, 200 100, 200 200)))");
  }

  public void testAOverlapBMultiHole() {
    checkIntersectionArea(
        "POLYGON ((60 200, 250 280, 111 135, 320 120, 50 40, 30 120, 60 200))",
        "MULTIPOLYGON (((55 266, 150 150, 170 290, 55 266)), ((100 0, 70 130, 260 160, 291 45, 100 0), (150 40, 125 98, 220 110, 150 40)))");
  }

  private void checkIntersectionArea(String wktA, String wktB) {
    Geometry a = read(wktA);
    Geometry b = read(wktB);
    
    OverlayArea ova = new OverlayArea(a);
    double ovIntArea = ova.intersectionArea(b);
    
    double intAreaFull = a.intersection(b).getArea();
    
    //System.out.printf("OverlayArea: %f   Full overlay: %f\n", ovIntArea, intAreaFull);
    assertEquals(intAreaFull, ovIntArea, 0.0001);
  }
}
