/*
 * Copyright (c) 2020 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.operation.overlayarea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.operation.overlayarea.OverlayArea;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;
import test.jts.util.IOUtil;

public class OverlayAreaGridsPerfTest extends PerformanceTestCase
{
  public static void main(String args[]) {
    PerformanceTestRunner.run(OverlayAreaGridsPerfTest.class);
  }
  boolean verbose = true;
  private Geometry geom;
  private Geometry grid;
  
  public OverlayAreaGridsPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 100, 200, 1000, 2000, 10_000, 20_000, 40_000, 100_000, 200_000, 400_000, 1000_000 });
    //setRunSize(new int[] { 100, 200, 20_000, 40_000, 400_000, 1000_000 });
    setRunIterations(1);
  }

  @Override
  public void startRun(int size) throws IOException, ParseException
  {
    //geom = createSineStar(10_000, 0);
    //geom = (Geometry) IOUtil.readWKTFile("D:/proj/jts/testing/intersectionarea/dvg_nw.wkt").toArray()[0];
    geom = (Geometry) IOUtil.readWKTFile("/Users/mdavis/proj/jts/git/jts/modules/core/src/test/resources/testdata/africa.wkt").toArray()[0];
    grid = grid(geom, size);
    
    System.out.printf("\n---  Running with Polygon size %d, grid # = %d -------------\n",
        geom.getNumPoints(), grid.getNumGeometries());
  }
  
  public void runOverlayArea()
  {
    double area = 0.0;
    OverlayArea intArea = new OverlayArea(geom);
    //System.out.println("Test 1 : Iter # " + iter++);
    for (int i = 0; i < grid.getNumGeometries(); i++) {
      Geometry cell = grid.getGeometryN(i);
      area += intArea.intersectionArea(cell);
      //checkOrigArea(geom, cell);
    }
    System.out.println(">>> OverlayArea = " + area);
  }
  
  private void checkOrigArea(Geometry geom0, Geometry geom1) {
    double intArea = OverlayArea.intersectionArea(geom0, geom1);
    double origArea = geom0.intersection(geom1).getArea();
    if (! isEqual(intArea, origArea, 0.1)) {
      System.out.println("********************   Areas are different! OA = " 
            + intArea + "  Orig = " + origArea);
    }
  }

  private boolean isEqual(double v1, double v2, double tol) {
    if (v1 == v2) return true;
    double diff = Math.abs( (v1 - v2) / (v1 + v2));
    return diff < tol;
  }

  public void runFullIntersection()
  {
    double area = 0.0;
    //System.out.println("Test 1 : Iter # " + iter++);
    for (int i = 0; i < grid.getNumGeometries(); i++) {
      Geometry cell = grid.getGeometryN(i);
      area += geom.intersection(cell).getArea();
    }
    System.out.println(">>> Full Intersection area = " + area);
  }
  
  public void runFullIntersectionPrep()
  {
    double area = 0.0;
    PreparedGeometry geomPrep = PreparedGeometryFactory.prepare(geom);
    //System.out.println("Test 1 : Iter # " + iter++);
    for (int i = 0; i < grid.getNumGeometries(); i++) {
      Geometry cell = grid.getGeometryN(i);
      area += intAreaFullPrep(geom, geomPrep, cell);
    }
    System.out.println(">>> Full Intersection area = " + area);
  }
  
  private static double intAreaFullPrep(Geometry geom, PreparedGeometry geomPrep, Geometry geom1) {
    if (! geomPrep.intersects(geom1)) return 0.0;
    if (geomPrep.contains(geom1)) return geom1.getArea();

    double intArea = geom.intersection(geom1).getArea();
    return intArea;
  }
  
  public static Geometry createSineStar(int nPts, double offset)
  {
    SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(new Coordinate(0, offset));
    gsf.setSize(100);
    gsf.setNumPoints(nPts);
    
    Geometry g = gsf.createSineStar();
    
    return g;
  }
  
  public static Geometry grid(Geometry g, int nCells)
  {
    Envelope env = g.getEnvelopeInternal();
    GeometryFactory geomFact = g.getFactory();
    
    int nCellsOnSideY = (int) Math.sqrt(nCells);
    int nCellsOnSideX = nCells / nCellsOnSideY;
    
    // alternate: make square cells, with varying grid width/height
    //double extent = env.minExtent();
    //double nCellsOnSide = Math.max(nCellsOnSideY, nCellsOnSideX);
    
    double cellSizeX = env.getWidth() / nCellsOnSideX;
    double cellSizeY = env.getHeight() / nCellsOnSideY;
    
    List geoms = new ArrayList(); 

    for (int i = 0; i < nCellsOnSideX; i++) {
      for (int j = 0; j < nCellsOnSideY; j++) {
        double x = env.getMinX() + i * cellSizeX;
        double y = env.getMinY() + j * cellSizeY;
        double x2 = env.getMinX() + (i + 1) * cellSizeX;
        double y2 = env.getMinY() + (j + 1) * cellSizeY;
      
        Envelope cellEnv = new Envelope(x, x2, y, y2);
        geoms.add(geomFact.toGeometry(cellEnv));
      }
    }
    return geomFact.buildGeometry(geoms);
  }
}
