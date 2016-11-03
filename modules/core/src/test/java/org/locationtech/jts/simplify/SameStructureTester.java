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

package org.locationtech.jts.simplify;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.Assert;

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