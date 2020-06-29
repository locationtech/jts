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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class LengthTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(LengthTest.class);
  }

  public LengthTest(String name) { super(name); }

  public void testArea() {
    checkLengthOfLine("LINESTRING (100 200, 200 200, 200 100, 100 100, 100 200)", 400.0);
  }
  
  void checkLengthOfLine(String wkt, double expectedLen) {
    LineString ring = (LineString) read(wkt);

    CoordinateSequence pts = ring.getCoordinateSequence();
    double actual = Length.ofLine(pts);
    assertEquals(actual, expectedLen);
  }
}
