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
  
  public void testLineIntersectionInterpolated() {
    checkIntersection("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (10 0 0, 0 10 10)",
        "POINT(5 5 5)");
  }

  private void checkIntersection(String wktA, String wktB, String wktExpected) {
    Geometry a = read(wktA);
    Geometry b = read(wktB);
    Geometry result = OverlayNG.overlay(a, b, OverlayNG.INTERSECTION);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result);
  }
}
