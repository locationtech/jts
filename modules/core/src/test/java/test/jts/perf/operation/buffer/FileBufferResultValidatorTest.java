
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
package test.jts.perf.operation.buffer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.operation.buffer.validate.BufferResultValidator;
import org.locationtech.jts.util.Stopwatch;

import junit.framework.TestCase;


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
    //    runTest(TestFiles.getResourceFilePath("world.wkt"));
    runTest("/testdata/africa.wkt");
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
    //System.out.println("Geom count = " + geoms.size() + "   distance = " + dist);
    for (Iterator i = geoms.iterator(); i.hasNext(); ) {
      Geometry g = (Geometry) i.next();
      runBuffer(g, dist);
      runBuffer(g.reverse(), dist);
      //System.out.print(".");
    }
    //System.out.println("  " + sw.getTimeString());

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
