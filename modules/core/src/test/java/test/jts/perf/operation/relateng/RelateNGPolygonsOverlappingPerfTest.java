/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.operation.relateng;

import static org.junit.Assert.assertEquals;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.locationtech.jts.operation.relateng.RelatePredicate;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class RelateNGPolygonsOverlappingPerfTest 
extends PerformanceTestCase
{

  public static void main(String args[]) {
    PerformanceTestRunner.run(RelateNGPolygonsOverlappingPerfTest.class);
  }
  
  private static final int N_ITER = 1;
  
  static double ORG_X = 100;
  static double ORG_Y = ORG_X;
  static double SIZE = 2 * ORG_X;
  static int N_ARMS = 6;
  static double ARM_RATIO = 0.3;
  
  static int GRID_SIZE = 100;
  static double GRID_CELL_SIZE = SIZE / GRID_SIZE;
  
  static int NUM_CASES = GRID_SIZE * GRID_SIZE;
  
  private static final int B_SIZE_FACTOR = 20;
  private static final GeometryFactory factory = new GeometryFactory();

  private Geometry geomA;

  private Geometry[] geomB;
  
  public RelateNGPolygonsOverlappingPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 100, 1000, 10000, 100000, 
        200000 });
    //setRunSize(new int[] { 200000 });
    setRunIterations(N_ITER);
  }

  public void setUp()
  {
    System.out.println("RelateNG perf test");
    System.out.println("SineStar: origin: ("
        + ORG_X + ", " + ORG_Y + ")  size: " + SIZE
        + "  # arms: " + N_ARMS + "  arm ratio: " + ARM_RATIO);   
    System.out.println("# Iterations: " + N_ITER);
    System.out.println("# B geoms: " + NUM_CASES);
  }
  
  public void startRun(int npts)
  {
    Geometry sineStar = SineStarFactory.create(new Coordinate(ORG_X, ORG_Y), SIZE, npts, N_ARMS, ARM_RATIO);
    geomA = sineStar;

    int nptsB = npts * B_SIZE_FACTOR / NUM_CASES;
    if (nptsB < 10 ) nptsB = 10;
    
    geomB =  createSineStarGrid(NUM_CASES, nptsB);
    //geomB =  createCircleGrid(NUM_CASES, nptsB);

    System.out.println("\n-------  Running with A: polygon # pts = " + npts 
        + "   B # pts = " +  nptsB + "  x " + NUM_CASES + " polygons");
    
    /*
    if (npts == 999) {
      System.out.println(geomA);
      
      for (Geometry g : geomB) {
        System.out.println(g);
      }
    }
*/
  }
  
  public void runIntersectsOld()
  {
    for (Geometry b : geomB) {
      geomA.intersects(b);
    }
  }  
  
  public void runIntersectsOldPrep()
  {
    PreparedGeometry pgA = PreparedGeometryFactory.prepare(geomA);
    for (Geometry b : geomB) {
      pgA.intersects(b);
    }
  }  
  
  public void runIntersectsNG()
  {
    for (Geometry b : geomB) {
      RelateNG.relate(geomA, b, RelatePredicate.intersects());
    }
  }  
  
  public void runIntersectsNGPrep()
  {
    RelateNG rng = RelateNG.prepare(geomA);
    for (Geometry b : geomB) {
      rng.evaluate(b, RelatePredicate.intersects());
    }
  }  
  
  public void runContainsOld()
  {
    for (Geometry b : geomB) {
      geomA.contains(b);
    }
  }  
  
  public void runContainsOldPrep()
  {
    PreparedGeometry pgA = PreparedGeometryFactory.prepare(geomA);
    for (Geometry b : geomB) {
      pgA.contains(b);
    }
  }  
  
  public void runContainsNG()
  {
    for (Geometry b : geomB) {
      RelateNG.relate(geomA, b, RelatePredicate.contains());
    }
  }  
  
  public void runContainsNGPrep()
  {
    RelateNG rng = RelateNG.prepare(geomA);
    for (Geometry b : geomB) {
      rng.evaluate(b, RelatePredicate.contains());
    }
  } 
  
  public void xrunContainsNGPrepValidate()
  {
    RelateNG rng = RelateNG.prepare(geomA);
    for (Geometry b : geomB) {
      boolean resultNG = rng.evaluate(b, RelatePredicate.contains());
      boolean resultOld = geomA.contains(b);
      assertEquals(resultNG, resultOld);
    }
  } 
  
  private Geometry[] createSineStarGrid(int nGeoms, int npts) {
    Geometry[] geoms = new Geometry[ NUM_CASES ];
    int index = 0;
    for (int i = 0; i < GRID_SIZE; i++) {
      for (int j = 0; j < GRID_SIZE; j++) {
        double x = GRID_CELL_SIZE/2 + i * GRID_CELL_SIZE;
        double y = GRID_CELL_SIZE/2 + j * GRID_CELL_SIZE;
        Geometry geom = SineStarFactory.create(new Coordinate(x, y), GRID_CELL_SIZE, npts, N_ARMS, ARM_RATIO);
        geoms[index++] = geom;
      }
    }
    return geoms;
  }

  private Geometry[] createCircleGrid(int nGeoms, int npts) {
    Geometry[] geoms = new Geometry[ NUM_CASES ];
    int index = 0;
    for (int i = 0; i < GRID_SIZE; i++) {
      for (int j = 0; j < GRID_SIZE; j++) {
        double x = GRID_CELL_SIZE/2 + i * GRID_CELL_SIZE;
        double y = GRID_CELL_SIZE/2 + j * GRID_CELL_SIZE;
        Coordinate p = new Coordinate(x, y);
        Geometry geom = factory.createPoint(p).buffer(GRID_CELL_SIZE / 2.0);
        geoms[index++] = geom;
      }
    }
    return geoms;
  }


}
