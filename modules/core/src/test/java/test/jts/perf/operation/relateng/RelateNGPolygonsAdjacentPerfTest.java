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

import java.io.FileReader;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.relateng.IntersectionMatrixPattern;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.locationtech.jts.operation.relateng.RelatePredicate;

import test.jts.TestFiles;
import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class RelateNGPolygonsAdjacentPerfTest 
extends PerformanceTestCase
{

  public static void main(String args[]) {
    PerformanceTestRunner.run(RelateNGPolygonsAdjacentPerfTest.class);
  }
  
  WKTReader rdr = new WKTReader();

  private static final int N_ITER = 10;
  
  private List<Geometry> polygons;
  
  public RelateNGPolygonsAdjacentPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 1 });
    //setRunSize(new int[] { 20 });
    setRunIterations(N_ITER);
  }

  public void setUp() throws Exception
  {
    String resource = "europe.wkt";
    //String resource = "world.wkt";
    loadPolygons(resource);
    
    System.out.println("RelateNG Performance Test - Adjacent Polygons ");
    System.out.println("Dataset: " + resource);
    
    System.out.println("# geometries: " + polygons.size()
        + "   # pts: " + numPts(polygons));
    System.out.println("----------------------------------");
  }
  
  private static int numPts(List<Geometry> geoms) {
    int n = 0;
    for (Geometry g : geoms) {
      n += g.getNumPoints();
    }
    return n;
  }

  private void loadPolygons(String resourceName) throws Exception {
    String path = TestFiles.getResourceFilePath(resourceName);
    WKTFileReader wktFileRdr = new WKTFileReader(new FileReader(path), rdr);
    polygons = wktFileRdr.read();
  }

  public void startRun(int npts)
  {

  }
  
  public void runIntersectsOld()
  {
    for (Geometry a : polygons) {
      for (Geometry b : polygons) {
        a.intersects(b);
      }
    }
  }  
  
  public void runIntersectsOldPrep()
  {
    for (Geometry a : polygons) {
      PreparedGeometry pgA = PreparedGeometryFactory.prepare(a);
      for (Geometry b : polygons) {
        pgA.intersects(b);
      }
    }
  }  
  
  public void runIntersectsNG()
  {
    for (Geometry a : polygons) {
      for (Geometry b : polygons) {
        RelateNG.relate(a, b, RelatePredicate.intersects());
      }
    }
  }  
  
  public void runIntersectsNGPrep()
  {
    for (Geometry a : polygons) {
      RelateNG rng = RelateNG.prepare(a);
      for (Geometry b : polygons) {
        rng.evaluate(b, RelatePredicate.intersects());
      }
    }
  }  
  
  public void runTouchesOld()
  {
    for (Geometry a : polygons) {
      for (Geometry b : polygons) {
        a.touches(b);
      }
    }
  }   
  
  public void runTouchesNG()
  {
    for (Geometry a : polygons) {
      for (Geometry b : polygons) {
        RelateNG.relate(a, b, RelatePredicate.touches());
      }
    }
  }  
  
  public void runTouchesNGPrep()
  {
    for (Geometry a : polygons) {
      RelateNG rng = RelateNG.prepare(a);
      for (Geometry b : polygons) {
        rng.evaluate(b, RelatePredicate.touches());
      }
    }
  }

  public void runAdjacentOld()
  {
    for (Geometry a : polygons) {
      for (Geometry b : polygons) {
        a.relate(b, IntersectionMatrixPattern.ADJACENT);
      }
    }
  }   
  
  public void runAdjacentNG()
  {
    for (Geometry a : polygons) {
      for (Geometry b : polygons) {
        RelateNG.relate(a, b, RelatePredicate.matches(IntersectionMatrixPattern.ADJACENT));
      }
    }
  }  
  
  public void runAdjacentNGPrep()
  {
    for (Geometry a : polygons) {
      RelateNG rng = RelateNG.prepare(a);
      for (Geometry b : polygons) {
        rng.evaluate(b, RelatePredicate.matches(IntersectionMatrixPattern.ADJACENT));
      }
    }
  }

  public void runInteriorIntersectsOld()
  {
    for (Geometry a : polygons) {
      for (Geometry b : polygons) {
        a.relate(b, IntersectionMatrixPattern.INTERIOR_INTERSECTS);
      }
    }
  }   
  
  public void runInteriorIntersectsNG()
  {
    for (Geometry a : polygons) {
      for (Geometry b : polygons) {
        RelateNG.relate(a, b, RelatePredicate.matches(IntersectionMatrixPattern.INTERIOR_INTERSECTS));
      }
    }
  }  
  
  public void runInteriorIntersectsNGPrep()
  {
    for (Geometry a : polygons) {
      RelateNG rng = RelateNG.prepare(a);
      for (Geometry b : polygons) {
        rng.evaluate(b, RelatePredicate.matches(IntersectionMatrixPattern.INTERIOR_INTERSECTS));
      }
    }
  }

}
