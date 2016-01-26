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

package com.vividsolutions.jts.simplify;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;

/**
 * Test if two geometries have the same structure
 * (but not necessarily the same coordinate sequences or adjacencies).
 */
public class SameStructureTester {

  public static boolean isSameStructure(Geometry g1, Geometry g2)
  {
    if (g1.getClass() != g2.getClass())
      return false;
    if (g1 instanceof GeometryCollection)
      return isSameStructureCollection((GeometryCollection) g1, (GeometryCollection) g2);
    else if (g1 instanceof Polygon)
      return isSameStructurePolygon((Polygon) g1, (Polygon) g2);
    else if (g1 instanceof LineString)
      return isSameStructureLineString((LineString) g1, (LineString) g2);
    else if (g1 instanceof Point)
      return isSameStructurePoint((Point) g1, (Point) g2);

    Assert.shouldNeverReachHere(
        "Unsupported Geometry class: " + g1.getClass().getName());
    return false;
  }

  private static boolean isSameStructureCollection(GeometryCollection g1, GeometryCollection g2)
  {
    if (g1.getNumGeometries() != g2.getNumGeometries())
        return false;
    for (int i = 0; i < g1.getNumGeometries(); i++) {
      if (! isSameStructure(g1.getGeometryN(i), g2.getGeometryN(i)))
        return false;
    }
    return true;
  }

  private static boolean isSameStructurePolygon(Polygon g1, Polygon g2)
  {
    if (g1.getNumInteriorRing() != g2.getNumInteriorRing())
        return false;
    // could check for both empty or nonempty here
    return true;
  }

  private static boolean isSameStructureLineString(LineString g1, LineString g2)
  {
    // could check for both empty or nonempty here
    return true;
  }

  private static boolean isSameStructurePoint(Point g1, Point g2)
  {
    // could check for both empty or nonempty here
    return true;
  }

  private SameStructureTester() {
  }
}