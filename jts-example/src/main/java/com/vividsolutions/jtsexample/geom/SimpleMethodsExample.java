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
package com.vividsolutions.jtsexample.geom;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;

/**
 * An example showing a simple use of JTS methods for:
 * <ul>
 * <li>WKT reading
 * <li>intersection
 * <li>relate
 * <li>WKT output
 * </ul>
 * <p>
 * The expected output from this program is:
 * <pre>
 * ----------------------------------------------------------
 * A = POLYGON ((40 100, 40 20, 120 20, 120 100, 40 100))
 * B = LINESTRING (20 80, 80 60, 100 140)
 * A intersection B = LINESTRING (40 73.33333333333334, 80 60, 90 100)
 * A relate C = 1F20F1102
 * ----------------------------------------------------------
 * </pre>
 *
 * @version 1.7
 */
public class SimpleMethodsExample
{
  public static void main(String[] args) {
    SimpleMethodsExample example = new SimpleMethodsExample();
    try {
      example.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public SimpleMethodsExample() {
  }

  public void run()
    throws ParseException
  {
    GeometryFactory fact = new GeometryFactory();
    WKTReader wktRdr = new WKTReader(fact);

    String wktA = "POLYGON((40 100, 40 20, 120 20, 120 100, 40 100))";
    String wktB = "LINESTRING(20 80, 80 60, 100 140)";
    Geometry A = wktRdr.read(wktA);
    Geometry B = wktRdr.read(wktB);
    Geometry C = A.intersection(B);
    System.out.println("A = " + A);
    System.out.println("B = " + B);
    System.out.println("A intersection B = " + C);
    System.out.println("A relate C = " + A.relate(B));
  }

}
