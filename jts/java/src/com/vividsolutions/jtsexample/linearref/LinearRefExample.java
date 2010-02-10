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
package com.vividsolutions.jtsexample.linearref;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.linearref.*;

/**
 * Examples of Linear Referencing
 *
 * @version 1.7
 */

public class LinearRefExample {

  static GeometryFactory fact = new GeometryFactory();
  static WKTReader rdr = new WKTReader(fact);

  public static void main(String[] args)
      throws Exception
  {
    LinearRefExample example = new LinearRefExample();
    example.run();
  }


  public LinearRefExample() {
  }

  public void run()
      throws Exception
  {
    runExtractedLine("LINESTRING (0 0, 10 10, 20 20)", 1, 10);
    runExtractedLine("MULTILINESTRING ((0 0, 10 10), (20 20, 25 25, 30 40))", 1, 20);
  }

  public void runExtractedLine(String wkt, double start, double end)
    throws ParseException
  {
    System.out.println("=========================");
    Geometry g1 = rdr.read(wkt);
    System.out.println("Input Geometry: " + g1);
    System.out.println("Indices to extract: " + start + " " + end);

    LengthIndexedLine indexedLine = new LengthIndexedLine(g1);

    Geometry subLine = indexedLine.extractLine(start, end);
    System.out.println("Extracted Line: " + subLine);

    double[] index = indexedLine.indicesOf(subLine);
    System.out.println("Indices of extracted line: " + index[0] + " " + index[1]);

    Coordinate midpt = indexedLine.extractPoint((index[0] + index[1]) / 2);
    System.out.println("Midpoint of extracted line: " + midpt);
  }
}