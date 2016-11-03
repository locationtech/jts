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

package test.jts.perf.operation.union;

import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import test.jts.TestFiles;

public class FileUnionPerfTest
{
  static final int MAX_ITER = 1;

  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  GeometryFactory factory = new GeometryFactory();

  public static void main(String[] args) {
    FileUnionPerfTest test = new FileUnionPerfTest();
    try {
      test.test();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  boolean testFailed = false;

  public FileUnionPerfTest() {
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

    UnionPerfTester tester = new UnionPerfTester(polys);
    tester.runAll();
  }

}
