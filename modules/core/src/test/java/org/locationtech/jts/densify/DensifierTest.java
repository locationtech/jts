/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.densify;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class DensifierTest extends GeometryTestCase {
  private static final double TOLERANCE = 1e-6;

  public static void main(String args[]) {
    TestRunner.run(DensifierTest.class);
  }

  public DensifierTest(String name) { super(name); }
  
  public void testLine() {
    checkDensify("LINESTRING (0 0, 30 40, 35 35)", 
        10, "LINESTRING (0 0, 5 6.666666666666668, 10 13.333333333333336, 15 20, 20 26.66666666666667, 25 33.33333333333334, 30 40, 35 35)");
  }

  public void testBox() {
    checkDensify("POLYGON ((10 30, 30 30, 30 10, 10 10, 10 30))", 
        10, "POLYGON ((10 30, 16.666666666666668 30, 23.333333333333336 30, 30 30, 30 23.333333333333332, 30 16.666666666666664, 30 10, 23.333333333333332 10, 16.666666666666664 10, 10 10, 10 16.666666666666668, 10 23.333333333333336, 10 30))");
  }

  public void testBoxNoValidate() {
    checkDensifyNoValidate("POLYGON ((10 30, 30 30, 30 10, 10 10, 10 30))", 
        10, "POLYGON ((10 30, 16.666666666666668 30, 23.333333333333336 30, 30 30, 30 23.333333333333332, 30 16.666666666666664, 30 10, 23.333333333333332 10, 16.666666666666664 10, 10 10, 10 16.666666666666668, 10 23.333333333333336, 10 30))");
  }

  private void checkDensify(String wkt, double distanceTolerance, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry expected = read(wktExpected);
    Geometry actual = Densifier.densify(geom, distanceTolerance);
    checkEqual(expected, actual, TOLERANCE);
  }
  
  /**
   * Note: it's hard to construct a geometry which would actually be invalid when densified.
   * This test just checks that the code path executes.
   * 
   * @param wkt
   * @param distanceTolerance
   * @param wktExpected
   */
  private void checkDensifyNoValidate(String wkt, double distanceTolerance, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry expected = read(wktExpected);
    Densifier den = new Densifier(geom);
    den.setDistanceTolerance(distanceTolerance);
    den.setValidate(false);
    Geometry actual = den.getResultGeometry();
    checkEqual(expected, actual, TOLERANCE);
  }

}
