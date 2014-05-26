
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
package com.vividsolutions.jtsexample.operation.polygonize;

import java.util.ArrayList;
import java.util.Collection;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

/**
 *  Example of using Polygonizer class to polygonize a set of fully noded linestrings
 *
 * @version 1.7
 */
public class PolygonizeExample
{
  public static void main(String[] args) throws Exception
  {
    PolygonizeExample test = new PolygonizeExample();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  public PolygonizeExample() {
  }

  void run()
      throws Exception
  {
    WKTReader rdr = new WKTReader();
    Collection lines = new ArrayList();

    lines.add(rdr.read("LINESTRING (0 0 , 10 10)"));   // isolated edge
    lines.add(rdr.read("LINESTRING (185 221, 100 100)"));   //dangling edge
    lines.add(rdr.read("LINESTRING (185 221, 88 275, 180 316)"));
    lines.add(rdr.read("LINESTRING (185 221, 292 281, 180 316)"));
    lines.add(rdr.read("LINESTRING (189 98, 83 187, 185 221)"));
    lines.add(rdr.read("LINESTRING (189 98, 325 168, 185 221)"));

    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);

    Collection polys = polygonizer.getPolygons();

    System.out.println("Polygons formed (" + polys.size() + "):");
    System.out.println(polys);
  }

}
