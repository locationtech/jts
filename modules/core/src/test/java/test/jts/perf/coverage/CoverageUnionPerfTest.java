/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.coverage;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.overlayng.CoverageUnion;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

/**
 * Shows how linear performance of {@link GeometryCollection#getDimension()}
 * affects performance.
 * (See https://github.com/locationtech/jts/issues/1100)
 * 
 * @author mdavis
 *
 */
public class CoverageUnionPerfTest  extends PerformanceTestCase
{
  public static void main(String[] args) {
    PerformanceTestRunner.run(CoverageUnionPerfTest.class);  
  }
  
  private Geometry grid;

  public CoverageUnionPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 10_000, 20_000, 40_000, 100_000, 200_000, 400_000 });
  }

  public void startRun(int nCells)
  {
    grid = createGrid(100.0, nCells, new GeometryFactory());
    System.out.println("\n-------  Running with cells = " + nCells);
  }
  
  private static Geometry createGrid(double size, int nCells, GeometryFactory geomFact) {
    
    int nCellsOnSideY = (int) Math.sqrt(nCells);
    int nCellsOnSideX = nCells / nCellsOnSideY;
    
    double cellSizeX = size / nCellsOnSideX;
    double cellSizeY = size / nCellsOnSideY;
    
    List<Geometry> geoms = new ArrayList<Geometry>(); 
  
    for (int i = 0; i < nCellsOnSideX; i++) {
      for (int j = 0; j < nCellsOnSideY; j++) {
        double x = 0 + i * cellSizeX;
        double y = 0 + j * cellSizeY;
        double x2 = 0 + (i + 1) * cellSizeX;
        double y2 = 0 + (j + 1) * cellSizeY;
      
        Envelope cellEnv = new Envelope(x, x2, y, y2);
        geoms.add(geomFact.toGeometry(cellEnv));
      }
    }
    return geomFact.createGeometryCollection(
        GeometryFactory.toGeometryArray(geoms));
  }
  
  public void runUnion() {
    CoverageUnion.union(grid);
  }
}
