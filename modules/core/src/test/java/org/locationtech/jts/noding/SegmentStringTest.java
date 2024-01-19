/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.noding;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

public class SegmentStringTest  extends GeometryTestCase {
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(SegmentStringTest.class);
  }  
  
  public SegmentStringTest(String name) {
    super(name);
  }

  public void testNextInRing() {
    SegmentString ss = create("LINESTRING(0 0, 1 2, 3 1, 0 0)");
    assertTrue(ss.isClosed());
    checkEqualXY(ss.nextInRing(0), new Coordinate(1, 2));
    checkEqualXY(ss.nextInRing(1), new Coordinate(3, 1));
    checkEqualXY(ss.nextInRing(2), new Coordinate(0, 0));
    checkEqualXY(ss.nextInRing(3), new Coordinate(1, 2));
  }

  public void testPrevInRing() {
    SegmentString ss = create("LINESTRING(0 0, 1 2, 3 1, 0 0)");
    assertTrue(ss.isClosed());
    checkEqualXY(ss.prevInRing(0), new Coordinate(3, 1));
    checkEqualXY(ss.prevInRing(1), new Coordinate(0, 0));
    checkEqualXY(ss.prevInRing(2), new Coordinate(1, 2));
    checkEqualXY(ss.prevInRing(3), new Coordinate(3, 1));
  }

  private SegmentString create(String wkt) {
    Geometry geom = read(wkt);
    return new BasicSegmentString(geom.getCoordinates(), null);
  }

}
