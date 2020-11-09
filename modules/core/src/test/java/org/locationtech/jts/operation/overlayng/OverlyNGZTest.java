/*
 * Copyright (c) 2020 Martin Davis.
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

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlyNGZTest extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(OverlyNGZTest.class);
  }

  public OverlyNGZTest(String name) {
    super(name);
  }
  
  public void testLineIntersectionPointInterpolated() {
    checkIntersection("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (10 0 0, 0 10 10)",
        "POINT(5 5 5)");
  }

  public void testLineIntersectionPoint() {
    checkIntersection("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (10 0 0, 5 5 999, 0 10 10)",
        "POINT(5 5 999)");
  }

  public void testLineDifferenceLineInterpolated() {
    checkDifference("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (5 5, 6 6)",
        "MULTILINESTRING ((0 0 0, 5 5 5), (6 6 6, 10 10 10))");
  }

  //=================================================
  
  
  private void checkIntersection(String wktA, String wktB, String wktExpected) {
    checkOverlay(OverlayNG.INTERSECTION, wktA, wktB, wktExpected);
  }
  private void checkDifference(String wktA, String wktB, String wktExpected) {
    checkOverlay(OverlayNG.DIFFERENCE, wktA, wktB, wktExpected);
  }
  
  private void checkOverlay(int opCode, String wktA, String wktB, String wktExpected) {
    Geometry a = read(wktA);
    Geometry b = read(wktB);
    Geometry result = OverlayNG.overlay(a, b, opCode);
    Geometry expected = read(wktExpected);
    checkEqualXYZ(expected, result);
  }
}
