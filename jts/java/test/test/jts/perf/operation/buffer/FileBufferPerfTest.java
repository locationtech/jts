/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package test.jts.perf.operation.buffer;

import java.util.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTFileReader;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.*;
import test.jts.*;

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
    test(TestFiles.DATA_DIR + "africa.wkt");
//    test(TestFiles.DATA_DIR + "world.wkt");
//    test(TestFiles.DATA_DIR + "bc-250k.wkt");
//    test(TestFiles.DATA_DIR + "bc_20K.wkt");
    
//    test("C:\\data\\martin\\proj\\jts\\data\\veg.wkt");
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
