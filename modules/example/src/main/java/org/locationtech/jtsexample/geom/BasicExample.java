
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
package org.locationtech.jtsexample.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

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
