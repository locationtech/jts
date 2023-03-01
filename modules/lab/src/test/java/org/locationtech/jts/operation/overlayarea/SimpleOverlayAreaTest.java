/*
 * Copyright (c) 2022 Martin Davis
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

import org.locationtech.jts.geom.Polygon;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class SimpleOverlayAreaTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(SimpleOverlayAreaTest.class);
  }
  
  public SimpleOverlayAreaTest(String name) {
    super(name);
  }

  public void testDisjoint() {
    checkIntersectionArea(
        "POLYGON ((10 90, 40 90, 40 60, 10 60, 10 90))",
        "POLYGON ((90 10, 50 10, 50 50, 90 50, 90 10))");
  }

  //TODO: fix this bug
  public void xtestTouching() {
    checkIntersectionArea(
        "POLYGON ((10 90, 50 90, 50 50, 10 50, 10 90))",
        "POLYGON ((90 10, 50 10, 50 50, 90 50, 90 10))");
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

  private void checkIntersectionArea(String wktA, String wktB) {
    Polygon a = (Polygon) read(wktA);
    Polygon b = (Polygon) read(wktB);
    
    double ovIntArea = SimpleOverlayArea.intersectionArea(a, b);
    
    double intAreaFull = a.intersection(b).getArea();
    
    //System.out.printf("OverlayArea: %f   Full overlay: %f\n", ovIntArea, intAreaFull);
    assertEquals(intAreaFull, ovIntArea, 0.0001);
  }
}
