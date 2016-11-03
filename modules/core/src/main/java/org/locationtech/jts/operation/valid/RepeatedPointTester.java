

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
package org.locationtech.jts.operation.valid;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

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
