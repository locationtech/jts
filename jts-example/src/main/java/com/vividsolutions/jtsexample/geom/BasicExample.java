
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Shows basic ways of creating and operating on geometries
 *
 * @version 1.7
 */
public class BasicExample
{
  public static void main(String[] args)
      throws Exception
  {
    // read a geometry from a WKT string (using the default geometry factory)
    Geometry g1 = new WKTReader().read("LINESTRING (0 0, 10 10, 20 20)");
    System.out.println("Geometry 1: " + g1);

    // create a geometry by specifying the coordinates directly
    Coordinate[] coordinates = new Coordinate[]{new Coordinate(0, 0),
      new Coordinate(10, 10), new Coordinate(20, 20)};
    // use the default factory, which gives full double-precision
    Geometry g2 = new GeometryFactory().createLineString(coordinates);
    System.out.println("Geometry 2: " + g2);

    // compute the intersection of the two geometries
    Geometry g3 = g1.intersection(g2);
    System.out.println("G1 intersection G2: " + g3);
  }
}
