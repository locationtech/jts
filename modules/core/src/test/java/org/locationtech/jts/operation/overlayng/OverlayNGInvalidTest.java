/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import junit.textui.TestRunner;

/**
 * Tests OverlayNG handling invalid geometry.
 * OverlayNG can handle "mildlt" invalid geometry.
 * 
 * @author mdavis
 *
 */
public class OverlayNGInvalidTest extends OverlayNGTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlayNGInvalidTest.class);
  }

  public OverlayNGInvalidTest(String name) { super(name); }
 
  public void testPolygonFlatIntersection() {
    checkIntersection(
        "POLYGON ((10 40, 40 40, 40 10, 10 10, 10 40))",
        "POLYGON ((50 30, 19 30, 50 30))",
        "LINESTRING (40 30, 19 30)");    
  }
  
  public void testPolygonAdjacentElementIntersection() {
    checkIntersection(
    "MULTIPOLYGON (((10 10, 10 40, 40 40, 40 10, 10 10)), ((70 10, 40 10, 40 40, 70 40, 70 10)))",
    "POLYGON ((20 50, 60 50, 60 20, 20 20, 20 50))",
    "POLYGON ((40 40, 60 40, 60 20, 40 20, 20 20, 20 40, 40 40))");
  }
  
  public void testPolygonInvertedIntersection() {
    checkIntersection(
    "POLYGON ((10 40, 70 40, 70 0, 40 0, 50 20, 30 20, 40 0, 10 0, 10 40))",
    "POLYGON ((20 50, 60 50, 60 10, 20 10, 20 50))",
    "POLYGON ((60 40, 60 10, 45 10, 50 20, 30 20, 35 10, 20 10, 20 40, 60 40))");   
  }
  
  // AKA self-touching polygon
  public void testPolygonExvertedIntersection() {
    checkIntersection(
    "POLYGON ((10 30, 70 30, 70 0, 40 30, 10 0, 10 30))",
    "POLYGON ((20 50, 60 50, 60 10, 20 10, 20 50))",
    "MULTIPOLYGON (((40 30, 20 10, 20 30, 40 30)), ((60 30, 60 10, 40 30, 60 30)))");   
  }
 
}
