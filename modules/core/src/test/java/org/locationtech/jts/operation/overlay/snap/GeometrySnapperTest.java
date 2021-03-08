/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlay.snap;

import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

public class GeometrySnapperTest extends GeometryTestCase {

  static final double SNAP_PRECISION_FACTOR = 1e-9;

  public GeometrySnapperTest(String name) {
    super(name);
  }

  public void testComputeOverlaySnapTolerance() {
    Geometry g1 = read("POLYGON((10 10, 10 20, 20 20, 20 10, 10 10))");
    assertEquals(10 * SNAP_PRECISION_FACTOR, GeometrySnapper.computeOverlaySnapTolerance(g1));

    g1 = read("MULTIPOLYGON(((10 10, 10 20, 20 20, 20 10, 10 10)), ((0 0, 0 5, 5 5, 5 0, 0 0)))");
    assertEquals(10 * SNAP_PRECISION_FACTOR, GeometrySnapper.computeOverlaySnapTolerance(g1));

    // test that the tolerance not increased because g1's envelope is now larger.
    g1 = read("MULTIPOLYGON(((100 100, 100 105, 105 105, 105 100, 100 100)),"
        + " ((210 210, 210 220, 220 220, 220 210, 210 210)),"
        + " ((10 10, 10 20, 20 20, 20 10, 10 10)))");
    assertEquals(10 * SNAP_PRECISION_FACTOR, GeometrySnapper.computeOverlaySnapTolerance(g1));
  }

}
