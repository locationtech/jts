
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Martin Davis
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Martin Davis BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package test.jts.perf.operation.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.operation.buffer.validate.*;

import junit.framework.TestCase;
import test.jts.TestFiles;
import test.jts.junit.*;


/**
 * @version 1.7
 */
public class FileBufferResultValidatorTest extends TestCase {

	WKTReader rdr = new WKTReader();
	
  public FileBufferResultValidatorTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(FileBufferResultValidatorTest.class);
  }

  public void testAfrica() throws Exception 
  {
//    runTest(TestFiles.DATA_DIR + "world.wkt");
    runTest("../../../../../data/africa.wkt");
  }
  
  void runTest(String resource) 
  throws Exception
  {
    InputStream is = this.getClass().getResourceAsStream(resource);
    runTest(new WKTFileReader(new InputStreamReader(is), rdr));
  }

  void runTest(WKTFileReader fileRdr)
  throws Exception
  {
    List polys = fileRdr.read();
    
    runAll(polys, 0.01);
    runAll(polys, 0.1);
    runAll(polys, 1.0);
    runAll(polys, 10.0);
    runAll(polys, 100.0);
    runAll(polys, 1000.0);
    
  }
  
  void runAll(List geoms, double dist)
  {
  	Stopwatch sw = new Stopwatch();
    System.out.println("Geom count = " + geoms.size() + "   distance = " + dist);
    for (Iterator i = geoms.iterator(); i.hasNext(); ) {
      Geometry g = (Geometry) i.next();
      runBuffer(g, dist);
      runBuffer(g.reverse(), dist);
      System.out.print(".");
    }
    System.out.println("  " + sw.getTimeString());

  }
  void runBuffer(Geometry g, double dist)
  {
  	Geometry buf = g.buffer(dist);
    BufferResultValidator validator = new BufferResultValidator(g, dist, buf);
    
    if (! validator.isValid()) {
      String msg = validator.getErrorMessage();

      System.out.println(msg);
      System.out.println(WKTWriter.toPoint(validator.getErrorLocation()));
      System.out.println(g);
    }
  	assertTrue(validator.isValid());
  }
}
