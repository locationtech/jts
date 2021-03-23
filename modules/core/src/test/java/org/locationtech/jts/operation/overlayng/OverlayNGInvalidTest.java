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

import static org.locationtech.jts.operation.overlayng.OverlayNG.DIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.SYMDIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests OverlayNG handling invalid geometry.
 * OverlayNG can handle "mildlt" invalid geometry.
 * 
 * @author mdavis
 *
 */
public class OverlayNGInvalidTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlayNGInvalidTest.class);
  }

  public OverlayNGInvalidTest(String name) { super(name); }
 
  public void testPolygonFlatIntersection() {
    Geometry a = read("POLYGON ((10 40, 40 40, 40 10, 10 10, 10 40))");
    Geometry b = read("POLYGON ((50 30, 19 30, 50 30))");
    Geometry expected = read("LINESTRING (40 30, 19 30)");
    checkEqualExact(expected, intersection(a, b));    
  }
  
  public void testPolygonAdjacentElementIntersection() {
    Geometry a = read("MULTIPOLYGON (((10 10, 10 40, 40 40, 40 10, 10 10)), ((70 10, 40 10, 40 40, 70 40, 70 10)))");
    Geometry b = read("POLYGON ((20 50, 60 50, 60 20, 20 20, 20 50))");
    Geometry expected = read("POLYGON ((40 40, 60 40, 60 20, 40 20, 20 20, 20 40, 40 40))");
    checkEqualExact(expected, intersection(a, b));    
  }
  
  public void testPolygonInvertedIntersection() {
    Geometry a = read("POLYGON ((10 40, 70 40, 70 0, 40 0, 50 20, 30 20, 40 0, 10 0, 10 40))");
    Geometry b = read("POLYGON ((20 50, 60 50, 60 10, 20 10, 20 50))");
    Geometry expected = read("POLYGON ((60 40, 60 10, 45 10, 50 20, 30 20, 35 10, 20 10, 20 40, 60 40))");
    checkEqualExact(expected, intersection(a, b));    
  }
  
  // AKA self-touching polygon
  public void testPolygonExvertedIntersection() {
    Geometry a = read("POLYGON ((10 30, 70 30, 70 0, 40 30, 10 0, 10 30))");
    Geometry b = read("POLYGON ((20 50, 60 50, 60 10, 20 10, 20 50))");
    Geometry expected = read("MULTIPOLYGON (((40 30, 20 10, 20 30, 40 30)), ((60 30, 60 10, 40 30, 60 30)))");
    checkEqualExact(expected, intersection(a, b));    
  }
  
  //============================================================
  
  
  public static Geometry difference(Geometry a, Geometry b) {
    PrecisionModel pm = new PrecisionModel();
    return OverlayNG.overlay(a, b, DIFFERENCE, pm);
  }
  
  public static Geometry symDifference(Geometry a, Geometry b) {
    PrecisionModel pm = new PrecisionModel();
    return OverlayNG.overlay(a, b, SYMDIFFERENCE, pm);
  }
  
  public static Geometry intersection(Geometry a, Geometry b) {
    PrecisionModel pm = new PrecisionModel();
    return OverlayNG.overlay(a, b, INTERSECTION, pm);
  }
  
  public static Geometry union(Geometry a, Geometry b) {
    PrecisionModel pm = new PrecisionModel();
    return OverlayNG.overlay(a, b, UNION, pm);
  }
  
}
