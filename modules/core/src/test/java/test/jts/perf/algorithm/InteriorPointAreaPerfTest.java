/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.algorithm;

import org.locationtech.jts.algorithm.InteriorPointArea;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.precision.GeometryPrecisionReducer;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

/**
 * An example of the usage of the {@link PerformanceTestRunner}.
 * 
 * @author Martin Davis
 *
 */
public class InteriorPointAreaPerfTest
extends PerformanceTestCase
{

  private static final int N_ITER = 100;

  static double ORG_X = 100;
  static double ORG_Y = 100;
  static double SIZE = 100;
  static int N_ARMS = 20;
  static double ARM_RATIO = 0.3;
  
  public static void main(String args[]) {
    PerformanceTestRunner.run(InteriorPointAreaPerfTest.class);
  }

  private Geometry sineStar;
  private Geometry sinePolyCrinkly;


  public InteriorPointAreaPerfTest(String name)
  {
    super(name);
    setRunSize(new int[] { 10, 100, 1000, 10000, 100000, 1000000 });
    setRunIterations(N_ITER);
  }

  public void setUp()
  {
    System.out.println("Interior Point Area perf test");
    System.out.println("SineStar: origin: ("
        + ORG_X + ", " + ORG_Y + ")  size: " + SIZE
        + "  # arms: " + N_ARMS + "  arm ratio: " + ARM_RATIO);   
    System.out.println("# Iterations: " + N_ITER);
  }
  
  public void startRun(int npts)
  {
    iter = 0;
    sineStar = SineStarFactory.create(new Coordinate(ORG_X, ORG_Y), SIZE, npts, N_ARMS, ARM_RATIO);
    
    double scale = npts / SIZE;
    PrecisionModel pm = new PrecisionModel(scale);
    
    sinePolyCrinkly = GeometryPrecisionReducer.reduce(sineStar, pm);

    System.out.println("\nRunning with # pts " + sinePolyCrinkly.getNumPoints() );
    //if (size <= 1000) System.out.println(sineStar);
  }
  
  private int iter = 0;
  
  public void runTest1()
  {
    InteriorPointArea.getInteriorPoint(sinePolyCrinkly);
  }

}
