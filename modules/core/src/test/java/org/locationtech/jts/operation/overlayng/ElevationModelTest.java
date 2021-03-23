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

public class ElevationModelTest extends GeometryTestCase {
  
  private static final double TOLERANCE = 0.00001;

  public static void main(String args[]) {
    TestRunner.run(ElevationModelTest.class);
  }

  public ElevationModelTest(String name) {
    super(name);
  }
  
  public void testBox() {
    checkElevation("POLYGON Z ((1 6 50, 9 6 60, 9 4 50, 1 4 40, 1 6 50))",
        0,10, 50,     5,10,  50,    10,10, 60,
        0,5,  50,     5,5, 50,      10,5, 50,
        0,4,  40,     5,4, 50,      10,4, 50,
        0,0,  40,     5,0, 50,      10,0, 50
        );
  }

  public void testLine() {
    checkElevation("LINESTRING Z (0 0 0, 10 10 10)",
     -1,11, 5,                            11,11,  10,
        0,10, 5,    5,10,  5,   10,10,  10,
        0,5, 5,     5,5, 5,     10,5,   5,
        0,0, 0,     5,0, 5,     10,0,   5,
     -1,-1, 0,      5,-1,  5,   11,-1,  5
        );
  }

  public void testPopulateZLine() {
    checkElevationPopulateZ("LINESTRING Z (0 0 0, 10 10 10)",
        "LINESTRING (1 1, 9 9)",
        "LINESTRING (1 1 0, 9 9 10)"
        );
  }

  public void testPopulateZBox() {
    checkElevationPopulateZ("LINESTRING Z (0 0 0, 10 10 10)",
        "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        "POLYGON Z ((1 1 0, 1 9 5, 9 9 10, 9 1 5, 1 1 0))"
        );
  }

  public void testMultiLine() {
    checkElevation("MULTILINESTRING Z ((0 0 0, 10 10 8), (1 2 2, 9 8 6))",
     -1,11, 4,                            11,11,  7,
        0,10, 4,    5,10, 4,    10,10,  7,
        0,5, 4,     5,5,  4,    10,5,   4,
        0,0, 1,     5,0,  4,    10,0,   4,
     -1,-1, 1,      5,-1, 4,    11,-1,  4
        );
  }

  public void testTwoLines() {
    checkElevation( "LINESTRING Z (0 0 0, 10 10 8)",
                    "LINESTRING Z (1 2 2, 9 8 6))",
     -1,11, 4,                            11,11,  7,
        0,10, 4,    5,10, 4,    10,10,  7,
        0,5, 4,     5,5,  4,    10,5,   4,
        0,0, 1,     5,0,  4,    10,0,   4,
     -1,-1, 1,      5,-1, 4,    11,-1,  4
        );
  }

  /**
   * Tests that XY geometries are scanned correctly (avoiding reading Z)
   * and that they produce a model Z value of NaN
   */
  public void testLine2D() {
    // LINESTRING (0 0, 10 10)
    checkElevation( "0102000000020000000000000000000000000000000000000000000000000024400000000000002440",
                    5, 5, Double.NaN
        );
  }
  
  public void testLineHorizontal() {
    checkElevation("LINESTRING Z (0 5 0, 10 5 10)",
        0,10, 0,    5,10,  5,     10,10,  10,
        0,5,  0,    5,5,   5,     10,5,   10,
        0,0,  0,    5,0,   5,     10,0,   10
        );
  }

  public void testLineVertical() {
    checkElevation("LINESTRING Z (5 0 0, 5 10 10)",
        0,10, 10,    5,10, 10,    10,10, 10,
        0,5,  5,     5,5,  5,     10,5,  5,
        0,0,  0,     5,0,  0,     10,0,  0
        );
  }

  // tests that single point Z is used for entire grid and beyond
  public void testPoint() {
    checkElevation("POINT Z (5 5 5)",
        0,9, 5,     5,9,  5,    9,9, 5,
        0,5, 5,     5,5,  5,    9,5, 5,
        0,0, 5,     5,0,  5,    9,0, 5
        );
  }

  // tests that Z is average of input points with same location
  public void testMultiPointSame() {
    checkElevation("MULTIPOINT Z ((5 5 5), (5 5 9))",
        0,9, 7,     5,9,  7,     9,9, 7,
        0,5, 7,     5,5,  7,     9,5, 7,
        0,0, 7,     5,0,  7,     9,0, 7
        );
  }

  private void checkElevation(String wkt1, String wkt2, double... ords) {
    checkElevation(read(wkt1), read(wkt2), ords);
  }
  
  private void checkElevation(String wkt1, double... ords) {
    checkElevation(read(wkt1), null, ords);
  }
  
  
  private void checkElevation(Geometry geom1, Geometry geom2, double[] ords) {
    ElevationModel model = ElevationModel.create(geom1, geom2);
    int numPts = ords.length / 3;
    if (3 * numPts != ords.length) {
      throw new IllegalArgumentException("Incorrect number of ordinates");
    }
    for (int i = 0; i < numPts; i++) {
      double x = ords[3*i];
      double y = ords[3*i + 1];
      double expectedZ = ords[3*i + 2];
      double actualZ = model.getZ(x, y);
      String msg = "Point ( "  + x + ", " + y + " ) : ";
      assertEquals(msg, expectedZ, actualZ, TOLERANCE);
    }
  }
  
  private void checkElevationPopulateZ(String wkt, String wktNoZ, String wktZExpected) {
    Geometry geom = read(wkt);
    ElevationModel model = ElevationModel.create(geom, null);
    
    Geometry geomNoZ = read(wktNoZ);
    model.populateZ(geomNoZ);
    
    Geometry geomZExpected = read(wktZExpected);
    checkEqualXYZ(geomZExpected, geomNoZ);
  }
}
