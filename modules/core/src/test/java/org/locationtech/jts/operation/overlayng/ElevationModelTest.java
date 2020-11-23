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

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class ElevationModelTest extends GeometryTestCase {
  
  private static final double TOLERANCE = 0.00001;

  public static void main(String args[]) {
    TestRunner.run(ElevationModelTest.class);
  }

  public ElevationModelTest(String name) {
    super(name);
  }
  
  public void testLine() {
    checkElevation("LINESTRING Z (0 0 0, 10 10 10)",
        0, 0, 0,
        5, 5, 5,
        10, 10, 10);
  }

  private void checkElevation(String wkt1, double... ords) {
    ElevationModel model = ElevationModel.create(read(wkt1), null);
    int numPts = ords.length / 3;
    if (3 * numPts != ords.length) {
      throw new IllegalArgumentException("Incorrect number of ordinates");
    }
    for (int i = 0; i < numPts; i++) {
      double x = ords[3*i];
      double y = ords[3*i + 1];
      double expectedZ = ords[3*i + 2];
      double actualZ = model.getZ(x, y);
      assertEquals(expectedZ, actualZ, TOLERANCE);
    }
  }
}
