/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.dissolve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.util.Memory;

import test.jts.junit.GeometryUtils;
import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;


public class LineDissolvePerfTest  extends PerformanceTestCase 
{
  public static void main(String args[]) {
    PerformanceTestRunner.run(LineDissolvePerfTest.class);
  }

  public LineDissolvePerfTest(String name) {
    super(name);
    setRunSize(new int[] {1, 2, 3, 4, 5});
    setRunIterations(1);
  }

  Collection data;
  
  public void setUp() throws IOException, ParseException
  {
    System.out.println("Loading data...");
    data = GeometryUtils.readWKTFile("/Users/mdavis/myproj/jts/svn/jts-topo-suite/trunk/jts/testdata/world.wkt");
  }
  
  public void runDissolver_World()
  {
    LineDissolver dis = new LineDissolver();
    dis.add(data);
    Geometry result = dis.getResult();
    System.out.println();
    System.out.println(Memory.allString());
  }
  
  public void runBruteForce_World()
  {
    Geometry result = dissolveLines(data);
    System.out.println(Memory.allString());
  }
  
  private Geometry dissolveLines(Collection lines) {
    Geometry linesGeom = extractLines(lines);
    return dissolveLines(linesGeom);
  }
  
  private Geometry dissolveLines(Geometry lines) {
    Geometry dissolved = lines.union();
    LineMerger merger = new LineMerger();
    merger.add(dissolved);
    Collection mergedColl = merger.getMergedLineStrings();
    Geometry merged = lines.getFactory().buildGeometry(mergedColl);
    return merged;
  }

  Geometry extractLines(Collection geoms)
  {
    GeometryFactory factory = null;
    List lines = new ArrayList();
    for (Iterator i = geoms.iterator(); i.hasNext(); ) {
      Geometry g = (Geometry) i.next();
      if (factory == null)
          factory = g.getFactory();
      lines.addAll(LinearComponentExtracter.getLines(g));
    }
    return factory.buildGeometry(geoms);
  }
  
}
