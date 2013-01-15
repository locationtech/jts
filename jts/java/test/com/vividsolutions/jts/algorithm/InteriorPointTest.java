/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.algorithm;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import test.jts.TestFiles;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.RectangleLineIntersector;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTFileReader;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.Stopwatch;

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
    checkInteriorPointFile(TestFiles.DATA_DIR + "world.wkt");
    checkInteriorPointFile(TestFiles.DATA_DIR + "africa.wkt");
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
    System.out.println();
    System.out.println("  " + sw.getTimeString());
  }

  private void checkInteriorPoint(Geometry g)
  {
    Point ip = g.getInteriorPoint();
    assertTrue(g.contains(ip));
  }

}
