

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
package org.locationtech.jts.operation.valid;

import org.locationtech.jts.geom.*;

/**
 * Implements the appropriate checks for repeated points
 * (consecutive identical coordinates) as defined in the
 * JTS spec.
 *
 * @version 1.7
 */
public class RepeatedPointTester {

  // save the repeated coord found (if any)
  private Coordinate repeatedCoord;

  public RepeatedPointTester() {
  }

  public Coordinate getCoordinate() { return repeatedCoord; }

  public boolean hasRepeatedPoint(Geometry g)
  {
    if (g.isEmpty()) return false;
    if (g instanceof Point)                   return false;
    else if (g instanceof MultiPoint)         return false;
                        // LineString also handles LinearRings
    else if (g instanceof LineString)         return hasRepeatedPoint(((LineString) g).getCoordinates());
    else if (g instanceof Polygon)            return hasRepeatedPoint((Polygon) g);
    else if (g instanceof GeometryCollection) return hasRepeatedPoint((GeometryCollection) g);
    else  throw new UnsupportedOperationException(g.getClass().getName());
  }

  public boolean hasRepeatedPoint(Coordinate[] coord)
  {
    for (int i = 1; i < coord.length; i++) {
      if (coord[i - 1].equals(coord[i]) ) {
        repeatedCoord = coord[i];
        return true;
      }
    }
    return false;
  }
  private boolean hasRepeatedPoint(Polygon p)
  {
    if (hasRepeatedPoint(p.getExteriorRing().getCoordinates())) return true;
    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      if (hasRepeatedPoint(p.getInteriorRingN(i).getCoordinates())) return true;
    }
    return false;
  }
  private boolean hasRepeatedPoint(GeometryCollection gc)
  {
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Geometry g = gc.getGeometryN(i);
      if (hasRepeatedPoint(g)) return true;
    }
    return false;
  }


}
