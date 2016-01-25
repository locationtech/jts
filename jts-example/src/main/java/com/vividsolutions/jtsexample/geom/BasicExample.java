
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
