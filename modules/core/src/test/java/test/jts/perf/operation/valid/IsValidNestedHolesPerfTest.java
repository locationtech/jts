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

import test.jts.geom.TestShapeFactory;
import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

/**
 * Intended to test out an optimization introduced in GEOS
 * (https://github.com/libgeos/geos/pull/255/commits/1bf16cdf5a4827b483a1f712e0597ccb243f58cb)
 * 
 * This test doesn't show a clear benefit, so not changing the code at the moment (2020/03/11)
 * 
 * @author mdavis
 *
 */
public class IsValidNestedHolesPerfTest extends PerformanceTestCase {
  
  static final int N_ITER = 10;
  
  public static void main(String args[]) {
    PerformanceTestRunner.run(IsValidNestedHolesPerfTest.class);
  }
  
  public IsValidNestedHolesPerfTest(String name)
  {
    super(name);
    setRunSize(new int[] { 1000, 10_000, 100_000, 1000_000, 2000_000 });
    setRunIterations(N_ITER);
  }
  
  Geometry geom;
  
  public void startRun(int npts)
  {
    geom = createSlantHoles(npts);
  }

  static int NUM_GEOMS = 100;
  
  private Geometry createSlantHoles(int npts) {
    Geometry ellipses = TestShapeFactory.createSlantedEllipses(new Coordinate(0,0), 100, 10,
        NUM_GEOMS, npts);
    Geometry geom = TestShapeFactory.createExtentWithHoles(ellipses);
    System.out.println("\nRunning Slanted Ellipses: # geoms = " + NUM_GEOMS + ", # pts " + npts );
    //System.out.println(geom);
    return geom;
  }
 
  public void runValidate()
  {
    geom.isValid();
  }
}
