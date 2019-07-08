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
package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.overlay.OverlayOp;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlaySRSingleTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlaySRSingleTest.class);
  }

  public OverlaySRSingleTest(String name) { super(name); }
  
  public void testBoxTriUnion() {
    Geometry a = read("POLYGON ((0 6, 4 6, 4 2, 0 2, 0 6))");
    Geometry b = read("POLYGON ((1 0, 2 5, 3 0, 1 0))");
    Geometry expected = read("POLYGON ((0 6, 4 6, 4 2, 3 2, 3 0, 1 0, 1 2, 0 2, 0 6))");
    Geometry actual = union(a, b, 1);
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
  
  
  public void XtestBoxLineIntersection() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    Geometry b = read("LINESTRING (50 150, 150 150)");
    Geometry expected = read("LINESTRING (100 150, 150 150)");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  
  
  
  
  public static Geometry union(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlaySR.overlay(a, b, pm, OverlayOp.UNION);
  }
  
  public static Geometry intersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlaySR.overlay(a, b, pm, OverlayOp.INTERSECTION);
  }
}
