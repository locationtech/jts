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
package test.jts.perf.operation.valid;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import test.jts.geom.TestShapeFactory;
import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

/**
 * Tests performance of IsValidOp.checkHolesInShell
 * for polygons with a large shell and many holes.
 */
public class IsValidHolesInShellPerfTest extends PerformanceTestCase {

  static final int N_ITER = 10;

  static final int N_PTS_SMALL_SHELL = 1000;
  static final int N_PTS_LARGE_SHELL = 100_000;

  public static void main(String args[]) {
    PerformanceTestRunner.run(IsValidHolesInShellPerfTest.class);
  }

  public IsValidHolesInShellPerfTest(String name)
  {
    super(name);
    // number of holes; must be squares
    setRunSize(new int[] { 1, 4, 16, 64, 256, 1024, 4096 });
    setRunIterations(N_ITER);
  }

  Geometry smallShellGeom;
  Geometry largeShellGeom;

  @Override
  public void startRun(int nHoles)
  {
    System.out.println("\nRunning with # holes " + nHoles );
    smallShellGeom = createCircleWithHoles(N_PTS_SMALL_SHELL, nHoles);
    largeShellGeom = createCircleWithHoles(N_PTS_LARGE_SHELL, nHoles);
  }

  private Geometry createCircleWithHoles(int nPtsShell, int nHoles) {
    Coordinate origin = new Coordinate(0, 0);
    Polygon circle = TestShapeFactory.createCircle(origin, 100, nPtsShell);
    // holes are placed in the square inscribed in the circle
    Polygon square = (Polygon) TestShapeFactory.createSquareWithCircleHoles(origin, 70, nHoles, 8);
    LinearRing[] holes = new LinearRing[square.getNumInteriorRing()];
    for (int i = 0; i < holes.length; i++) {
      holes[i] = square.getInteriorRingN(i);
    }
    return circle.getFactory().createPolygon(circle.getExteriorRing(), holes);
  }

  public void runValidateSmallShell()
  {
    smallShellGeom.isValid();
  }

  public void runValidateLargeShell()
  {
    largeShellGeom.isValid();
  }
}
