/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Location;

/**
 * Functions for locating points within basic geometric
 * structures such as lines and rings.
 * 
 * @author Martin Davis
 *
 */
public class PointLocation {

  /**
   * Tests whether a point lies on the line defined by a list of
   * coordinates.
   * 
   * @param p the point to test
   * @param line the line coordinates
   * @return true if the point is a vertex of the line or lies in the interior
   *         of a line segment in the line
   */
  public static boolean isOnLine(Coordinate p, Coordinate[] line)
  {
    LineIntersector lineIntersector = new RobustLineIntersector();
    for (int i = 1; i < line.length; i++) {
      Coordinate p0 = line[i - 1];
      Coordinate p1 = line[i];
      lineIntersector.computeIntersection(p, p0, p1);
      if (lineIntersector.hasIntersection()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether a point lies on the line defined by a 
   * {@link CoordinateSequence}.
   * 
   * @param p the point to test
   * @param line the line coordinates
   * @return true if the point is a vertex of the line or lies in the interior
   *         of a line segment in the line
   */
  public static boolean isOnLine(Coordinate p, CoordinateSequence line)
  {
    LineIntersector lineIntersector = new RobustLineIntersector();
    Coordinate p0 = new Coordinate();
    Coordinate p1 = new Coordinate();
    int n = line.size();
    for (int i = 1; i < n; i++) {
      line.getCoordinate(i-1, p0);
      line.getCoordinate(i, p1);
      lineIntersector.computeIntersection(p, p0, p1);
      if (lineIntersector.hasIntersection()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether a point lies inside or on a ring. The ring may be oriented in
   * either direction. A point lying exactly on the ring boundary is considered
   * to be inside the ring.
   * <p>
   * This method does <i>not</i> first check the point against the envelope of
   * the ring.
   * 
   * @param p
   *          point to check for ring inclusion
   * @param ring
   *          an array of coordinates representing the ring (which must have
   *          first point identical to last point)
   * @return true if p is inside ring
   * 
   * @see PointLocation#locateInRing(Coordinate, Coordinate[])
   */
  public static boolean isInRing(Coordinate p, Coordinate[] ring)
  {
    return PointLocation.locateInRing(p, ring) != Location.EXTERIOR;
  }

  /**
   * Determines whether a point lies in the interior, on the boundary, or in the
   * exterior of a ring. The ring may be oriented in either direction.
   * <p>
   * This method does <i>not</i> first check the point against the envelope of
   * the ring.
   * 
   * @param p
   *          point to check for ring inclusion
   * @param ring
   *          an array of coordinates representing the ring (which must have
   *          first point identical to last point)
   * @return the {@link Location} of p relative to the ring
   */
  public static int locateInRing(Coordinate p, Coordinate[] ring)
  {
    return RayCrossingCounter.locatePointInRing(p, ring);
  }

}
