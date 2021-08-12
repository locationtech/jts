/*
 * Copyright (c) 2017 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

public class OrientationFunctions {

  public static boolean isCCW(Geometry g)
  {
    Coordinate[] ptsRing = OrientationFunctions.getRing(g);
    if (ptsRing == null) return false;
    return Orientation.isCCW(ptsRing);
  }

  public static int orientationIndex(Geometry segment, Geometry ptGeom) {
    if (segment.getNumPoints() != 2 || ptGeom.getNumPoints() != 1) {
      throw new IllegalArgumentException("A must have two points and B must have one");
    }
    Coordinate[] segPt = segment.getCoordinates();
    
    Coordinate p = ptGeom.getCoordinate();
    int index = Orientation.index(segPt[0], segPt[1], p);
    return index;
  }

  static Coordinate[] getRing(Geometry g) {
    Coordinate[] pts = null;
    if (g instanceof Polygon) {
      pts = ((Polygon) g).getExteriorRing().getCoordinates();
    } 
    else if (g instanceof LineString
        && ((LineString) g).isClosed()) {
      pts = g.getCoordinates();
    }
    return pts;
  }

}
