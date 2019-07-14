/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlayng.OverlayNG;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlayNGSingleTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlayNGSingleTest.class);
  }

  public OverlayNGSingleTest(String name) { super(name); }
  
  public void xtestBoxGoreIntersection() {
    Geometry a = read("MULTIPOLYGON (((1 1, 5 1, 5 0, 1 0, 1 1)), ((1 1, 5 2, 5 4, 1 4, 1 1)))");
    Geometry b = read("POLYGON ((1 0, 1 2, 2 2, 2 0, 1 0))");
    Geometry expected = read("POLYGON ((2 0, 1 0, 1 1, 1 2, 2 2, 2 1, 2 0))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestBoxGoreUnion() {
    Geometry a = read("MULTIPOLYGON (((1 1, 5 1, 5 0, 1 0, 1 1)), ((1 1, 5 2, 5 4, 1 4, 1 1)))");
    Geometry b = read("POLYGON ((1 0, 1 2, 2 2, 2 0, 1 0))");
    Geometry expected = read("POLYGON ((2 0, 1 0, 1 1, 1 2, 1 4, 5 4, 5 2, 2 1, 5 1, 5 0, 2 0))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestCollapseBoxGoreIntersection() {
    Geometry a = read("MULTIPOLYGON (((1 1, 5 1, 5 0, 1 0, 1 1)), ((1 1, 5 2, 5 4, 1 4, 1 1)))");
    Geometry b = read("POLYGON ((1 0, 1 2, 2 2, 2 0, 1 0))");
    Geometry expected = read("POLYGON ((2 0, 1 0, 1 1, 1 2, 2 2, 2 1, 2 0))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestCollapseTriBoxIntersection() {
    Geometry a = read("POLYGON ((1 2, 1 1, 9 1, 1 2))");
    Geometry b = read("POLYGON ((9 2, 9 1, 8 1, 8 2, 9 2))");
    Geometry expected = read("LINESTRING (8 1, 9 1)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void XtestCollapseTriBoxUnion() {
    Geometry a = read("POLYGON ((1 2, 1 1, 9 1, 1 2))");
    Geometry b = read("POLYGON ((9 2, 9 1, 8 1, 8 2, 9 2))");
    Geometry expected = read("MULTIPOLYGON (((1 1, 1 2, 8 1, 1 1)), ((8 1, 8 2, 9 2, 9 1, 8 1)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void XtestAdjacentBoxes() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("POLYGON ((300 200, 300 100, 200 100, 200 200, 300 200))");
    Geometry expected = read("POLYGON ((100 100, 100 200, 200 200, 300 200, 300 100, 200 100, 100 100))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestBoxTriIntersection() {
    Geometry a = read("POLYGON ((0 6, 4 6, 4 2, 0 2, 0 6))");
    Geometry b = read("POLYGON ((1 0, 2 5, 3 0, 1 0))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  

  public void xtestBoxTriUnion() {
    Geometry a = read("POLYGON ((0 6, 4 6, 4 2, 0 2, 0 6))");
    Geometry b = read("POLYGON ((1 0, 2 5, 3 0, 1 0))");
    Geometry expected = read("POLYGON ((0 6, 4 6, 4 2, 3 2, 3 0, 1 0, 1 2, 0 2, 0 6))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestMultiHoleBoxUnion() {
    Geometry a = read("MULTIPOLYGON (((0 200, 200 200, 200 0, 0 0, 0 200), (50 50, 190 50, 50 200, 50 50), (20 20, 20 50, 50 50, 50 20, 20 20)), ((60 100, 50 50, 100 60, 60 100)))");
    Geometry b = read("POLYGON ((60 110, 100 110, 100 60, 60 60, 60 110))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestNestedPolysUnion() {
    Geometry a = read("MULTIPOLYGON (((0 200, 200 200, 200 0, 0 0, 0 200), (50 50, 190 50, 50 200, 50 50)), ((60 100, 100 60, 50 50, 60 100)))");
    Geometry b = read("POLYGON ((135 176, 180 176, 180 130, 135 130, 135 176))");
    Geometry expected = read("MULTIPOLYGON (((0 0, 0 200, 50 200, 200 200, 200 0, 0 0), (50 50, 190 50, 50 200, 50 50)), ((50 50, 60 100, 100 60, 50 50)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  // TODO: check this when it has be implemented...
  public void xtestMultiHoleSideTouchingBoxUnion() {
    Geometry a = read("MULTIPOLYGON (((0 200, 200 200, 200 0, 0 0, 0 200), (50 50, 190 50, 50 200, 50 50), (20 20, 20 50, 50 50, 50 20, 20 20)))");
    Geometry b = read("POLYGON ((100 100, 100 50, 50 50, 50 100, 100 100))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void XtestBoxTriIntersection() {
    Geometry a = read("POLYGON ((0 6, 4 6, 4 2, 0 2, 0 6))");
    Geometry b = read("POLYGON ((1 0, 2 5, 3 0, 1 0))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void XtestNestedShellsIntersection() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("POLYGON ((120 180, 180 180, 180 120, 120 120, 120 180))");
    Geometry expected = read("POLYGON ((120 180, 180 180, 180 120, 120 120, 120 180))");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void xtestBoxLineIntersection() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("LINESTRING (50 150, 150 150)");
    Geometry expected = read("LINESTRING (100 150, 150 150)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  public void xtestBoxLineUnion() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("LINESTRING (50 150, 150 150)");
    Geometry expected = read("GEOMETRYCOLLECTION (LINESTRING (50 150, 100 150), POLYGON ((100 200, 200 200, 200 100, 100 100, 100 150, 100 200)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void testAdjacentBoxesIntersection() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("POLYGON ((300 200, 300 100, 200 100, 200 200, 300 200))");
    Geometry expected = read("LINESTRING (200 100, 200 200)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  
  
  
  public static Geometry union(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, pm, OverlayOp.UNION);
  }
  
  public static Geometry intersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, pm, OverlayOp.INTERSECTION);
  }
}
