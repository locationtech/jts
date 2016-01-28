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
package org.locationtech.jts.algorithm;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.Stopwatch;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import test.jts.TestFiles;


public class InteriorPointTest extends TestCase
{
  public static void main(String args[])
  {
    TestRunner.run(InteriorPointTest.class);
  }

  WKTReader rdr = new WKTReader();

  public InteriorPointTest(String name)
  {
    super(name);
  }

  public void testAll() throws Exception
  {
    checkInteriorPointFile(TestFiles.getResourceFilePath("world.wkt"));
    checkInteriorPointFile(TestFiles.getResourceFilePath("africa.wkt"));
    //checkInteriorPointFile("../../../../../data/africa.wkt");
  }


  void checkInteriorPointFile(String file) throws Exception
  {
    WKTFileReader fileRdr = new WKTFileReader(new FileReader(file), rdr);
    checkInteriorPointFile(fileRdr);
  }

  void checkInteriorPointResource(String resource) throws Exception
  {
    InputStream is = this.getClass().getResourceAsStream(resource);
    WKTFileReader fileRdr = new WKTFileReader(new InputStreamReader(is), rdr);
    checkInteriorPointFile(fileRdr);
  }

  private void checkInteriorPointFile(WKTFileReader fileRdr) throws IOException, ParseException
  {
    List polys = fileRdr.read();
    checkInteriorPoint(polys);
  }

  void checkInteriorPoint(List geoms)
  {
    Stopwatch sw = new Stopwatch();
    for (Iterator i = geoms.iterator(); i.hasNext();) {
      Geometry g = (Geometry) i.next();
      checkInteriorPoint(g);
      System.out.print(".");
    }
    //System.out.println();
    //System.out.println("  " + sw.getTimeString());
  }

  private void checkInteriorPoint(Geometry g)
  {
    Point ip = g.getInteriorPoint();
    assertTrue(g.contains(ip));
  }

}
