/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.Stopwatch;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;
import test.jts.TestFiles;


public class InteriorPointTest extends GeometryTestCase
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

  public void testPolygonZeroArea() {
    checkInteriorPoint(read("POLYGON ((10 10, 10 10, 10 10, 10 10))"), new Coordinate(10, 10));
  }
  
  public void testAll() throws Exception
  {
    checkInteriorPointFile(TestFiles.getResourceFilePath("world.wkt"));
    //checkInteriorPointFile(TestFiles.getResourceFilePath("africa.wkt"));
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
  
  private void checkInteriorPoint(Geometry g, Coordinate expectedPt)
  {
    Point ip = g.getInteriorPoint();
    assertTrue(ip.getCoordinate().equals2D(expectedPt));
  }

  /**
  public void testPointInteriorPoint() throws ParseException {
    Geometry point = rdr.read("Point(10 10)");
    assertTrue(point.getInteriorPoint().equals(rdr.read("POINT(10 10)")));
  }

  public void testMultiPointInteriorPoint() throws ParseException {
    Geometry point = rdr.read("MULTIPOINT ((60 300), (200 200), (240 240), (200 300), (40 140), (80 240), (140 240), (100 160), (140 200), (60 200))");
    assertTrue(point.getInteriorPoint().equals(rdr.read("POINT (140 240)")));
  }

  public void testRelate() throws ParseException {
    Geometry point = rdr.read("POINT (10 10)");
    Geometry line = rdr.read("LINESTRING (10 10, 10 10)");
    assertTrue(point.equalsTopo(line));
  }

  public void testGeometryCollection() throws ParseException {
    Geometry gc = rdr.read("GEOMETRYCOLLECTION (POLYGON ((10 10, 10 10, 10 10, 10 10)), \n" +
            "  LINESTRING (20 20, 30 30))");
    assertTrue(gc.getInteriorPoint().equals(rdr.read("POINT(20 20)")));
  }


  public void testZeroLengthLineStringInteriorPoint() throws ParseException {
    Geometry line = rdr.read("LineString(10 10, 10 10)");
    assertTrue(line.getInteriorPoint().equals(rdr.read("POINT(10 10)")));
  }
   **/

}
