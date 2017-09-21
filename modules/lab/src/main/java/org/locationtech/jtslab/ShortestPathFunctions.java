package org.locationtech.jtslab;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.offsetcurve.ShortestPath;

public class ShortestPathFunctions {
  public static Geometry path(Geometry lines, Geometry startEnd) {
    Coordinate[] se = startEnd.getCoordinates();
    Geometry path = ShortestPath.findPath(lines, se[0], se[1]);
    return path;
}
}
