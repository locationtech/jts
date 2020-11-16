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

package test.jts.perf.operation.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.locationtech.jts.util.Stopwatch;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class DistanceGeomPairPerfTest extends PerformanceTestCase 
{

  static final int MAX_ITER = 100;

  public static void main(String args[]) {
    PerformanceTestRunner.run(DistanceGeomPairPerfTest.class);
  }

  boolean testFailed = false;
  boolean verbose = true;

  public DistanceGeomPairPerfTest(String name) {
    super(name);
    setRunSize(new int[] {10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10_000, 20_000, 50_000});
    setRunIterations(1000);
  }
  
  static final double SIZE = 100;
  static final double OFFSET = SIZE * 10;
  
  private Geometry geom1;
  private Geometry geom2;
  private Point pt2;
  
  public void startRun(int nPts)
  {
    //int nPts2 = nPts;
    int nPts2 = 100;
    
    System.out.println("\nRunning with " + nPts + " points (size-product = " + nPts * nPts2);
    
    geom1 = createSineStar(nPts, 0);
    geom2 = createSineStar(nPts2, OFFSET);
    
    pt2 = geom2.getCentroid();
  }
  
  public void runSimpleLines()
  {
    double dist = DistanceOp.distance(geom1, geom2);
  }

  public void runIndexedLines()
  {
    double dist = IndexedFacetDistance.distance(geom1, geom2);
  }

  
  public void runSimpleLinePoint()
  {
    double dist = DistanceOp.distance(geom1, pt2);
  }

  public void runIndexedLinePoint()
  {
    double dist = IndexedFacetDistance.distance(geom1, pt2);
  }

  public void runCachedLinePoint()
  {
    double dist = CachedFastDistance.getDistance(geom1, pt2);
  }

  
  Geometry createSineStar(int nPts, double offset)
  {
    SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(new Coordinate(0, 0));
    gsf.setSize(SIZE);
    gsf.setNumPoints(nPts);
    gsf.setCentre(new Coordinate(0, offset));

    Geometry g2 = gsf.createSineStar().getBoundary();
    
    return g2;
  }
}
  
  
