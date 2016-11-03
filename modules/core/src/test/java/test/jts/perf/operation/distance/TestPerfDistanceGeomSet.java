/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.operation.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.util.Stopwatch;

public class TestPerfDistanceGeomSet 
{
  static final int MAX_ITER = 1;
  static final int NUM_GEOM = 100;
  static final double GEOM_SIZE = 1;
  static final double MAX_X = 100;

  public static void main(String[] args) {
    TestPerfDistanceGeomSet test = new TestPerfDistanceGeomSet();
//    test.test();
    test.test();
  }

  boolean testFailed = false;
  boolean verbose = false;

  public TestPerfDistanceGeomSet() {
  }

  public void test()
  {
    
    
//    test(5000);
//    test(8001);

    test(10);
    test(3);
    test(4);
    test(5);
    test(10);
    test(20);
    test(30);
    test(40);
    test(50);
    test(60);
    test(100);
    test(200);
    test(500);
    test(1000);
    test(5000);
    test(10000);
    test(50000);
    test(100000);
  }

  public void test2()
  {
    verbose = false;
    
    for (int i = 800; i <= 2000; i += 100) {
      test(i);
    }
  }
  
  double size = 100;
  double separationDist = size * 2;
  
  public void test(int num)
  {
    
//    Geometry[] geom = createRandomCircles(nPts);
    Geometry[] geom = createRandomCircles(100, 5, num);
//    Geometry[] geom = createSineStarsRandomLocation(nPts);
    
    if (verbose) System.out.println("Running with " + num + " points");
    if (! verbose) System.out.print(num + ", ");
    test(geom);
  }
  
  public void test(Geometry[] geom)
  {
    Stopwatch sw = new Stopwatch();
    double dist = 0.0;
    for (int i = 0; i < MAX_ITER; i++) {
      testAll(geom);
    }
    if (! verbose) System.out.println(sw.getTimeString());
    if (verbose) {
      System.out.println("Finished in " + sw.getTimeString());
      System.out.println("       (Distance = " + dist + ")");
    }
  }

  void testAll(Geometry[] geom)
  {
    for (int i = 0; i < geom.length; i++ ) {
      for (int j = 0; j < geom.length; j++ ) {
       double dist = geom[i].distance(geom[j]);
//      double dist = SortedBoundsFacetDistance.distance(g1, g2);
//      double dist = BranchAndBoundFacetDistance.distance(geom[i], geom[j]);
//      double dist = CachedBABDistance.getDistance(geom[i], geom[j]);
        
      }
    }
  }
  
  Geometry[] createRandomCircles(int nPts)
  {
    Geometry[] geoms = new Geometry[NUM_GEOM];
    for (int i = 0; i < NUM_GEOM; i++) {
      geoms[i] = createCircleRandomLocation(nPts);
    }
    return geoms;
  }
    
  Geometry[] createRandomCircles(int numGeom, int nPtsMin, int nPtsMax)
  {
    int nPtsRange = nPtsMax - nPtsMin + 1;
    Geometry[] geoms = new Geometry[numGeom];
    for (int i = 0; i < numGeom; i++) {
      int nPts = (int) (nPtsRange * Math.random()) + nPtsMin;
      geoms[i] = createCircleRandomLocation(nPts);
    }
    return geoms;
  }
    
  Geometry createCircleRandomLocation(int nPts)  
  {
    SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(randomLocation());
    gsf.setSize(GEOM_SIZE);
    gsf.setNumPoints(nPts);
    
    Polygon g = gsf.createCircle();
//    Geometry g = gsf.createSineStar();
    
    return g;
  }
  
  Coordinate randomLocation()
  {
    double x = Math.random() * MAX_X;
    double y = Math.random() * MAX_X;
    return new Coordinate(x, y);
  }
}
  
  
