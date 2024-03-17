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

import org.locationtech.jts.geom.Geometry;
import test.jts.GeometryTestCase;

/**
 * Base class for testing overlay area algorithms
 * by comparing them to the standard overlay intersection area.
 * This is a parameterized test class.
 * Subclasses provide the overlay area algorithm to test.
 */
public abstract class BaseOverlayAreaTest extends GeometryTestCase {

  public BaseOverlayAreaTest(String name) {
    super(name);
  }

  public void testDisjoint() {
    checkIntersectionAreaSymmetric(
        "POLYGON ((10 90, 40 90, 40 60, 10 60, 10 90))",
        "POLYGON ((90 10, 50 10, 50 50, 90 50, 90 10))");
  }

  public void testTouching() {
    checkIntersectionAreaSymmetric(
        "POLYGON ((10 90, 50 90, 50 50, 10 50, 10 90))",
        "POLYGON ((90 10, 50 10, 50 50, 90 50, 90 10))");
  }

  public void testRectangleAContainsB() {
    checkIntersectionAreaSymmetric(
        "POLYGON ((100 300, 300 300, 300 100, 100 100, 100 300))",
        "POLYGON ((150 250, 250 250, 250 150, 150 150, 150 250))");
  }

  public void testTriangleAContainsB() {
    checkIntersectionAreaSymmetric(
        "POLYGON ((60 170, 270 370, 380 60, 60 170))",
        "POLYGON ((200 250, 245 155, 291 195, 200 250))");
  }

  public void testRectangleOverlap() {
    checkIntersectionAreaSymmetric(
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((250 250, 250 150, 150 150, 150 250, 250 250))");
  }

  public void testRectangleTriangleOverlap() {
    checkIntersectionAreaSymmetric(
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((300 200, 150 150, 300 100, 300 200))");
  }

  public void testSawOverlap() {
    checkIntersectionAreaSymmetric(
        "POLYGON ((100 300, 305 299, 150 200, 300 150, 150 100, 300 50, 100 50, 100 300))",
        "POLYGON ((400 350, 150 250, 350 200, 200 150, 350 100, 180 50, 400 50, 400 350))");
  }

  protected final void checkIntersectionArea(String wktA, String wktB) {
    Geometry a = read(wktA);
    Geometry b = read(wktB);
    
    double ovIntArea = computeOverlayArea(a, b);
    
    double intAreaFull = a.intersection(b).getArea();
    
    //System.out.printf("OverlayArea: %f   Full overlay: %f\n", ovIntArea, intAreaFull);
    assertEquals(intAreaFull, ovIntArea, 0.0001);
  }

  protected final void checkIntersectionAreaSymmetric(String wktA, String wktB) {
    checkIntersectionArea(wktA, wktB);
    checkIntersectionArea(wktB, wktA);
  }

  abstract protected double computeOverlayArea(Geometry a, Geometry b);
}