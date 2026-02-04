/*
 * Copyright (c) 2026 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.operation.distance;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.distance.DiscreteHausdorffDistance;
import org.locationtech.jts.algorithm.distance.DirectedHausdorffDistance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.util.Debug;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class HausdorffDistancePerfTest extends PerformanceTestCase {

  static final int MAX_ITER = 10000;
  
  static final boolean VERBOSE = false;

  static GeometryFactory geomFact = new GeometryFactory();
  
  public static void main(String[] args) {
    PerformanceTestRunner.run(HausdorffDistancePerfTest.class);
  }

  private Geometry b;

  private List<Geometry> grid;

  private int distance;

  private DirectedHausdorffDistance dhd;

  public HausdorffDistancePerfTest(String name) {
    super(name);
    setRunSize(new int[] { 100, 10_000, 40_000, 160_000, 1_000_000 });
    setRunIterations(1);
  }

  public void startRun(int npts)
  {
    int nCircles = (int) Math.sqrt(npts);
    System.out.println("\n-------  Running with # circles = " + nCircles * nCircles);
    b = createSineStar(1000, 300, 50);
    dhd = new DirectedHausdorffDistance(b);
    grid = createCircleGrid(2000, nCircles);
    distance = 2000 / nCircles;
  }
  
  public void runFullyWithin() {
    Debug.println(b);
    
    int iter = 0;
    for (Geometry g : grid) {
      iter++;
      Debug.println(iter + "  ----------------");
      Debug.println(g);
      //checkHausdorff(g, b);
      //boolean isWithin = DirectedHausdorffDistance.isFullyWithinDistance(g, b, 4 * distance, 0.1);
      boolean isWithin = dhd.isFullyWithinDistance(g, 4 * distance, 0.1);
      
      //if (iter > 10) break;
    }
  }
  
  private void checkHausdorff(Geometry g, Geometry b) {
    double distDHD = DiscreteHausdorffDistance.distance(g, b, 0.01);
    
    double distHD = DirectedHausdorffDistance.hausdorffDistanceLine(g, b, 0.01).getLength();
    //-- performance testing only
    //double distDHD = distHD;
    
    double err = Math.abs(distDHD - distHD) / (distDHD + distHD);
    
    Debug.println(distDHD + "   " + distHD + "   err = " + err);
    if (err > .01) {
      System.out.println("<<<<<<<<<<<  ERROR!");
    }
    
    checkFullyWithinDistance(g, b);
  }

  private void checkFullyWithinDistance(Geometry g, Geometry b) {
    double distDHD = DirectedHausdorffDistance.distanceLine(g, b, 0.01).getLength();
    double tol = distDHD / 1000;
    boolean isWithin = DirectedHausdorffDistance.isFullyWithinDistance(g, b, 1.05 * distDHD, tol);
    boolean isBeyond = ! DirectedHausdorffDistance.isFullyWithinDistance(g, b, 0.95 * distDHD, tol);
    if (! (isWithin && isBeyond)) {
      System.out.format("ioWithin = %b   isBeyond = %b\n", isWithin, isBeyond);
      System.out.println("<<<<<<<<<<<  ERROR!");
      DirectedHausdorffDistance.isFullyWithinDistance(g, b, 0.75 * distDHD,tol);
    }
  }

  private List<Geometry> createCircleGrid(double size, int nSide) {
    List<Geometry> geoms = new ArrayList<Geometry>();
    
    double inc = size / nSide;
    
    for (int i = 0; i < nSide; i++) {
      for (int j = 0; j < nSide; j++) {
        Coordinate p = new Coordinate(i * inc, j * inc);
        Point pt = geomFact.createPoint(p);
        Geometry buf = pt.buffer(inc);
        geoms.add(buf);
      }
    }
    return geoms;
  }

  private Geometry createSineStar(double loc, double size, int nPts)
  {
    SineStarFactory gsf = new SineStarFactory(geomFact);
    gsf.setCentre(new Coordinate(loc, loc));
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    
    Geometry g = gsf.createSineStar().getBoundary();

    return g;
  }
}
