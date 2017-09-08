package org.locationtech.jtstest.function;

import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class PointLocationFunctions {

  public static boolean isPointInRing(Geometry g1, Geometry g2) {
    Geometry ring = g1;
    Geometry pt = g2;
    if (g1.getNumPoints() == 1) {
      ring = g2;
      pt = g1;
    }
    Coordinate[] ptsRing = OrientationFunctions.getRing(ring);
    if (ptsRing == null) return false;
    return PointLocation.isInRing(pt.getCoordinate(), ptsRing);
  }

}
