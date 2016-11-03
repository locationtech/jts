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

package test.jts.perf.operation.buffer;

import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.Stopwatch;

import test.jts.TestFiles;

public class FileBufferPerfTest
{
  static final int MAX_ITER = 1;

  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);

  GeometryFactory factory = new GeometryFactory();

  public static void main(String[] args) {
    FileBufferPerfTest test = new FileBufferPerfTest();
    try {
      test.test();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  boolean testFailed = false;

  public FileBufferPerfTest() {
  }

  public void test()
  throws Exception
  {
    test(TestFiles.getResourceFilePath("africa.wkt"));
    // test(TestFiles.getResourceFilePath("world.wkt"));
    // test(TestFiles.getResourceFilePath("bc-250k.wkt"));
    // test(TestFiles.getResourceFilePath("bc_20K.wkt"));
  }

  public void test(String filename)
    throws Exception
  {
    WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr);
    List polys = fileRdr.read();

    runAll(polys, 0.01);
    runAll(polys, 0.1);
    runAll(polys, 1.0);
    runAll(polys, 10.0);
    runAll(polys, 100.0);
    runAll(polys, 1000.0);
  }

  void runAll(List polys, double distance)
  {
    System.out.println("Geom count = " + polys.size() + "   distance = " + distance);
    Stopwatch sw = new Stopwatch();
    for (Iterator i = polys.iterator(); i.hasNext(); ) {
      Geometry g = (Geometry) i.next();
      g.buffer(distance);
      System.out.print(".");
    }
    System.out.println();
    System.out.println("   Time = " + sw.getTimeString());
  }
}
