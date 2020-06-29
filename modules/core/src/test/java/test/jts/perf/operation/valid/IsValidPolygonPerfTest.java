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
 * Used to test performance enhancement in IsValidOp.checkHolesInShell.
 * 
 * @author mdavis
 *
 */
public class IsValidPolygonPerfTest extends PerformanceTestCase {
  
  
  static final int N_ITER = 10;
  
  public static void main(String args[]) {
    PerformanceTestRunner.run(IsValidPolygonPerfTest.class);
  }
  
  public IsValidPolygonPerfTest(String name)
  {
    super(name);
    setRunSize(new int[] { 1000, 10_000, 100_000, 1000_000, 2000_000 });
    setRunIterations(N_ITER);
  }
  
  Geometry geom;
  
  public void startRun(int npts)
  {
    geom = createSineStar(npts);
  }
 
  private Geometry createSineStar(int npts) {
    Geometry sineStar = TestShapeFactory.createSineStar(new Coordinate(0,0), 100, npts);
    System.out.println("\nRunning with # pts " + sineStar.getNumPoints() );
    return sineStar;
  }
 
  
  public void runValidate()
  {
    geom.isValid();
  }
}
